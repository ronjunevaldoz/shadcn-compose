#!/usr/bin/env python3
from __future__ import annotations

import argparse
import re
from pathlib import Path


REQUIRED_MARKERS = (
    "## When to Use This Skill",
    "Trigger keywords:",
    "metadata:",
    "last-updated:",
    "## Changelog",
)

# Design-system content checks ─────────────────────────────────────────────────

# The base skill declares exactly these 6 core components — all must be implemented.
_DS_CORE_COMPONENTS = (
    "fun AppButton",
    "fun AppBadge",
    "fun AppCard",
    "fun AppChip",
    "fun AppTextField",
    "fun AppText",
)

# AppTheme.spacing.X / AppTheme.colors.X / AppTheme.typography.X — static access anti-pattern.
# Requires the trailing property name (.lg, .primary, …) to avoid matching inline-doc backticks.
_DS_STATIC_ACCESS_RE = re.compile(r"AppTheme\.(spacing|colors|typography)\.\w+")

# override val X = N.dp — hardcoded dp in a sealed-interface layout property.
# `override val dp = N.dp` (component dimension enums like IconSize, AvatarSize) is exempt.
# Plain `val` token definitions are also exempt; only other `override val` properties are flagged.
_DS_HARDCODED_DP_RE = re.compile(r"override val (?!dp\b)\w+ = \d+\.dp\b")


def _check_design_system(skill_dir: Path, text: str, findings: list[str]) -> None:
    """Targeted content checks for the design-system skills."""
    name = skill_dir.name
    if name not in (
        "kotlin-multiplatform-design-system",
        "kotlin-multiplatform-design-system-extended",
    ):
        return

    if name == "kotlin-multiplatform-design-system":
        # All 6 declared core components must have an implementation.
        missing = [c for c in _DS_CORE_COMPONENTS if c not in text]
        if missing:
            findings.append(
                f"{name}: missing component implementation(s): "
                + ", ".join(missing)
            )

        # Must have a Component Previews section with at least one previews/ block.
        if "## Component Previews" not in text or "### `previews/" not in text:
            findings.append(
                f"{name}: missing Component Previews section — "
                "add '## Component Previews' with previews/AppXxxPreview.kt blocks"
            )

        # `enum class TextStyle` collides with Compose's own TextStyle — rename to AppTextStyle.
        if re.search(r"\benum class TextStyle\b", text):
            findings.append(
                f"{name}: 'enum class TextStyle' shadows Compose's TextStyle — "
                "rename to AppTextStyle"
            )

        # ExperimentalStylesApi usage requires a visible @OptIn note.
        if "ExperimentalStylesApi" in text and "OptIn(ExperimentalStylesApi" not in text:
            findings.append(
                f"{name}: ExperimentalStylesApi used without @OptIn — "
                "add @file:OptIn(ExperimentalStylesApi::class) or a note in Steps 5–6"
            )

    # Both skills: AppTheme.<property>.<field> is a compile error — use appTheme accessor.
    if _DS_STATIC_ACCESS_RE.search(text):
        findings.append(
            f"{name}: AppTheme.<property>.<field> static access found — "
            "use the appTheme @Composable accessor instead"
        )

    # Both skills: hardcoded dp in override val should reference AppSpacing() tokens.
    if _DS_HARDCODED_DP_RE.search(text):
        findings.append(
            f"{name}: 'override val X = N.dp' found — "
            "use AppSpacing() token reference (e.g. AppSpacing().xxl)"
        )


FAST_MOVING_HINTS = (
    "agp",
    "buildkonfig",
    "compose",
    "koin",
    "ktor",
    "kotlin rpc",
    "kotlinx rpc",
    "navigation",
    "sqldelight",
    "resources",
    "graphics",
    "network",
    "database",
    "mvi",
)


# Subdirectories where all .md files must be kebab-case
KEBAB_DIRS = ("agents", "commands", "docs", "samples")

# Root-level .md files must be SCREAMING_CASE (all uppercase stem + optional underscores/hyphens)
_SCREAMING_RE = re.compile(r"^[A-Z][A-Z0-9_-]*$")
_KEBAB_RE = re.compile(r"^(\d{4}-\d{2}-\d{2}-)?[a-z][a-z0-9-]*$")


def _check_naming_conventions(root: Path, findings: list[str]) -> None:
    # Root-level .md files must be SCREAMING_CASE
    for f in root.glob("*.md"):
        if not _SCREAMING_RE.match(f.stem):
            findings.append(
                f"naming: root-level {f.name} should be SCREAMING_CASE "
                f"(e.g. {f.stem.upper()}.md)"
            )

    # Subdirectory .md files must be kebab-case
    for subdir_name in KEBAB_DIRS:
        subdir = root / subdir_name
        if not subdir.exists():
            continue
        for f in subdir.rglob("*.md"):
            if not _KEBAB_RE.match(f.stem):
                findings.append(
                    f"naming: {f.relative_to(root)} should be kebab-case "
                    f"(e.g. {f.stem.lower().replace('_', '-')}.md)"
                )


DOCS_MAX_LINES = 150
LESSON_STALE_DAYS = 30
LESSON_BACKLOG_LIMIT = 20

# Non-doc extensions that do not belong directly inside docs/
_NON_DOC_EXTENSIONS = {
    ".json", ".yaml", ".yml", ".xml", ".csv", ".toml",
    ".proto", ".graphql", ".sql", ".sh", ".py", ".kt",
}

# Subdirectory names that are known non-doc homes inside docs/
# (do not flag files inside these — they were intentionally placed)
_KNOWN_NON_DOC_SUBDIRS = {"archive"}

_SNAKE_CASE_RE = re.compile(r"^[a-z][a-z0-9]*(_[a-z0-9]+)+$")


_JVM_ONLY_APIS = (
    "String.format(",
    ".format(",
    "DecimalFormat(",
    "SimpleDateFormat(",
    "java.text.",
    "java.util.Locale",
    "java.util.Date",
    "java.util.Calendar",
)


def _check_commonmain_jvm_apis(root: Path, findings: list[str]) -> None:
    """Flag JVM-only APIs used in commonMain Kotlin source files."""
    for common_main in root.rglob("commonMain"):
        if not common_main.is_dir():
            continue
        for kt in common_main.rglob("*.kt"):
            text = kt.read_text(encoding="utf-8")
            hits = [api for api in _JVM_ONLY_APIS if api in text]
            if hits:
                findings.append(
                    f"jvm-only in commonMain: {kt.relative_to(root)} uses "
                    + ", ".join(f"`{h.rstrip('(')}`" for h in hits)
                    + " — replace with Kotlin string templates or an expect/actual formatter"
                )


def _check_docs_hygiene(root: Path, findings: list[str]) -> None:
    """Flag bloated, stale, or un-archived docs/ files in a consumer project."""
    docs_dir = root / "docs"
    if not docs_dir.exists():
        return

    import datetime

    today = datetime.date.today()

    # 1. Any docs/ file (outside archive/) exceeding the line limit
    for md in docs_dir.rglob("*.md"):
        if "archive" in md.parts:
            continue
        lines = md.read_text(encoding="utf-8").count("\n")
        if lines > DOCS_MAX_LINES:
            findings.append(
                f"docs hygiene: {md.relative_to(root)} is {lines} lines "
                f"(limit {DOCS_MAX_LINES}) — split or archive completed sections"
            )

    # 2. Lessons older than LESSON_STALE_DAYS still in active lessons dir
    lessons_dir = docs_dir / "lessons"
    if lessons_dir.exists():
        stale = []
        for md in sorted(lessons_dir.glob("*.md")):
            # Filename must start with YYYY-MM-DD
            stem = md.stem
            try:
                file_date = datetime.date.fromisoformat(stem[:10])
                age = (today - file_date).days
                if age > LESSON_STALE_DAYS:
                    stale.append((md.relative_to(root), age))
            except ValueError:
                findings.append(
                    f"docs hygiene: {md.relative_to(root)} filename does not start "
                    "with YYYY-MM-DD — rename to match lesson convention"
                )
        for path, age in stale:
            findings.append(
                f"docs hygiene: {path} is {age} days old and not yet harvested "
                "— run kotlin-multiplatform-skill-harvester or archive"
            )

        # 3. Too many unprocessed lessons
        total = sum(1 for _ in lessons_dir.glob("*.md"))
        if total > LESSON_BACKLOG_LIMIT:
            findings.append(
                f"docs hygiene: {total} lesson files in docs/lessons/ "
                f"(limit {LESSON_BACKLOG_LIMIT}) — harvest and archive processed lessons"
            )

    # 4. Task files marked done still in active tasks dir (not archive)
    tasks_dir = docs_dir / "tasks"
    if tasks_dir.exists():
        for md in tasks_dir.glob("*.md"):
            if "archive" in md.parts:
                continue
            text = md.read_text(encoding="utf-8").lower()
            if re.search(r"status:\s*(done|completed|closed)", text):
                findings.append(
                    f"docs hygiene: {md.relative_to(root)} is marked done "
                    "— move to docs/tasks/archive/"
                )

    # 5. Non-markdown files sitting directly in docs/ (flag as non-docs)
    for f in docs_dir.iterdir():
        if f.is_file() and f.suffix in _NON_DOC_EXTENSIONS:
            findings.append(
                f"docs hygiene: {f.relative_to(root)} is a non-doc file in docs/ "
                f"— move to a purpose-specific directory (api/, spec/, tests/fixtures/, etc.)"
            )
        if f.is_dir() and f.name not in _KNOWN_NON_DOC_SUBDIRS:
            # Check for non-doc files one level inside subdirs (e.g. docs/smoke/*.json)
            for sub in f.iterdir():
                if sub.is_file() and sub.suffix in _NON_DOC_EXTENSIONS:
                    findings.append(
                        f"docs hygiene: {sub.relative_to(root)} is a non-doc file inside docs/ "
                        f"— move to tests/fixtures/, api/, or spec/"
                    )

    # 6. Snake_case filenames in docs/ (should be kebab-case)
    for md in docs_dir.rglob("*.md"):
        if "archive" in md.parts:
            continue
        if _SNAKE_CASE_RE.match(md.stem):
            kebab = md.stem.replace("_", "-")
            findings.append(
                f"docs hygiene: {md.relative_to(root)} uses snake_case "
                f"— rename to {kebab}.md"
            )


def audit_skills_repo(root: Path) -> list[str]:
    findings: list[str] = []
    skills_dir = root / "skills"

    if not skills_dir.exists():
        return [f"missing skills directory: {skills_dir}"]

    for skill_dir in sorted(p for p in skills_dir.iterdir() if p.is_dir()):
        skill_file = skill_dir / "SKILL.md"
        if not skill_file.exists():
            findings.append(f"{skill_dir.name}: missing SKILL.md")
            continue

        text = skill_file.read_text(encoding="utf-8")
        missing = [marker for marker in REQUIRED_MARKERS if marker not in text]
        if missing:
            findings.append(f"{skill_dir.name}: missing markers: {', '.join(missing)}")

        if (skill_dir / "references").exists() and "Reference" not in text and "Docs to Recheck First" not in text:
            findings.append(f"{skill_dir.name}: has references/ but no references guidance in SKILL.md")

        if (skill_dir / "scripts").exists() and "Script" not in text and "scripts/" not in text:
            findings.append(f"{skill_dir.name}: has scripts/ but no script guidance in SKILL.md")

        _check_design_system(skill_dir, text, findings)

        if any(hint in text.lower() for hint in FAST_MOVING_HINTS) and re.search(
            r"latest|freshness|recheck",
            text,
            re.IGNORECASE,
        ) is None:
            findings.append(f"{skill_dir.name}: missing freshness guidance for fast-moving dependencies")

        if skill_dir.name == "kotlin-multiplatform-feature-scaffold" and "all-targets" not in text:
            findings.append(
                "kotlin-multiplatform-feature-scaffold: missing all-targets branch guidance for full-stack KMP scaffolds"
            )

        if skill_dir.name == "kotlin-multiplatform-feature-scaffold" and (
            "build-logic" not in text or "libs.versions.toml" not in text
        ):
            findings.append(
                "kotlin-multiplatform-feature-scaffold: missing build-logic and libs.versions.toml guidance"
            )

    _check_naming_conventions(root, findings)
    _check_docs_hygiene(root, findings)
    _check_commonmain_jvm_apis(root, findings)

    readme = root / "README.md"
    if readme.exists():
        readme_text = readme.read_text(encoding="utf-8")
        if "Start here" not in readme_text:
            findings.append("README.md: missing start-here guidance")
        if "Roadmap" not in readme_text:
            findings.append("README.md: missing roadmap section")
    else:
        findings.append("missing README.md")

    return findings


def main() -> int:
    parser = argparse.ArgumentParser(description="Audit the skills repo for documentation hygiene.")
    parser.add_argument("root", type=Path, help="Repo root")
    parser.add_argument(
        "--docs-hygiene-only",
        action="store_true",
        help="Run only the docs/ hygiene checks (line limits, stale lessons, done tasks)",
    )
    parser.add_argument(
        "--jvm-api-only",
        action="store_true",
        help="Scan only for JVM-only APIs in commonMain Kotlin files",
    )
    args = parser.parse_args()

    root = args.root.resolve()
    if args.docs_hygiene_only:
        findings: list[str] = []
        _check_docs_hygiene(root, findings)
    elif args.jvm_api_only:
        findings = []
        _check_commonmain_jvm_apis(root, findings)
    else:
        findings = audit_skills_repo(root)
    for finding in findings:
        print(finding)
    return 1 if findings else 0


if __name__ == "__main__":
    raise SystemExit(main())
