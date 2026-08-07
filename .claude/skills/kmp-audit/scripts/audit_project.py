#!/usr/bin/env python3
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


PATTERNS = [
    ("state copy race", re.compile(r"_state\.value\s*=\s*_state\.value\.copy\(")),
    ("sharedflow replay effect", re.compile(r"MutableSharedFlow<.*replay\s*=\s*1")),
    ("network result in ui", re.compile(r"NetworkResult<")),
    ("data import in ui", re.compile(r"import .*\.data\.")),
    ("manual screen capture", re.compile(
        r"playwright|adb\s+screencap|xcrun\s+simctl\s+io|Robot\(\)\.createScreenCapture|ProcessBuilder.*screenshot",
        re.IGNORECASE,
    )),
    ("magic color literal", re.compile(r"\bColor\(0x[0-9A-Fa-f]")),
    ("named color in ui", re.compile(
        r"\bColor\.(Black|White|Gray|LightGray|DarkGray|Red|Green|Blue|Yellow|Cyan|Magenta)\b"
    )),
    ("hardcoded divider color", re.compile(
        r"\b(HorizontalDivider|VerticalDivider|Divider)\b[^)]*color\s*=\s*Color\b"
    )),
    ("system dark theme scatter", re.compile(r"\bisSystemInDarkTheme\(\)")),
    ("hardcoded spacing", re.compile(r"\bpadding\([^)]*[1-9]\d*\.dp")),
    ("livedata in viewmodel", re.compile(r"MutableLiveData|LiveData<")),
    ("direct state assignment", re.compile(r"_state\.value\s*=")),
    ("globalscope usage", re.compile(r"\bGlobalScope\b")),
    ("navcontroller in viewmodel", re.compile(r"NavController.*ViewModel|ViewModel.*NavController")),
    ("dto leak to domain", re.compile(r"import .*\.dto\.|import .*\.entity\.|@SerialName.*class.*UseCase")),
]


def _at(text: str, pos: int) -> tuple[int, str]:
    """Return (1-based line number, stripped source line) for a match offset.

    Used to attach verifiable evidence (file:line + the matched line) to findings so a
    reviewer can confirm a finding before committing to a refactor.
    """
    line_no = text.count("\n", 0, pos) + 1
    lines = text.splitlines()
    snippet = lines[line_no - 1].strip() if 1 <= line_no <= len(lines) else ""
    return line_no, snippet

# ── Roadmap detection ─────────────────────────────────────────────────────────

def _has(root: Path, *globs: str) -> bool:
    return any(
        not _is_excluded(p, root) for g in globs for p in root.rglob(g)
    )


def _count_files(root: Path, *globs: str) -> int:
    return sum(1 for g in globs for p in root.rglob(g) if not _is_excluded(p, root))


def _read_all(root: Path, *globs: str) -> str:
    parts = []
    for g in globs:
        for p in root.rglob(g):
            if _is_excluded(p, root):
                continue
            try:
                parts.append(p.read_text(encoding="utf-8", errors="ignore"))
            except OSError:
                pass
    return "\n".join(parts)


def _detect_state_mgmt(root: Path) -> str:
    kt = _read_all(root, "*.kt")
    if "MutableStateFlow" in kt and ("sealed interface Intent" in kt or "sealed class Intent" in kt):
        return "MVI (StateFlow + Contract)"
    if "MutableStateFlow" in kt:
        return "StateFlow (no MVI Contract)"
    if "MutableLiveData" in kt:
        return "LiveData (MVVM)"
    if "MutableState" in kt and "remember" in kt:
        return "Compose remember (no ViewModel)"
    return "unknown"


def _detect_modules(root: Path) -> str:
    settings = root / "settings.gradle.kts"
    if not settings.exists():
        settings = root / "settings.gradle"
    if not settings.exists():
        return "single-module (no settings.gradle found)"
    text = settings.read_text(encoding="utf-8", errors="ignore")
    feature_modules = re.findall(r'include\("[^"]*feature[^"]*"\)', text)
    if len(feature_modules) >= 4:
        return f"multi-module ({len(feature_modules)} feature modules)"
    if feature_modules:
        return f"partial split ({len(feature_modules)} feature modules)"
    return "single-module (no :feature: includes)"


def _detect_di(root: Path) -> str:
    kt = _read_all(root, "*.kt")
    if "@KoinViewModel" in kt or "koinViewModel()" in kt:
        return "Koin 4 (annotated)"
    if "koinInject" in kt or "val module = module {" in kt:
        return "Koin (manual)"
    if "@HiltViewModel" in kt or "@AndroidEntryPoint" in kt:
        return "Hilt"
    if "@Inject" in kt:
        return "Dagger / manual inject"
    return "none detected"


def _detect_tests(root: Path) -> str:
    test_files = _count_files(root, "*Test.kt", "*Spec.kt")
    if test_files == 0:
        return "none"
    if test_files < 5:
        return f"minimal ({test_files} test files)"
    return f"present ({test_files} test files)"


def _detect_detekt(root: Path) -> str:
    if _has(root, "detekt.yml", "detekt.yaml", "detekt-config.yml"):
        return "configured"
    return "missing"


def _detect_version_catalog(root: Path) -> str:
    if _has(root, "libs.versions.toml"):
        return "present"
    return "missing"


def assess_project(root: Path) -> dict:
    vm_info = _detect_viewmodel_size(root)
    return {
        "state_mgmt":       _detect_state_mgmt(root),
        "modules":          _detect_modules(root),
        "feature_split":    _detect_feature_split(root),
        "di":               _detect_di(root),
        "tests":            _detect_tests(root),
        "detekt":           _detect_detekt(root),
        "version_catalog":  _detect_version_catalog(root),
        "viewmodel_max_lines": vm_info["max_lines"],
        "large_vms":        vm_info["large_vms"],
    }


ADOPTION_PLAN = [
    # (condition_fn, priority, skill, reason, action)
    (
        lambda s: s["detekt"] == "missing",
        "HIGH",
        "kmp-code-quality",
        "No Detekt gates — new violations accumulate faster than you migrate them",
        "Add detekt.yml with layer rules before touching any architecture code",
    ),
    (
        lambda s: s["version_catalog"] == "missing",
        "HIGH",
        "kmp-feature-scaffold",
        "No version catalog — dependency versions drift across modules",
        "Add gradle/libs.versions.toml and migrate build files to use it",
    ),
    (
        lambda s: "LiveData" in s["state_mgmt"],
        "HIGH",
        "kmp-mvi",
        "LiveData detected — migrate to StateFlow+MVI screen by screen",
        "Pick the highest-traffic screen, write tests for it, then migrate to StateFlow (Path A Step 1)",
    ),
    (
        lambda s: s["state_mgmt"] == "StateFlow (no MVI Contract)",
        "MEDIUM",
        "kmp-mvi",
        "StateFlow present but no MVI Contract — effects may be using SharedFlow or callbacks",
        "Add Contract (State/Intent/Effect) to screens that have navigation side-effects",
    ),
    (
        lambda s: "single-module" in s["modules"],
        "MEDIUM",
        "kmp-clean-architecture",
        "Single module — no layer isolation; UI can import data layer directly",
        "Extract :model first (zero-logic move), then :api, then :domain (see migration Path B)",
    ),
    (
        lambda s: "partial split" in s["modules"],
        "MEDIUM",
        "kmp-clean-architecture",
        "Partial module split — some features separated, others still monolithic",
        "Complete the split for the highest-churn feature first",
    ),
    (
        lambda s: s["tests"] == "none",
        "MEDIUM",
        "kmp-unit-testing",
        "No tests — migrating without tests risks invisible regressions",
        "Add ViewModel tests (with FakeRepository) before migrating each screen",
    ),
    (
        lambda s: "Hilt" in s["di"] or "Dagger" in s["di"],
        "LOW",
        "kmp-dependency-injection",
        "Hilt/Dagger detected — not compatible with KMP non-Android targets",
        "Migrate one @Module at a time to Koin 4 (Path C); Hilt and Koin can coexist during migration",
    ),
    (
        lambda s: s["tests"] == "minimal",
        "LOW",
        "kmp-unit-testing",
        "Few tests — coverage is too thin to migrate safely at speed",
        "Add tests for every ViewModel being migrated before the migration PR",
    ),
    (
        lambda s: s["viewmodel_max_lines"] >= 300,
        "HIGH",
        "kmp-mvi",
        "God ViewModel detected (300+ lines) — business logic has leaked into the ViewModel",
        "Extract business operations into use cases (see 'ViewModel Size and Decomposition' in mvi skill); "
        "each handleIntent branch that touches 2+ repos belongs in a use case",
    ),
    (
        lambda s: 150 <= s["viewmodel_max_lines"] < 300,
        "MEDIUM",
        "kmp-mvi",
        "Large ViewModel detected (150–299 lines) — growing toward monolithic",
        "Review handleIntent branches for inline logic that can be extracted to use cases before size crosses 300 lines",
    ),
    (
        lambda s: "no feature layer split" in s["feature_split"] and "multi-module" in s["modules"],
        "HIGH",
        "kmp-clean-architecture",
        "Multi-module project but features have no :presenter / :domain / :ui layer split",
        "Apply the start-thin tier decision: each feature needs at least :ui; add :presenter when "
        "the screen has its own ViewModel; add :domain when use cases are shared or complex",
    ),
    (
        lambda s: "thin split" in s["feature_split"],
        "MEDIUM",
        "kmp-clean-architecture",
        "Features have :ui modules only — no :presenter separation",
        "Promote features with complex ViewModels to medium tier (:presenter + :ui); "
        "reserve full tier for CRUD / offline-first features",
    ),
]


def build_roadmap(state: dict) -> list[dict]:
    plan = []
    for condition, priority, skill, reason, action in ADOPTION_PLAN:
        if condition(state):
            plan.append({
                "priority": priority,
                "skill":    skill,
                "reason":   reason,
                "action":   action,
            })
    plan.sort(key=lambda x: {"HIGH": 0, "MEDIUM": 1, "LOW": 2}[x["priority"]])
    return plan


def print_roadmap(root: Path, state: dict, plan: list[dict]) -> None:
    print(f"\n{'='*60}")
    print("  KMP ADOPTION ROADMAP")
    print(f"  Project: {root}")
    print(f"{'='*60}\n")

    print("Current state:")
    print(f"  State management : {state['state_mgmt']}")
    print(f"  Module structure : {state['modules']}")
    print(f"  Feature split    : {state['feature_split']}")
    print(f"  DI               : {state['di']}")
    print(f"  Tests            : {state['tests']}")
    print(f"  Detekt           : {state['detekt']}")
    print(f"  Version catalog  : {state['version_catalog']}")
    vm_max = state["viewmodel_max_lines"]
    if vm_max > 0:
        vm_label = "god ViewModel (300+)" if vm_max >= 300 else "large (150–299)"
        print(f"  Largest ViewModel: {vm_max} lines ({vm_label})")
        if state["large_vms"]:
            top = state["large_vms"][:3]
            for rel, n in top:
                print(f"    - {rel} ({n} lines)")
    else:
        print(f"  Largest ViewModel: not detected")
    print()

    if not plan:
        print("No adoption gaps detected. Project appears well-structured.")
        print("Run without --roadmap to check for implementation violations.\n")
        return

    print(f"Adoption plan ({len(plan)} items):\n")
    for i, item in enumerate(plan, 1):
        print(f"  {i}. [{item['priority']}] {item['skill']}")
        print(f"     Why:    {item['reason']}")
        print(f"     Action: {item['action']}")
        print()

    print("Run audit_project.py without --roadmap to check for implementation violations.")
    print()


# ── Agent & consumer setup checks ────────────────────────────────────────────

def _detect_agent_setup(root: Path) -> list[str]:
    # Only meaningful for real Gradle projects; skip bare temp dirs used in unit tests.
    is_gradle_project = (root / "settings.gradle.kts").exists() or (root / "settings.gradle").exists()
    if not is_gradle_project:
        return []

    findings: list[str] = []
    claude = root / ".claude"

    if not (root / "CLAUDE.md").exists():
        findings.append("agent-setup [HIGH]: CLAUDE.md missing — skills context never loads (run /kmp-setup-agents)")

    if not (claude / "AGENTS.md").exists():
        findings.append("agent-setup [HIGH]: .claude/AGENTS.md missing — no skill routing table (run /kmp-setup-agents)")

    commands_dir = claude / "commands"
    if not commands_dir.exists() or not any(commands_dir.iterdir()):
        findings.append("agent-setup [MEDIUM]: .claude/commands/ missing — consumer commands not installed")

    skills_dir = claude / "skills"
    if not skills_dir.exists() or not any(skills_dir.iterdir()):
        findings.append("agent-setup [MEDIUM]: .claude/skills/ missing or empty — skills not deployed")

    has_claude_setup = (root / "CLAUDE.md").exists() or claude.exists()

    agents_skills_dir = root / ".agents" / "skills"
    if has_claude_setup:
        if not agents_skills_dir.exists() or not any(agents_skills_dir.iterdir()):
            findings.append(
                "agent-setup [MEDIUM]: .agents/skills/ missing or empty — the "
                "agentskills.io cross-client target isn't deployed; other clients "
                "(Cursor, Amp, Goose, ...) working in this project see no skills "
                "(run /kmp-setup-agents or update-consumer-skills.sh)"
            )
        elif skills_dir.exists() and any(skills_dir.iterdir()):
            claude_names = {p.name for p in skills_dir.iterdir() if p.is_dir()}
            agents_names = {p.name for p in agents_skills_dir.iterdir() if p.is_dir()}
            if claude_names != agents_names:
                only_claude = sorted(claude_names - agents_names)
                only_agents = sorted(agents_names - claude_names)
                detail = []
                if only_claude:
                    detail.append(f"only in .claude/skills/: {only_claude}")
                if only_agents:
                    detail.append(f"only in .agents/skills/: {only_agents}")
                findings.append(
                    "agent-setup [MEDIUM]: .claude/skills/ and .agents/skills/ have "
                    "drifted — " + "; ".join(detail) + " (re-run the deploy step so "
                    "both copies match)"
                )

    project_skills_dir = root / "skills"
    if project_skills_dir.is_dir():
        bundled_named = sorted(
            p.name for p in project_skills_dir.iterdir()
            if p.is_dir() and (p.name.startswith("kmp-") or p.name.startswith("jni-"))
        )
        if bundled_named:
            findings.append(
                "agent-setup [HIGH]: bundled-looking skill name(s) under project-root "
                f"skills/ — {bundled_named}; project-root skills/ is for project-owned "
                "CUSTOM skills only, bundled kmp-agent-skills content belongs in "
                ".agents/skills/ and .claude/skills/, never copied into the source tree"
            )
    source_layout = {
        "agents/": root / "agents",
        "rules/": root / "rules",
        "hooks/": root / "hooks",
        "commands/": root / "commands",
        "skills/": root / "skills",
        "docs/reference/ai-collaboration.md": root / "docs" / "reference" / "ai-collaboration.md",
        "docs/reference/agent-catalog.md": root / "docs" / "reference" / "agent-catalog.md",
    }
    missing_source_layout = [label for label, path in source_layout.items() if not path.exists()]
    if has_claude_setup and missing_source_layout:
        findings.append(
            "agent-setup [MEDIUM]: project-owned agent scaffold incomplete — missing "
            + ", ".join(missing_source_layout)
            + "; keep project-specific agent sources at the repo root and `.claude/` as the deployed runtime"
        )

    # Multi-surface project: AGENTS.md exists but only mentions one surface
    agents_md = claude / "AGENTS.md"
    if agents_md.exists():
        text = agents_md.read_text(encoding="utf-8", errors="ignore")
        settings = root / "settings.gradle.kts"
        if settings.exists():
            s = settings.read_text(encoding="utf-8", errors="ignore")
            has_studio = "studio" in s or "shared" in s
            has_core   = ":core:" in s or ":native" in s
            if has_studio and has_core:
                mentions_studio = "studio" in text.lower() or "shared" in text.lower()
                mentions_core   = "core" in text.lower() or "native" in text.lower()
                if not (mentions_studio and mentions_core):
                    findings.append(
                        "agent-setup [MEDIUM]: AGENTS.md covers only one surface of a multi-surface project "
                        "— add routing for the missing surface"
                    )

    return findings


def _detect_mvi_placement(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("MviViewModel.kt"):
        if _is_excluded(path, root):
            continue
        rel = path.relative_to(root).as_posix()
        # Flag if it's inside a feature module, not in shared/core
        if any(seg in rel for seg in ("feature", "studio", "app")) and not any(
            seg in rel for seg in ("shared/core", "core/mvi", "core/common", ":core:mvi")
        ):
            findings.append(
                f"arch [MEDIUM]: MviViewModel base class in feature module ({rel}) "
                f"— move to :shared:core or :core:mvi so all features can extend it"
            )
    return findings


def _detect_design_system_wiring(root: Path) -> list[str]:
    findings: list[str] = []
    theme_pattern    = re.compile(r"MaterialTheme\s*\(")
    dark_hardcoded   = re.compile(r"darkTheme\s*=\s*false")
    token_file_pattern = re.compile(r"(ULong|Long)\s*=\s*0x[0-9A-Fa-f]{6,}")

    token_files: list[Path] = []
    for path in root.rglob("*Tokens.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if token_file_pattern.search(text):
            token_files.append(path)

    if len(token_files) >= 2:
        rel_paths = ", ".join(str(p.relative_to(root)) for p in token_files[:3])
        findings.append(
            f"design-system [LOW]: multiple parallel token files with raw ULong constants "
            f"({rel_paths}) — consolidate under a single AppColors data class"
        )

    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        if not any(part in path.stem for part in ("Theme", "theme")):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        rel = path.relative_to(root)
        if theme_pattern.search(text):
            findings.append(
                f"design-system [MEDIUM]: {rel} wraps MaterialTheme — "
                f"blocks custom token ownership; use CompositionLocalProvider + AppTheme"
            )
        if dark_hardcoded.search(text):
            findings.append(
                f"design-system [MEDIUM]: {rel} hardcodes darkTheme=false — "
                f"replace with isSystemInDarkTheme() default"
            )

    return findings


# ── Standard audit ────────────────────────────────────────────────────────────

# A class that extends a ViewModel base (ViewModel, MviViewModel, BaseViewModel, …).
# Filename-independent: catches *Presenter.kt, coordinators, *VM.kt, etc.
_CLASS_DECL_RE = re.compile(r"\bclass\s+(\w+)")


def _class_header_end(text: str, name_end: int) -> int:
    """Return the index right after a class's `<T>` generic params, optional
    constructor annotation/`constructor` keyword, and primary constructor —
    i.e. where a supertype clause (`: Foo`) would start if the class has one.

    Skips the primary constructor by balancing parens (`_find_matching_paren`)
    rather than stopping at the first `)`, so a lambda-typed param like
    `val onChange: (CueSettings) -> Unit` — which has its own parens — doesn't
    get mistaken for the constructor's own close.
    """
    i, n = name_end, len(text)
    while i < n and text[i].isspace():
        i += 1
    if i < n and text[i] == "<":
        close = text.find(">", i)
        i = close + 1 if close != -1 else i
        while i < n and text[i].isspace():
            i += 1
    ann = re.match(r"@\w+(?:\([^)]*\))?\s*(?:constructor\s*)?", text[i:])
    if ann:
        i += ann.end()
    if i < n and text[i] == "(":
        close = _find_matching_paren(text, i)
        i = close + 1 if close is not None else i
    return i


def _class_supertype_and_body(text: str, name_end: int) -> tuple[str, int | None, int | None]:
    """Return (supertype_clause, body_start, body_end) for one `class Name` match.

    supertype_clause is '' when the class has no `: Foo` clause at all — this is
    positional (found right after the real constructor closes), so a comment or
    an unrelated property's type-annotation colon elsewhere in the file can never
    be mistaken for it. body_start/body_end (brace indices, via
    `_find_matching_brace`) are None for a class with no body (e.g. a one-line
    data class constructor with nothing after it).
    """
    i = _class_header_end(text, name_end)
    n = len(text)
    while i < n and text[i].isspace():
        i += 1
    supertype = ""
    if i < n and text[i] == ":":
        j = i + 1
        brace_idx = text.find("{", j)
        end = brace_idx if brace_idx != -1 else min(n, j + 300)
        supertype = text[j:end]
        i = brace_idx if brace_idx != -1 else end

    j = i
    while j < n and text[j] not in "{\n":
        j += 1
    body_start = body_end = None
    if j < n and text[j] == "{":
        body_start = j
        body_end = _find_matching_brace(text, j)

    return supertype, body_start, body_end


def _is_viewmodel_file(text: str) -> bool:
    """True if the file defines a ViewModel — by supertype or viewModelScope usage —
    regardless of filename. Hardens detectors against alternate naming conventions."""
    if "viewModelScope" in text:
        return True
    for m in _CLASS_DECL_RE.finditer(text):
        supertype, _, _ = _class_supertype_and_body(text, m.end())
        # Substring, not \bViewModel\b — the base is usually MviViewModel/BaseViewModel,
        # so "ViewModel" never starts on a word boundary.
        if "ViewModel" in supertype:
            return True
    return False


def _detect_viewmodel_size(root: Path) -> dict:
    """Return max line count and list of oversized ViewModel files.

    Detects ViewModels by content (supertype / viewModelScope) so files that do not
    follow the *ViewModel.kt naming convention are still measured.
    """
    large: list[tuple[Path, int]] = []
    seen: set[Path] = set()
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or path in seen:
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        # Filename convention OR content signal
        if not (path.stem.endswith("ViewModel") or _is_viewmodel_file(text)):
            continue
        seen.add(path)
        count = len(text.splitlines())
        if count >= 150:
            large.append((path, count))
    large.sort(key=lambda x: x[1], reverse=True)
    return {
        "max_lines": large[0][1] if large else 0,
        "large_vms": [(str(p.relative_to(root)), n) for p, n in large],
    }


def _detect_feature_split(root: Path) -> str:
    """Detect whether features follow the layer split convention."""
    settings = root / "settings.gradle.kts"
    if not settings.exists():
        settings = root / "settings.gradle"
    if not settings.exists():
        return "unknown (no settings.gradle)"
    text = settings.read_text(encoding="utf-8", errors="ignore")
    presenter = re.findall(r'include\("[^"]*:presenter[^"]*"\)', text)
    domain    = re.findall(r'include\("[^"]*:domain[^"]*"\)', text)
    ui        = re.findall(r'include\("[^"]*:ui[^"]*"\)', text)
    if presenter and domain and ui:
        return f"full split (presenter={len(presenter)}, domain={len(domain)}, ui={len(ui)})"
    if ui and not presenter:
        return f"thin split (:ui only, {len(ui)} modules — no :presenter or :domain)"
    if presenter and not domain:
        return f"medium split (:presenter+:ui, no :domain)"
    return "no feature layer split detected"


_MULTI_VM_RE = re.compile(r'\bkoinViewModel\s*[<(]')
_LAUNCHED_EFFECT_RE = re.compile(r'\bLaunchedEffect\s*\(')
_EFFECT_COLLECT_RE = re.compile(r'\.effect\s*\.\s*collect\b')


def _detect_multi_viewmodel_screen(root: Path) -> list[str]:
    """Flag composables that instantiate 3+ ViewModels directly.

    Each koinViewModel() in one composable creates tight coupling and makes it
    untestable in isolation. Detected on any Compose file (by content), not just
    *Screen.kt, so non-convention names (Dashboard, Hub, Home) are still caught.
    The fix: split each feature into its own screen, sharing data via a repository.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue
        count = len(_MULTI_VM_RE.findall(text))
        if count >= 3:
            line_no, snippet = _at(text, _MULTI_VM_RE.search(text).start())
            findings.append(
                f"multi viewmodel screen [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— {count} koinViewModel() calls; split each feature into its own screen "
                f"behind a NavHost (one ViewModel per screen), sharing data via a repository "
                f"(see MVI skill → feature orchestration decision order)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# Matches a @Composable function whose signature has a param typed `*ViewModel`
# that does NOT have a `= koinViewModel()` default (i.e. the VM is forced in by the caller).
_COMPOSABLE_FUN_RE = re.compile(
    r"@Composable\s+(?:private\s+|internal\s+|public\s+)?fun\s+(\w+)\s*\((?P<params>.*?)\)",
    re.DOTALL,
)
_VM_PARAM_NO_DEFAULT_RE = re.compile(
    r"\b(\w+)\s*:\s*\w*ViewModel\b(?P<after>[^,)]*)"
)


def _detect_viewmodel_as_composable_param(root: Path) -> list[str]:
    """Flag @Composable functions that receive a ViewModel as a forced parameter.

    A screen should obtain its ViewModel via `vm: FooViewModel = koinViewModel()` (a
    defaulted param) — never as a required parameter passed down by a parent. A required
    `*ViewModel` param means the parent is constructing/owning child ViewModels and threading
    them down: the god-composable shape. The fix is the same decision order: separate screens
    with their own koinViewModel(), or hoist state (not the VM) into the parent.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for m in _COMPOSABLE_FUN_RE.finditer(text):
            params = m.group("params")
            for pm in _VM_PARAM_NO_DEFAULT_RE.finditer(params):
                after = pm.group("after")
                # Skip if this param has a koinViewModel()/viewModel() default
                if "koinViewModel" in after or "= viewModel" in after:
                    continue
                line_no, snippet = _at(text, m.start())
                findings.append(
                    f"viewmodel as composable param [MEDIUM]: {path.relative_to(root)}:{line_no} "
                    f"— @Composable {m.group(1)}(...) takes '{pm.group(1)}: *ViewModel' as a "
                    f"required param; use 'vm: FooViewModel = koinViewModel()' instead, or split "
                    f"into separate screens (see MVI skill → feature orchestration decision order)\n"
                    f"    {line_no} | {snippet}"
                )
                break  # one finding per composable
    return findings


# A ViewModel held as a property (by inject, by viewModels, type annotation) inside a VM.
_VM_PARAM_RE = re.compile(r":\s*\w*ViewModel\b")
_VM_PROPERTY_RE = re.compile(r"\b(?:val|var)\s+\w+\s*:\s*\w*ViewModel\b")
# A ViewModel instantiated directly: `= FooViewModel(`
_VM_INSTANTIATE_RE = re.compile(r"=\s*\w*ViewModel\s*\(")
# A ViewModel obtained via a DI generic: inject<FooViewModel>() / koinInject<FooViewModel>()
_VM_INJECT_GENERIC_RE = re.compile(r"\b(?:inject|koinInject|koinViewModel)\s*<\s*\w*ViewModel\b")


def _detect_viewmodel_in_viewmodel(root: Path) -> list[str]:
    """Flag a ViewModel that depends on another ViewModel — by constructor param,
    injected property, internal instantiation, or DI generic.

    ViewModels are created by ViewModelProvider/factory with their own viewModelScope,
    SavedStateHandle, and CreationExtras — nesting them breaks lifecycle ownership and
    DI. The fix: demote the injected sub-unit to a State Holder (plain class taking a
    CoroutineScope) or a use case. Scans all .kt files so coordinators that don't follow
    the *ViewModel.kt naming convention are still caught.

    Every check below is scoped to one class's own constructor/body span (via
    `_class_supertype_and_body`) — never the whole file — so a local `val` inside an
    unrelated `@Composable` function, or a comment mentioning "ViewModel", can't be
    mistaken for a property of the ViewModel class itself.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue

        # The file must define a ViewModel for any of these to be the anti-pattern.
        if not _is_viewmodel_file(text):
            continue

        for m in _CLASS_DECL_RE.finditer(text):
            supertype, body_start, body_end = _class_supertype_and_body(text, m.end())
            body_end = body_end if body_end is not None else len(text)
            is_vm_class = "ViewModel" in supertype or (
                body_start is not None and "viewModelScope" in text[body_start:body_end]
            )
            if not is_vm_class:
                continue

            vm_name = m.group(1)
            how: str | None = None
            pos = 0

            # 1) Constructor param typed *ViewModel (scoped to this class's own ctor)
            ctor_text = text[m.end():_class_header_end(text, m.end())]
            pm = _VM_PARAM_RE.search(ctor_text)
            if pm:
                how = "a constructor param"
                pos = m.end() + pm.start()

            # 2) Property / instantiation / DI-generic — scoped to this class's own body
            if how is None and body_start is not None:
                body_text = text[body_start:body_end]
                for rx, desc in (
                    (_VM_PROPERTY_RE, "an injected/declared property"),
                    (_VM_INSTANTIATE_RE, "a directly instantiated property"),
                    (_VM_INJECT_GENERIC_RE, "a DI lookup (inject<…ViewModel>())"),
                ):
                    mm = rx.search(body_text)
                    if mm:
                        how, pos = desc, body_start + mm.start()
                        break

            if how is not None:
                line_no, snippet = _at(text, pos)
                findings.append(
                    f"viewmodel in viewmodel [HIGH]: {path.relative_to(root)}:{line_no} "
                    f"— {vm_name} depends on another ViewModel via {how}; "
                    f"demote it to a State Holder (plain class + injected CoroutineScope) "
                    f"or a use case (see MVI skill → Coordinator ViewModel)\n"
                    f"    {line_no} | {snippet}"
                )
    return findings


# A repository injected inside a @Composable — suffix-matched (`*Repository`), same
# convention as this file's other type-suffix checks. Injecting it and forwarding it
# to a real ViewModel/state-holder/controller constructor is fine; the violation
# (checked separately below) is the composable itself reading the repository's Flow.
_COMPOSABLE_FUN_START_RE = re.compile(
    r"@Composable\s+(?:private\s+|internal\s+|public\s+)?fun\s+(\w+)\s*\("
)
_REPO_INJECT_RE = re.compile(
    r"\b(?:val|var)\s+(\w+)\s*:\s*\w*Repository\b\s*=\s*koinInject\s*(?:<[^>]*>)?\s*\("
)


def _composable_body_span(text: str, open_paren_idx: int) -> tuple[int | None, int | None]:
    """Given the index of a composable function's opening '(', balance the
    parameter list (so a lambda-typed param's own parens don't confuse the scan),
    skip an optional `: ReturnType`, and return the function body's brace span."""
    close_paren = _find_matching_paren(text, open_paren_idx)
    if close_paren is None:
        return None, None
    brace_idx = text.find("{", close_paren + 1)
    if brace_idx == -1:
        return None, None
    return brace_idx, _find_matching_brace(text, brace_idx)


def _detect_repository_in_composable(root: Path) -> list[str]:
    """Flag a repository injected directly inside a @Composable function and then
    Flow-collected there — the UI layer reading the data layer with nothing in
    between. A ViewModel (or a use case it calls) should own that read; the
    composable should only ever see already-processed UI state.

    Known gaps, by design — a textual heuristic, not a compiler:
      - misses indirection through an intermediate local val
        (`val flow = repo.settings; val s by flow.collectAsState()`)
      - only matches the `*Repository` suffix convention, not a raw client/DAO
        doing the same thing (widening that net raises false positives, since
        some client types are legitimately UI-safe)
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue

        for fm in _COMPOSABLE_FUN_START_RE.finditer(text):
            body_start, body_end = _composable_body_span(text, fm.end() - 1)
            if body_start is None:
                continue
            body_text = text[body_start:body_end if body_end is not None else len(text)]

            for rm in _REPO_INJECT_RE.finditer(body_text):
                repo_name = rm.group(1)
                collect_re = re.compile(
                    rf"\b{re.escape(repo_name)}\.\w+\.collect(?:AsState)?\s*\("
                )
                cm = collect_re.search(body_text)
                if cm:
                    pos = body_start + cm.start()
                    line_no, snippet = _at(text, pos)
                    findings.append(
                        f"repository in composable [MEDIUM]: {path.relative_to(root)}:{line_no} "
                        f"— '{repo_name}' (a Repository) is injected and its Flow collected "
                        f"directly inside '{fm.group(1)}', bypassing ViewModel ownership; "
                        f"move this read into a ViewModel (or a use case it calls) and expose "
                        f"already-processed UI state instead (see kmp-repository-pattern)\n"
                        f"    {line_no} | {snippet}"
                    )
    return findings


def _detect_god_composable(root: Path) -> list[str]:
    """Flag Screen/Content composables that orchestrate too much side-effect logic.

    Signals of a 'god composable' — orchestration that belongs in a coordinator
    ViewModel but leaked into the UI layer:
      - 5+ LaunchedEffect blocks (effect collection / persistence / restore in UI), OR
      - 3+ .effect.collect calls (the composable is acting as a VM-to-VM message bus)

    The fix is a coordinator ViewModel: move state assembly, effect collection,
    and persistence into viewModelScope so the screen shrinks to state + onIntent.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        # A screen by name, or any Compose file (catches non-convention composables)
        if not (any(path.stem.endswith(part) for part in _SCREEN_STEMS) or _is_compose_ui_file(text, path)):
            continue
        le_count = len(_LAUNCHED_EFFECT_RE.findall(text))
        collect_count = len(_EFFECT_COLLECT_RE.findall(text))
        if le_count >= 5 or collect_count >= 3:
            severity = "HIGH" if (le_count >= 8 or collect_count >= 5) else "MEDIUM"
            anchor = _LAUNCHED_EFFECT_RE.search(text) or _EFFECT_COLLECT_RE.search(text)
            line_no, snippet = _at(text, anchor.start())
            findings.append(
                f"god composable [{severity}]: {path.relative_to(root)}:{line_no} "
                f"— {le_count} LaunchedEffect blocks, {collect_count} effect.collect calls; "
                f"split features into separate screens behind a NavHost (sharing data via a "
                f"repository), or if they must share one screen, move state assembly + effect "
                f"collection + persistence into viewModelScope (see MVI skill decision order)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── God class (repo-wide, not scoped to ViewModel/Composable) ──────────────────
# god-object detection existed for exactly two file types — ViewModel size and
# god composable — nothing caught a repository, use case, or manager class
# accumulating too many responsibilities. This is the heuristic backstop for a
# project that hasn't wired kmp-code-quality's LargeClass/
# TooManyFunctions Detekt rules yet; those AST-based rules are the precise version
# of this same check.

_PLAIN_CLASS_DECL_RE = re.compile(
    r"(?m)^(?!.*\b(?:data|sealed|enum|value|annotation)\s+class\b).*\bclass\s+(\w+)"
)
_FUN_DECL_RE = re.compile(r"\bfun\s+\w+\s*\(")
_GOD_CLASS_LINE_THRESHOLD = 400
_GOD_CLASS_FUN_THRESHOLD = 15


def _detect_god_class(root: Path) -> list[str]:
    """Flag a plain class (not data/sealed/enum/value/annotation) that's grown past
    both a line-count and a function-count threshold — a repository, use case, or
    manager accumulating too many responsibilities. Skips files already covered by
    the ViewModel-size and god-composable detectors to avoid double-reporting.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if path.stem.endswith("ViewModel") or _is_viewmodel_file(text):
            continue
        if _is_compose_ui_file(text, path):
            continue
        class_match = _PLAIN_CLASS_DECL_RE.search(text)
        if not class_match:
            continue
        line_count = len(text.splitlines())
        fun_count = len(_FUN_DECL_RE.findall(text))
        if line_count < _GOD_CLASS_LINE_THRESHOLD or fun_count < _GOD_CLASS_FUN_THRESHOLD:
            continue
        severity = "HIGH" if (line_count >= 700 or fun_count >= 25) else "MEDIUM"
        line_no, snippet = _at(text, class_match.start())
        findings.append(
            f"god class [{severity}]: {path.relative_to(root)}:{line_no} "
            f"— {line_count} lines, {fun_count} functions in '{class_match.group(1)}'; "
            f"split into smaller, single-responsibility classes. Per "
            f"kmp-code-quality's LargeClass/TooManyFunctions Detekt "
            f"rules, this should already fail CI once configured\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── runBlocking in shared (commonMain) code ─────────────────────────────────────
# runBlocking blocks the calling thread until the coroutine completes — on Android/iOS
# that's very often the main thread. Fine in a JVM/Desktop CLI entry point (fun main),
# a real correctness hazard (ANR / deadlock risk) anywhere else in shared business logic.

_RUNBLOCKING_RE = re.compile(r"\brunBlocking\s*[{(]")
_FUN_MAIN_RE = re.compile(r"\bfun\s+main\s*\(")


def _detect_runblocking_in_shared_code(root: Path) -> list[str]:
    """Flag runBlocking used in commonMain outside a fun main() entry point."""
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        if "/commonmain/" not in path.as_posix().lower():
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if _FUN_MAIN_RE.search(text):
            continue
        match = _RUNBLOCKING_RE.search(text)
        if not match:
            continue
        line_no, snippet = _at(text, match.start())
        findings.append(
            f"runBlocking in shared code [MEDIUM]: {path.relative_to(root)}:{line_no} "
            f"— blocks the calling thread, often the main thread on Android/iOS; use a "
            f"suspend function and let the caller's coroutine scope handle it instead\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── Koin circular dependency ─────────────────────────────────────────────────────
# Only detects bindings that use an explicit single<Type>/factory<Type>/scoped<Type>
# declaration and explicit get<Type>() calls within the same line — this collection's
# own kmp-dependency-injection skill already recommends explicit
# typing for interface bindings. A plain single { Foo(get(), get()) } with no explicit
# type arguments can't be resolved to a dependency graph without also parsing Foo's
# constructor signature elsewhere, which this heuristic doesn't attempt — real gap,
# but narrowing scope to explicitly-typed bindings keeps false positives near zero.

_KOIN_TYPED_BINDING_RE = re.compile(
    r"\b(?:single|factory|scoped)\s*<\s*(\w+)\s*>\s*\{([^}]*)\}"
)
_KOIN_GET_TYPED_RE = re.compile(r"\bget\s*<\s*(\w+)\s*>\s*\(")


def _detect_koin_circular_dependency(root: Path) -> list[str]:
    """Flag a cycle among explicitly-typed Koin bindings (single<A>/factory<A>/
    scoped<A> referencing get<B>() where B eventually depends back on A).
    """
    graph: dict[str, set[str]] = {}
    origin: dict[str, tuple[Path, int]] = {}
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if "module {" not in text and "module{" not in text:
            continue
        for match in _KOIN_TYPED_BINDING_RE.finditer(text):
            bound_type, body = match.group(1), match.group(2)
            deps = set(_KOIN_GET_TYPED_RE.findall(body)) - {bound_type}
            graph.setdefault(bound_type, set()).update(deps)
            if bound_type not in origin:
                origin[bound_type] = (path, _at(text, match.start())[0])

    findings: list[str] = []
    reported: set[frozenset] = set()

    def find_cycle(start: str) -> list[str] | None:
        stack = [(start, [start])]
        seen_paths: set[str] = set()
        while stack:
            node, path_so_far = stack.pop()
            for neighbor in graph.get(node, ()):
                if neighbor == start:
                    return path_so_far + [start]
                if neighbor in path_so_far or neighbor in seen_paths:
                    continue
                seen_paths.add(neighbor)
                stack.append((neighbor, path_so_far + [neighbor]))
        return None

    for bound_type in graph:
        cycle = find_cycle(bound_type)
        if not cycle:
            continue
        key = frozenset(cycle)
        if key in reported:
            continue
        reported.add(key)
        path, line_no = origin[bound_type]
        findings.append(
            f"koin circular dependency [HIGH]: {path.relative_to(root)}:{line_no} "
            f"— {' → '.join(cycle)}; break the cycle by extracting the shared piece "
            f"into a third binding both sides depend on, or by injecting a Provider/"
            f"lazy indirection at one edge\n"
            f"    {line_no} | {bound_type} binding"
        )
    return findings


# ── Compose unstable collection parameter ─────────────────────────────────────
# Raw List<T>/Map<K,V>/Set<T> parameters on a @Composable are treated as unstable by
# the Compose compiler (they're mutable-capable interfaces), forcing recomposition on
# every parent recompose even when the contents haven't changed. kotlinx.collections
# .immutable's ImmutableList/ImmutableMap/ImmutableSet (or a wrapping @Immutable data
# class) fix this. Heuristic-only nudge — a raw collection param is common and often
# fine for a leaf composable that recomposes cheaply; this is a LOW-severity signal for
# a composable worth checking, not a claim it's definitely wrong.

_UNSTABLE_COLLECTION_PARAM_RE = re.compile(r"\b(?:List|Map|Set)\s*<")
_IMMUTABLE_COLLECTION_PREFIX_RE = re.compile(r"\b(?:Immutable|Persistent)(?:List|Map|Set)\s*<")


def _detect_compose_unstable_collection_param(root: Path) -> list[str]:
    """Flag a @Composable function with a raw List/Map/Set parameter — Compose treats
    these as unstable, causing unnecessary recomposition.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue
        for match in _COMPOSABLE_FUN_RE.finditer(text):
            fn_name = match.group(1)
            params = _split_top_level(match.group("params"))
            unstable = [
                p.strip()
                for p in params
                if _UNSTABLE_COLLECTION_PARAM_RE.search(p)
                and not _IMMUTABLE_COLLECTION_PREFIX_RE.search(p)
            ]
            if not unstable:
                continue
            line_no, snippet = _at(text, match.start())
            findings.append(
                f"compose unstable collection param [LOW]: {path.relative_to(root)}:{line_no} "
                f"— '{fn_name}' takes a raw List/Map/Set parameter ({'; '.join(unstable)}); "
                f"Compose treats these as unstable, forcing recomposition even when "
                f"contents are unchanged. Use kotlinx.collections.immutable's "
                f"ImmutableList/ImmutableMap/ImmutableSet instead — note the library is "
                f"still Alpha (API subject to change), so pin its version deliberately "
                f"and don't expose it across a library's own public API surface\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Library project structure (kmp-library-publishing conformance) ──────────────
# No existing check verified whether a library project actually followed
# kmp-library-publishing's own documented setup (vanniktech-mavenPublish,
# binary-compatibility-validator, explicitApi(), and — once split into multiple
# published modules — a build-logic convention plugin instead of duplicating the
# same config per module). Gated on vanniktech-mavenPublish actually being applied
# somewhere in the project — that's the one unambiguous "this is meant to be a
# published library" signal; a plain internal KMP module (:core:network inside an
# app) never applies it, so this can't false-trigger on ordinary app-internal code.

_VANNIKTECH_PLUGIN_RE = re.compile(r"\bcom\.vanniktech\.maven\.publish\b")
_MAVEN_PUBLISHING_COORDINATES_RE = re.compile(r"\bmavenPublishing\s*\{")
_BINARY_COMPAT_VALIDATOR_RE = re.compile(
    r"\bbinary-compatibility-validator\b|\borg\.jetbrains\.kotlinx\.binary-compatibility-validator\b"
)


def _vanniktech_modules(root: Path) -> list[Path]:
    """Return every build.gradle.kts that applies the vanniktech plugin — one
    per published artifact. Length of this list is the single/multi-module signal."""
    modules: list[Path] = []
    for path in root.rglob("build.gradle.kts"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if _VANNIKTECH_PLUGIN_RE.search(text) or _MAVEN_PUBLISHING_COORDINATES_RE.search(text):
            modules.append(path)
    return modules


def _detect_library_missing_binary_compat_validator(root: Path) -> list[str]:
    """Flag a library project (vanniktech applied) with no binary-compatibility-validator
    wired anywhere and no committed `.api` dump — Step 5's tracked-API-surface gate.
    """
    modules = _vanniktech_modules(root)
    if not modules:
        return []
    for path in root.rglob("*.gradle.kts"):
        if _is_excluded(path, root):
            continue
        try:
            if _BINARY_COMPAT_VALIDATOR_RE.search(path.read_text(encoding="utf-8", errors="ignore")):
                return []
        except OSError:
            continue
    if any(root.rglob("*.api")):
        return []
    line_no, snippet = _at(modules[0].read_text(encoding="utf-8", errors="ignore"), 0)
    return [
        f"library missing binary-compat validator [MEDIUM]: {modules[0].relative_to(root)}:1 "
        f"— vanniktech-mavenPublish is applied but no binary-compatibility-validator plugin "
        f"or committed .api file was found; a public API change can ship unreviewed and break "
        f"consumers silently (see kmp-library-publishing Step 5)\n"
        f"    1 | {snippet}"
    ]


def _detect_library_missing_explicit_api(root: Path) -> list[str]:
    """Flag a library project (vanniktech applied) with no explicitApi()/
    explicitApiWarning() anywhere — Step 3's library-only visibility discipline.
    """
    modules = _vanniktech_modules(root)
    if not modules or _project_uses_explicit_api(root):
        return []
    line_no, snippet = _at(modules[0].read_text(encoding="utf-8", errors="ignore"), 0)
    return [
        f"library missing explicitApi() [MEDIUM]: {modules[0].relative_to(root)}:1 "
        f"— vanniktech-mavenPublish is applied but no module calls explicitApi(); without it, "
        f"every internal type Kotlin defaults to public leaks into the published API surface "
        f"(see kmp-library-publishing Step 3)\n"
        f"    1 | {snippet}"
    ]


def _detect_library_multimodule_missing_build_logic(root: Path) -> list[str]:
    """Classify single- vs multi-module library structure, and flag the real
    duplication problem multi-module introduces: 2+ published modules each
    re-applying vanniktech/explicitApi directly instead of sharing one convention
    plugin (kmp-library-publishing Step 1a — build-logic only earns its keep once
    a library splits past one :library module).
    """
    modules = _vanniktech_modules(root)
    if len(modules) < 2:
        return []  # single-module: build-logic adds nothing, per Step 1
    has_build_logic = (root / "build-logic").exists() and any(
        (root / "build-logic").rglob("*.gradle.kts")
    )
    if has_build_logic:
        return []
    module_names = ", ".join(sorted(m.parent.name for m in modules))
    line_no, snippet = _at(modules[0].read_text(encoding="utf-8", errors="ignore"), 0)
    return [
        f"library multi-module missing build-logic [MEDIUM]: {modules[0].relative_to(root)}:1 "
        f"— {len(modules)} published modules ({module_names}) each apply "
        f"vanniktech/explicitApi directly with no build-logic convention plugin; that's real, "
        f"growing duplication a shared plugin removes (see kmp-library-publishing Step 1a)\n"
        f"    1 | {snippet}"
    ]


# ── Undocumented public API (library projects only) ─────────────────────────────
# kmp-library-publishing's explicitApi() forces every public
# declaration to state its visibility explicitly — once "public" is a deliberate
# choice, an undocumented one is a real gap. Gated on the project actually using
# explicitApi()/explicitApiWarning() anywhere; without it "public" isn't a strong
# enough signal to check (most app code is public by Kotlin's own default).

_EXPLICIT_API_MARKER_RE = re.compile(r"\bexplicitApi(?:Warning)?\s*\(")
# `val`/`var` included deliberately: a public property is as much of a published API
# surface as a function under explicitApi() (binary-compatibility-validator tracks it
# either way), but this regex matched only class/interface/object/fun, and
# kmp-code-quality's Detekt block enabled only UndocumentedPublicClass/Function — so a
# public `val` was documented by neither, despite the doc claiming "every public
# declaration". Safe to match here because this whole detector is gated on the project
# actually using explicitApi(), which forces the literal `public` keyword.
_PUBLIC_DECL_RE = re.compile(
    r"(?m)^(?P<indent>[ \t]*)public\s+(?:class|interface|object|fun|val|var)\s+(\w+)"
)


def _project_uses_explicit_api(root: Path) -> bool:
    for path in root.rglob("*.gradle.kts"):
        if _is_excluded(path, root):
            continue
        try:
            if _EXPLICIT_API_MARKER_RE.search(path.read_text(encoding="utf-8", errors="ignore")):
                return True
        except OSError:
            continue
    return False


def _detect_undocumented_public_api(root: Path) -> list[str]:
    """Flag a `public class`/`interface`/`object`/`fun` with no KDoc block in the
    lines immediately above it — scoped to projects that already use explicitApi().
    """
    if not _project_uses_explicit_api(root):
        return []
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
        except OSError:
            continue
        for i, line in enumerate(lines):
            match = _PUBLIC_DECL_RE.match(line)
            if not match:
                continue
            # Look back up to 5 lines for a KDoc close (*/) — the doc block itself
            # may span several lines above that.
            preceding = lines[max(0, i - 5): i]
            if any("*/" in p for p in preceding):
                continue
            findings.append(
                f"undocumented public api [LOW]: {path.relative_to(root)}:{i + 1} "
                f"— '{match.group(2)}' is public with no KDoc; a consumer sees it in "
                f"autocomplete with no explanation. Per kmp-library-"
                f"publishing's KDoc coverage rule, document the public contract\n"
                f"    {i + 1} | {line.strip()}"
            )
    return findings


# ── @Composable function returning Unit named like a verb, not a type ──────────
# Per the Android Kotlin style guide's naming rules: a @Composable function that
# returns Unit is a UI node, not an action — it must be PascalCase, read as a noun
# (AppButton, ProductListScreen), never camelCase like a verb (appButton). A
# @Composable that returns a value (rememberScrollState()) is a factory, not a UI
# node, and correctly stays camelCase — excluded by requiring no explicit return
# type before the opening brace.

_COMPOSABLE_ANNOTATION_RE = re.compile(r"@Composable\b")
_FUN_AFTER_COMPOSABLE_RE = re.compile(
    r"\bfun\s+(?:<[^>]*>\s*)?([a-zA-Z_]\w*)\s*\("
)


def _find_matching_paren(text: str, open_idx: int) -> int | None:
    depth = 0
    for i in range(open_idx, len(text)):
        if text[i] == "(":
            depth += 1
        elif text[i] == ")":
            depth -= 1
            if depth == 0:
                return i
    return None


def _detect_lowercase_unit_composable(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for ann_match in _COMPOSABLE_ANNOTATION_RE.finditer(text):
            fun_match = _FUN_AFTER_COMPOSABLE_RE.search(text, ann_match.end())
            if not fun_match or fun_match.start() > ann_match.end() + 200:
                continue
            name = fun_match.group(1)
            if not name[0].islower():
                continue
            paren_start = fun_match.end() - 1
            close_idx = _find_matching_paren(text, paren_start)
            if close_idx is None:
                continue
            rest = text[close_idx + 1:close_idx + 30]
            after = rest.lstrip()
            if not after.startswith("{"):
                continue  # explicit return type present — a factory function, not a UI node
            line_no, snippet = _at(text, fun_match.start())
            findings.append(
                f"lowercase unit composable [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— '{name}' is a @Composable function returning Unit (a UI node), named "
                f"like a verb; per the Android Kotlin style guide, it must be PascalCase, "
                f"read as a noun (e.g. '{name[0].upper()}{name[1:]}')\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── KDoc documents some parameters but not others ───────────────────────────
# A KDoc block that names one parameter and stays silent on the rest reads as
# complete but isn't — worse than no KDoc, since a reader has no signal anything
# is missing. Coverage must be all-or-nothing: either every parameter gets a
# mention (inline [name] or @param), or none do (a plain summary with no
# parameter-level detail, which the official guidance already allows).

_KDOC_BLOCK_RE = re.compile(r"/\*\*(.*?)\*/", re.DOTALL)
_KDOC_FUN_SIGNATURE_RE = re.compile(r"\bfun\s+(?:<[^>]*>\s*)?\w+\s*\(")
_PARAM_NAME_RE = re.compile(r"^\s*(?:vararg\s+)?(?:val\s+|var\s+)?(\w+)\s*:")
_PARAM_TAG_NAME_RE = re.compile(r"@param\s+(\w+)")
_INLINE_BRACKET_REF_RE = re.compile(r"\[(\w+)\]")


def _detect_partial_param_documentation(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for kdoc_match in _KDOC_BLOCK_RE.finditer(text):
            window_start = kdoc_match.end()
            window = text[window_start:window_start + 300]
            fun_match = _KDOC_FUN_SIGNATURE_RE.search(window)
            if not fun_match or fun_match.start() > 200:
                continue  # not immediately followed by a function (annotations tolerated)
            paren_start = window_start + fun_match.end() - 1
            close_idx = _find_matching_paren(text, paren_start)
            if close_idx is None:
                continue
            params = _split_top_level(text[paren_start + 1:close_idx])
            names = [m.group(1) for p in params if (m := _PARAM_NAME_RE.search(p))]
            if len(names) < 2:
                continue  # "partial" is meaningless with 0-1 params
            kdoc_body = kdoc_match.group(1)
            documented = set(_PARAM_TAG_NAME_RE.findall(kdoc_body)) | set(
                _INLINE_BRACKET_REF_RE.findall(kdoc_body)
            )
            covered = [n for n in names if n in documented]
            missing = [n for n in names if n not in documented]
            if not covered or not missing:
                continue  # zero coverage (allowed) or full coverage — not partial
            line_no, snippet = _at(text, window_start + fun_match.start())
            findings.append(
                f"partial param documentation [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— KDoc documents {covered} but not {missing}; per "
                f"kmp-code-quality's coverage rule, either address every "
                f"parameter or none — a partially-documented signature reads as complete "
                f"and isn't\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Full kotlin-reflect usage in commonMain ──────────────────────────────────
# kotlin-reflect is JVM-primary — limited/absent on Kotlin/Native and Kotlin/JS, and a
# real runtime cost even on JVM. A commonMain file reaching for full reflection (not the
# always-available KClass/::class literal) signals the platform split was skipped.

_KOTLIN_REFLECT_FULL_RE = re.compile(
    r"\bimport\s+kotlin\.reflect\.full\.|\bmemberProperties\b|\bdeclaredMemberFunctions\b|"
    r"\bprimaryConstructor\b|\.callBy\("
)


def _detect_kotlin_reflect_in_common(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        if "/commonmain/" not in path.as_posix().lower():
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        match = _KOTLIN_REFLECT_FULL_RE.search(text)
        if not match:
            continue
        line_no, snippet = _at(text, match.start())
        findings.append(
            f"kotlin-reflect in commonMain [MEDIUM]: {path.relative_to(root)}:{line_no} "
            f"— full reflection API used in shared code; kotlin-reflect is JVM-primary "
            f"and limited/absent on Native and JS/Wasm. Use kotlinx.serialization "
            f"(compiler-plugin codegen, no runtime reflection) for cross-platform needs, "
            f"or move this code to a JVM-only module\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── God Utils/Helpers/Extensions file ────────────────────────────────────────
# A *Utils.kt/*Helpers.kt file accumulating unrelated top-level functions across
# different domains has no single responsibility — the filename tells a reader nothing
# about what's actually inside. A file of extensions all sharing one receiver type is
# fine; the smell is unrelated functions sharing only a generic filename.

# `Extensions` included because kmp-code-quality's own rule names all three
# (`Utils.kt`/`Helpers.kt`/`Extensions.kt`) — the regex covered only two, so a god
# `AppExtensions.kt` sailed through the check written to catch it. Safe to add: the
# _GOD_UTILS_MIN_RECEIVER_TYPES threshold below still exempts the *recommended* shape
# (`StringExtensions.kt`, all one receiver type), which is 1 receiver, not 3+.
_UTILS_FILENAME_RE = re.compile(r"(Utils|Helpers|Extensions)$", re.IGNORECASE)
# Visibility modifiers matter here: this was `^fun\s+`, which matches a bare top-level
# `fun` only. Under explicitApi() — which every library this collection scaffolds turns
# on — every top-level function is written `public fun`/`internal fun`, so the detector
# found zero functions and silently never fired, in exactly the projects where API
# hygiene matters most.
# A generic receiver (`fun List<String>.foo()`) matched nothing at all before: the
# receiver group was `([\w.]+)\.`, which stops dead at the `<`, so the whole line failed
# to parse and the function was invisible to both the count and the receiver-diversity
# check. The receiver's type arguments are consumed but not captured, so `List<String>`
# and `List<Int>` count as the same receiver type — which is what "distinct receiver
# types" should mean here.
_TOP_LEVEL_FUN_RE = re.compile(
    r"(?m)^(?:public\s+|internal\s+|private\s+)?fun\s+(?:<[^>]*>\s*)?"
    r"(?:([\w.]+)(?:<[^>]*>)?\.)?(\w+)\s*\("
)
_GOD_UTILS_MIN_FUNCTIONS = 10
_GOD_UTILS_MIN_RECEIVER_TYPES = 3


def _detect_god_utils_file(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        if not _UTILS_FILENAME_RE.search(path.stem):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        matches = list(_TOP_LEVEL_FUN_RE.finditer(text))
        if len(matches) < _GOD_UTILS_MIN_FUNCTIONS:
            continue
        receivers = {m.group(1) or "(none)" for m in matches}
        if len(receivers) < _GOD_UTILS_MIN_RECEIVER_TYPES:
            continue
        findings.append(
            f"god utils file [LOW]: {path.relative_to(root)} — {len(matches)} top-level "
            f"functions spanning {len(receivers)} distinct receiver types/none "
            f"({sorted(receivers)}); split by what each function is for "
            f"(StringExtensions.kt, DateExtensions.kt, ...) or move each into the "
            f"module that owns its domain\n"
            f"    1 | {path.name}"
        )
    return findings


# ── Regex literal inlined instead of bound to a named val ───────────────────
# A regex used more than once, or complex enough to need explaining, should be a
# well-named constant — an inline literal buried in a function call is unreadable at
# the call site and gets recompiled on every call if it's on a hot path.

_INLINE_REGEX_CALL_RE = re.compile(r"\b(?:Regex|Pattern)\s*\(\s*\"(?P<pattern>(?:[^\"\\]|\\.)*)\"|\"(?P<pattern2>(?:[^\"\\]|\\.)*)\"\.toRegex\(")
_NAMED_REGEX_BINDING_RE = re.compile(
    r"\b(?:private\s+|internal\s+)?val\s+\w+\s*(?::\s*(?:Regex|Pattern))?\s*=\s*"
)
_MIN_INLINE_REGEX_PATTERN_LEN = 12


def _detect_inline_unnamed_regex(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
        except OSError:
            continue
        for i, line in enumerate(lines):
            match = _INLINE_REGEX_CALL_RE.search(line)
            if not match:
                continue
            prefix = line[: match.start()]
            if _NAMED_REGEX_BINDING_RE.search(prefix):
                continue  # bound to a val on this line — named, not inline
            pattern_text = match.group("pattern") or match.group("pattern2") or ""
            if len(pattern_text) < _MIN_INLINE_REGEX_PATTERN_LEN:
                continue  # short enough not to need a name
            findings.append(
                f"inline unnamed regex [LOW]: {path.relative_to(root)}:{i + 1} — a "
                f"Regex/Pattern is constructed inline as part of an expression instead "
                f"of bound to a named `val`; bind it to a well-named constant so the "
                f"call site is readable and it isn't recompiled on every call\n"
                f"    {i + 1} | {line.strip()}"
            )
    return findings


# ── Expensive object instantiated inside a loop ──────────────────────────────
# Verified against Detekt's own Performance ruleset (detekt.dev/docs/rules/performance)
# before writing this — none of its 8 rules cover an object constructed inside a loop
# body that doesn't depend on the loop variable. Real, common perf killer: a formatter/
# client/parser rebuilt every iteration instead of hoisted once before the loop.

_EXPENSIVE_CTOR_RE = re.compile(
    r"\b(SimpleDateFormat|DateTimeFormatter|HttpClient|MessageDigest|Gson|ObjectMapper)\s*\("
)
_FOR_IN_LOOP_RE = re.compile(r"\bfor\s*\(\s*(\w+)\s+in\s+[^)]*\)\s*\{")
_WHILE_LOOP_RE = re.compile(r"\bwhile\s*\([^)]*\)\s*\{")


def _find_matching_brace(text: str, open_idx: int) -> int | None:
    depth = 0
    for i in range(open_idx, len(text)):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                return i
    return None


def _detect_object_creation_in_loop(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        loop_matches = [
            (m, m.group(1)) for m in _FOR_IN_LOOP_RE.finditer(text)
        ] + [
            (m, None) for m in _WHILE_LOOP_RE.finditer(text)
        ]
        for loop_match, loop_var in loop_matches:
            brace_open = text.index("{", loop_match.start())
            brace_close = _find_matching_brace(text, brace_open)
            if brace_close is None:
                continue
            body = text[brace_open + 1: brace_close]
            for ctor_match in _EXPENSIVE_CTOR_RE.finditer(body):
                ctor_name = ctor_match.group(1)
                paren_open = ctor_match.end() - 1
                paren_close = _find_matching_paren(body, paren_open)
                args = body[paren_open + 1: paren_close] if paren_close is not None else ""
                if loop_var and re.search(rf"\b{re.escape(loop_var)}\b", args):
                    continue  # constructor genuinely depends on the loop variable
                abs_pos = brace_open + 1 + ctor_match.start()
                line_no, snippet = _at(text, abs_pos)
                findings.append(
                    f"object creation in loop [MEDIUM]: {path.relative_to(root)}:{line_no} "
                    f"— '{ctor_name}' constructed inside the loop body with no apparent "
                    f"dependency on the loop variable; hoist it to a val before the loop "
                    f"so it's built once, not once per iteration\n"
                    f"    {line_no} | {snippet}"
                )
    return findings


# ── Public mutable collection exposure ───────────────────────────────────────
# Distinct from _detect_compose_unstable_collection_param, which is scoped to
# @Composable function parameters (a recomposition-stability concern). This is an
# encapsulation concern: a public MutableList/MutableMap/MutableSet property or return
# type lets any caller mutate internal state through the reference — real regardless of
# Compose, and especially relevant on an explicitApi() library's public surface.

_PUBLIC_MUTABLE_COLLECTION_RE = re.compile(
    r"(?m)^(?!.*\bprivate\b)(?!.*\binternal\b).*\b(?:val|var|fun)\s+\w+\s*"
    r"(?::\s*Mutable(List|Map|Set)<|\([^)]*\)\s*:\s*Mutable(List|Map|Set)<)"
)


def _detect_public_mutable_collection(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for match in _PUBLIC_MUTABLE_COLLECTION_RE.finditer(text):
            kind = match.group(1) or match.group(2)
            line_no, snippet = _at(text, match.start())
            findings.append(
                f"public mutable collection exposure [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— a public declaration exposes Mutable{kind} directly; a caller holding "
                f"this reference can mutate your internal state. Expose {kind} (read-only) "
                f"instead, backed by a private Mutable{kind}\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Android Context/Activity leak in a singleton scope ───────────────────────
# The classic Android memory leak: a companion object or singleton `object` holding
# a Context/Activity reference outlives the Activity itself, preventing garbage
# collection. `applicationContext`/`Application` is safe to hold long-term (it lives
# for the process) — this only flags Context/Activity/*Activity subtypes.

_SINGLETON_SCOPE_RE = re.compile(r"\bcompanion\s+object\b[^{]*\{|(?<!companion\s)\bobject\s+\w+\s*(?::[^{]*)?\{")
_CONTEXT_PROPERTY_RE = re.compile(
    r"(?m)^\s*(?:private\s+|internal\s+)?(?:var|val)\s+\w+\s*:\s*"
    r"(Context|Activity|FragmentActivity|AppCompatActivity|ComponentActivity)\??\s*(?:=|$)"
)


def _detect_context_leak_in_singleton(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for scope_match in _SINGLETON_SCOPE_RE.finditer(text):
            brace_open = text.index("{", scope_match.start())
            brace_close = _find_matching_brace(text, brace_open)
            if brace_close is None:
                continue
            body = text[brace_open + 1: brace_close]
            for prop_match in _CONTEXT_PROPERTY_RE.finditer(body):
                type_name = prop_match.group(1)
                abs_pos = brace_open + 1 + prop_match.start()
                line_no, snippet = _at(text, abs_pos)
                findings.append(
                    f"context leak in singleton [HIGH]: {path.relative_to(root)}:{line_no} "
                    f"— a companion object/singleton stores a `{type_name}` reference; it "
                    f"outlives the Activity, preventing garbage collection. Store "
                    f"applicationContext instead (safe — lives for the process), or don't "
                    f"cache the reference at all\n"
                    f"    {line_no} | {snippet}"
                )
    return findings


# ── Hardcoded user-facing string in Compose UI ───────────────────────────────
# Documented in this skill's own "What to Inspect" checklist since the beginning
# ("flag hardcoded user-facing strings, route to kmp-shared-resources")
# but never mechanically checked — every other "hardcoded X" (colors, spacing, URLs,
# version codes) already has a real detector; strings didn't.

_HARDCODED_TEXT_CALL_RE = re.compile(
    r'\b(?:Text|AppText|ShadcnText)\s*\(\s*"([^"]{2,})"'
)
_HARDCODED_CONTENT_DESC_RE = re.compile(r'\bcontentDescription\s*=\s*"([^"]{2,})"')
_NON_TRANSLATABLE_STRING_RE = re.compile(r"^[\d\s.,:/%+\-]*$")


def _detect_hardcoded_ui_string(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        if "Preview" in path.stem:
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue
        for pattern, label in (
            (_HARDCODED_TEXT_CALL_RE, "Text/AppText/ShadcnText call"),
            (_HARDCODED_CONTENT_DESC_RE, "contentDescription"),
        ):
            for match in pattern.finditer(text):
                literal = match.group(1)
                if _NON_TRANSLATABLE_STRING_RE.match(literal):
                    continue  # numbers/punctuation only — nothing to localize
                line_no, snippet = _at(text, match.start())
                findings.append(
                    f"hardcoded ui string [LOW]: {path.relative_to(root)}:{line_no} "
                    f"— literal \"{literal}\" in a {label}; route through "
                    f"kmp-shared-resources's stringResource(Res.string.x) "
                    f"instead so it can be localized\n"
                    f"    {line_no} | {snippet}"
                )
    return findings


_EXCLUDED_DIRS = {
    "build", ".gradle", ".git", "vendor", "third_party",
    "node_modules", ".idea", ".kotlin", "kotlin-js-store",
    "worktrees",  # .claude/worktrees/ — agent scratch copies of the repo
    # Deployed agent skills bundles (kmp-agent-skills' own reference templates,
    # example code, and scripts) — not the consumer project's source. Scanning these
    # produces false positives from the skill collection's own scaffold templates
    # (e.g. kmp-feature-scaffold's templates/androidApp/build.gradle.kts
    # legitimately has a literal versionCode = 1 placeholder, meant to be filled in
    # during real scaffolding, not a hardcoded value in a real app). Matches the same
    # agent-dir candidates update-consumer-skills.sh already recognizes.
    ".claude", ".codex", ".cursor", ".continue", "copilot",
}

# ── Hardcoded Android versionCode ─────────────────────────────────────────────

# A bare integer literal assignment — nothing else on the value side. A derived
# expression (major * 1_000_000 + minor * 1_000 + patch, or a variable reference)
# never matches this because the lookahead requires end-of-line/comment/brace right
# after the digits, with no trailing operator or identifier.
_VERSION_CODE_LITERAL_RE = re.compile(
    r"versionCode\s*=\s*([0-9][0-9_]*)[ \t]*(?=\r?\n|//|\}|$)", re.MULTILINE
)
_ANDROID_APP_MARKER_RE = re.compile(r"applicationId\s*=|com\.android\.application")


def _detect_hardcoded_android_version_code(root: Path) -> list[str]:
    """Flag a hardcoded (literal) Android versionCode in an app module.

    versionCode must be strictly increasing across ACCEPTED Play Console uploads. A bare
    integer literal will not auto-increment on the next release — it must be derived from
    the single semver source of truth (e.g. major*1_000_000 + minor*1_000 + patch). This
    is a silent trap: the app builds and runs identically whether versionCode is derived
    or hardcoded, and the bug only surfaces as a hard Play Console rejection on the
    SECOND release, by which point several versions may have shipped unnoticed.
    """
    findings: list[str] = []
    for path in root.rglob("*.gradle.kts"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _ANDROID_APP_MARKER_RE.search(text):
            continue
        m = _VERSION_CODE_LITERAL_RE.search(text)
        if m:
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"hardcoded android versioncode [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— versionCode is a literal integer ({m.group(1)}); Play Console rejects an "
                f"upload whose versionCode isn't strictly higher than the last accepted one — "
                f"derive it from the semver source instead, e.g. major*1_000_000 + minor*1_000 "
                f"+ patch (see release skill → platform-native version fields)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Compose Styles API compliance ─────────────────────────────────────────────
# Detectors below check generated/hand-written code against the official Do's/Don'ts
# and Limitations in references/compose-styles-api-reference.md (design-system skill).

# Don't #2 — a `style: Style = Style { ... }` default WITH A BODY. The sanctioned form
# is an empty default (`style: Style = Style`); defaults are merged inside the function.
_STYLE_DEFAULT_WITH_BODY_RE = re.compile(r"style\s*:\s*Style\s*=\s*Style\s*\{")

# Regression guard for the isEnabled bug: `.enabled = ` is not a real StyleState
# property (the official API uses `isEnabled`). Scoped to a `styleState` receiver name
# to avoid matching unrelated `.enabled` properties on other types.
_STYLE_STATE_WRONG_ENABLED_RE = re.compile(r"\b(?:styleState|it)\.enabled\s*=")

# Don't #3 — a `style: Style` parameter on a composable whose name signals a screen/page,
# not a component. Reuses the same stems the structural detectors already recognize.
_STYLE_PARAM_ON_SCREEN_RE = re.compile(
    r"@Composable\s+(?:private\s+|internal\s+|public\s+)?fun\s+(\w+)\s*\([^)]*\bstyle\s*:\s*Style\b"
)

# Don't #4 — a @Composable function named ...Style(): Style that reads a CompositionLocal
# (MaterialTheme.* or a Local*.current accessor) and returns a Style built from it. The
# value is captured once at definition time and goes stale when the theme changes.
_STYLE_RETURNING_FUN_RE = re.compile(
    r"@Composable\s+fun\s+\w*[Ss]tyle\s*\([^)]*\)\s*:\s*Style\s*\{(?P<body>.*?)\n\}",
    re.DOTALL,
)
_COMPOSITIONLOCAL_READ_RE = re.compile(r"MaterialTheme\.\w+|\bLocal\w+\.current\b")

# Limitation §5 — pressed{}/hovered{} Style blocks combined with a clickable() that
# doesn't set indication = null render both the Style animation AND the default ripple.
_STYLE_PRESSED_OR_HOVERED_RE = re.compile(r"\b(?:pressed|hovered)\s*\{")
_CLICKABLE_CALL_RE = re.compile(r"\bclickable\s*\(")
_INDICATION_NULL_RE = re.compile(r"\bindication\s*=\s*null\b")


def _detect_style_default_with_body(root: Path) -> list[str]:
    """Flag `style: Style = Style { ... }` — a default WITH a body.

    The sanctioned pattern is an empty default (`style: Style = Style`) with project
    defaults merged inside the function via `defaultStyle then style` in
    `Modifier.styleable(...)`. A default with a body can silently clobber the merge
    order and makes the "empty by convention" contract ambiguous for callers.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        m = _STYLE_DEFAULT_WITH_BODY_RE.search(text)
        if m:
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"style default with body [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— 'style: Style = Style {{ ... }}' as a parameter default; use an empty "
                f"'style: Style = Style' and merge project defaults inside via "
                f"'defaultStyle then style' in Modifier.styleable(...) "
                f"(see compose-styles-api-reference.md → Do's #6, Don't #2)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


def _detect_style_state_wrong_enabled(root: Path) -> list[str]:
    """Flag `styleState.enabled = ...` — not a real StyleState property.

    The official API property is `isEnabled`, set via
    `rememberUpdatedStyleState(interactionSource) { it.isEnabled = enabled }`. This is a
    regression guard for the exact bug found and fixed across AppButton/AppChip/
    AppTextField/AppIconButton during the Compose Styles API doc audit.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        m = _STYLE_STATE_WRONG_ENABLED_RE.search(text)
        if m:
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"style state wrong enabled property [HIGH]: {path.relative_to(root)}:{line_no} "
                f"— '.enabled = ' is not a real StyleState property; the API uses 'isEnabled'. "
                f"Use rememberUpdatedStyleState(interactionSource) {{ it.isEnabled = enabled }} "
                f"(see compose-styles-api-reference.md § State construction)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


def _detect_style_param_on_screen(root: Path) -> list[str]:
    """Flag a `style: Style` parameter on a screen/page-named composable.

    Styles are designed for components, not layouts or screen-level composables — the
    official docs call this out explicitly (unclear to callers what a style would do at
    the layout level).
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for m in _STYLE_PARAM_ON_SCREEN_RE.finditer(text):
            fn_name = m.group(1)
            if not any(fn_name.endswith(stem) for stem in _SCREEN_STEMS):
                continue
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"style param on screen composable [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— @Composable {fn_name}(...) takes a 'style: Style' param; Styles are for "
                f"components, not screens/layouts — hoist the styling into a child component "
                f"instead (see compose-styles-api-reference.md → Don't #3)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


def _detect_stale_compositionlocal_in_style_function(root: Path) -> list[str]:
    """Flag a @Composable ...Style(): Style function that reads a CompositionLocal and
    returns a built Style — the value is captured once at definition time, not at the
    point the Style is actually consumed, and goes stale when the theme changes.

    The correct pattern is a StyleScope extension property (e.g. `val StyleScope.colors
    get() = ...`) read INSIDE the `Style { }` lambda, never outside it.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for m in _STYLE_RETURNING_FUN_RE.finditer(text):
            body = m.group("body")
            return_style_idx = body.find("return Style")
            if return_style_idx == -1:
                continue
            before_return = body[:return_style_idx]
            local_match = _COMPOSITIONLOCAL_READ_RE.search(before_return)
            if not local_match:
                continue
            line_no, snippet = _at(text, m.start() + len("@Composable fun "))
            findings.append(
                f"stale compositionlocal in style function [HIGH]: {path.relative_to(root)}:{line_no} "
                f"— a @Composable fun ...Style(): Style reads '{local_match.group(0)}' before "
                f"returning the Style; the value is captured once at definition time and goes "
                f"stale when the theme changes. Use a StyleScope extension property read inside "
                f"the Style {{ }} lambda instead (see compose-styles-api-reference.md → Don't #4)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


def _detect_missing_indication_null_with_style_state(root: Path) -> list[str]:
    """Flag a file with a Style `pressed {}`/`hovered {}` block and a `clickable(...)`
    call that has no `indication = null` anywhere in the file.

    Without indication = null, the Style-driven visual change AND the platform's default
    ripple render simultaneously — a visibly doubled effect (official Limitations §5).
    This is a file-level heuristic; verify the specific clickable() at the flagged line.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _STYLE_PRESSED_OR_HOVERED_RE.search(text):
            continue
        click_match = _CLICKABLE_CALL_RE.search(text)
        if not click_match:
            continue
        if _INDICATION_NULL_RE.search(text):
            continue
        line_no, snippet = _at(text, click_match.start())
        findings.append(
            f"missing indication null with style state [LOW]: {path.relative_to(root)}:{line_no} "
            f"— file has a Style pressed{{}}/hovered{{}} block and a clickable(...) with no "
            f"indication = null anywhere in the file; the Style animation and the default "
            f"ripple will render simultaneously (see compose-styles-api-reference.md → "
            f"Limitations §5)\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── Toggle/collapsible layout stability ────────────────────────────────────────
# A trigger button (accordion header, collapsible chevron) must never shift position
# when toggled. The two most common causes: swapping between two differently-sized
# icon composables instead of rotating one icon, and toggling content with a bare
# `if` instead of AnimatedVisibility/animateContentSize (an instant layout snap reads
# as "the button moved"). See kmp-compose-design-system-extended's AppAccordion
# for the correct pattern (graphicsLayer { rotationZ } + AnimatedVisibility below a
# stable trigger row).

_CHEVRON_ICON_PAIRS = [
    ("KeyboardArrowDown", "KeyboardArrowUp"),
    ("ChevronDown", "ChevronUp"),
    ("ExpandMore", "ExpandLess"),
    ("ArrowDropDown", "ArrowDropUp"),
]
_GRAPHICS_ROTATION_RE = re.compile(r"graphicsLayer\s*\{[^}]*rotationZ")
_MODIFIER_ROTATE_RE = re.compile(r"Modifier\s*\.\s*rotate\s*\(")


def _detect_toggle_icon_swap(root: Path) -> list[str]:
    """Flag a file that references both icons of a known chevron/expand pair
    (e.g. KeyboardArrowDown + KeyboardArrowUp) with no graphicsLayer { rotationZ }
    or Modifier.rotate() anywhere in the file.

    Swapping between two icon composables on toggle can change the trigger row's
    measured bounds if the two icons' intrinsic sizes differ even slightly — shifting
    the trigger's position in its parent. Rotating a single icon via a draw-phase
    transform never changes layout bounds, regardless of angle. File-level heuristic —
    verify the specific icon swap at the flagged line.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if _GRAPHICS_ROTATION_RE.search(text) or _MODIFIER_ROTATE_RE.search(text):
            continue
        for down_name, up_name in _CHEVRON_ICON_PAIRS:
            if down_name in text and up_name in text:
                match = re.search(re.escape(down_name), text)
                line_no, snippet = _at(text, match.start())
                findings.append(
                    f"toggle icon swap instead of rotation [MEDIUM]: {path.relative_to(root)}:{line_no} "
                    f"— both {down_name} and {up_name} appear in this file with no "
                    f"graphicsLayer {{ rotationZ }} / Modifier.rotate() present; swapping between "
                    f"two icon composables on toggle can shift the trigger's layout bounds if their "
                    f"intrinsic sizes differ — rotate a single icon instead\n"
                    f"    {line_no} | {snippet}"
                )
                break
    return findings


_BARE_CONDITIONAL_EXPAND_RE = re.compile(
    r"\bif\s*\(\s*[\w.!]*(?:[Ee]xpand|[Ii]sOpen|[Tt]oggled)\w*\s*\)\s*\{"
)
_ANIMATED_VISIBILITY_RE = re.compile(r"\bAnimatedVisibility\s*\(")
_ANIMATE_CONTENT_SIZE_RE = re.compile(r"\.animateContentSize\s*\(")
_COMPOSABLE_CALL_IN_BLOCK_RE = re.compile(r"\b[A-Z]\w*\s*\(")


def _detect_bare_conditional_collapse(root: Path) -> list[str]:
    """Flag a bare `if (isExpanded) { ... }` around what looks like composable content
    (a PascalCase call inside the block) when the file has no AnimatedVisibility or
    .animateContentSize() anywhere.

    A raw conditional snaps the layout instantly instead of animating it — the
    instant jump is what reads as "the trigger button moved" even though the trigger
    itself never changed. File-level heuristic — verify the flagged block actually
    renders collapsible content, not just a state/log toggle.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if _ANIMATED_VISIBILITY_RE.search(text) or _ANIMATE_CONTENT_SIZE_RE.search(text):
            continue
        match = _BARE_CONDITIONAL_EXPAND_RE.search(text)
        if not match:
            continue
        window = text[match.end():match.end() + 400]
        if not _COMPOSABLE_CALL_IN_BLOCK_RE.search(window):
            continue
        line_no, snippet = _at(text, match.start())
        findings.append(
            f"bare conditional collapse [MEDIUM]: {path.relative_to(root)}:{line_no} "
            f"— content is shown/hidden with a raw `if` and no AnimatedVisibility / "
            f".animateContentSize() anywhere in the file; this snaps the layout instantly "
            f"and can visibly shift a sibling trigger's position — wrap the conditional "
            f"content in AnimatedVisibility(expandVertically()/shrinkVertically()) or add "
            f".animateContentSize() to the containing layout\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── Focused/selected state animates border width ────────────────────────────────
# CSS uses `ring` (box-shadow) instead of `border` for focus indicators specifically
# because box-shadow never participates in the box model. The Compose equivalent isn't
# a new primitive — animating borderWidth (or borderBottomWidth/etc.) inside a state
# block re-measures the component on every focus/selection change, which can visibly
# shift the component or its siblings. Reserve the final width at rest (transparent
# color if there's no border at rest) and animate borderColor only.

_STATE_BLOCK_BORDER_WIDTH_RE = re.compile(
    r"\b(?:focused|selected)\s*\{[^}]{0,200}?border\w*Width\s*\("
)


def _detect_focused_state_animates_border_width(root: Path) -> list[str]:
    """Flag a `focused {}`/`selected {}` Style block that changes a border width
    property (borderWidth, borderBottomWidth, etc.) instead of only borderColor.

    File-level heuristic (bounded lookahead window) — verify the specific state block
    at the flagged line actually changes width, not just color.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        match = _STATE_BLOCK_BORDER_WIDTH_RE.search(text)
        if not match:
            continue
        line_no, snippet = _at(text, match.start())
        findings.append(
            f"focused state animates border width [MEDIUM]: {path.relative_to(root)}:{line_no} "
            f"— a focused{{}}/selected{{}} Style block changes a border width property; "
            f"animating border width re-measures the component on focus/selection and can "
            f"shift its layout — reserve the final width at rest (borderColor(Color.Transparent) "
            f"if there's no border at rest) and animate borderColor only\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── Combined "one file per X" violations ───────────────────────────────────────
# Several skills document a hard "one file per X, never combine, never append" rule
# (kmp-lessons, kmp-layout-system,
# kmp-sqldelight-setup) enforced only by their scaffolding scripts
# refusing to overwrite — nothing catches a violation that bypassed the script (a
# hand-written file, a merge, a copy-paste). These detectors close that gap.

_LESSON_SECTION_RE = re.compile(r"^##\s+What we followed\s*$", re.MULTILINE)


def _detect_combined_lesson_file(root: Path) -> list[str]:
    """Flag a docs/lessons/*.md file containing more than one lesson.

    kmp-lessons requires exactly one lesson per file — the harvester
    parses docs/lessons/*.md as one Lesson per file, so a combined file silently breaks
    grouping and review. `create_lesson.py` enforces this at creation time, but a
    hand-written or merged file can still violate it; this catches that case.
    """
    findings: list[str] = []
    lessons_dir = root / "docs" / "lessons"
    if not lessons_dir.is_dir():
        return findings
    for path in sorted(lessons_dir.glob("*.md")):
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        count = len(_LESSON_SECTION_RE.findall(text))
        if count > 1:
            findings.append(
                f"combined lesson file [HIGH]: {path.relative_to(root)} — contains "
                f"{count} lessons ('## What we followed' appears {count} times) in one "
                f"file; the harvester reads one Lesson per file. Split into separate "
                f"files via create_lesson.py, one invocation per finding"
            )
    return findings


_LAYOUT_SCREEN_H1_RE = re.compile(r"^#\s+.+$", re.MULTILINE)


def _detect_combined_layout_screen_file(root: Path) -> list[str]:
    """Flag a docs/layout-system/*.md screen file containing more than one screen.

    kmp-layout-system requires one file per screen (`_components.md`
    is the only shared exception). `create_wireframe.py` refuses to overwrite an
    existing screen file, but a hand-edited file can still merge two screens together;
    this catches that case via a simple signal — more than one H1 heading in the file.
    """
    findings: list[str] = []
    layout_dir = root / "docs" / "layout-system"
    if not layout_dir.is_dir():
        return findings
    for path in sorted(layout_dir.glob("*.md")):
        if path.name == "_components.md":
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        count = len(_LAYOUT_SCREEN_H1_RE.findall(text))
        if count > 1:
            findings.append(
                f"combined layout screen file [MEDIUM]: {path.relative_to(root)} — "
                f"contains {count} top-level (`# `) headings, suggesting more than one "
                f"screen was written into this file. One screen per file — run "
                f"create_wireframe.py once per screen instead"
            )
    return findings


_SQ_CREATE_TABLE_RE = re.compile(r"\bCREATE\s+TABLE\b", re.IGNORECASE)


def _detect_combined_sqldelight_table_file(root: Path) -> list[str]:
    """Flag a `.sq` file defining more than one table.

    kmp-sqldelight-setup's Common Anti-Patterns table says to keep
    `.sq` files focused, one file per table — this is a real maintainability rule (query
    files grow unbounded and become hard to navigate when tables are combined) with no
    prior enforcement. Migration files and files with a single CREATE TABLE are fine.
    """
    findings: list[str] = []
    for path in root.rglob("*.sq"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        count = len(_SQ_CREATE_TABLE_RE.findall(text))
        if count > 1:
            findings.append(
                f"combined sqldelight table file [MEDIUM]: {path.relative_to(root)} — "
                f"defines {count} tables (CREATE TABLE appears {count} times) in one "
                f".sq file; keep .sq files focused, one file per table"
            )
    return findings


# ── Combined design-system component file ───────────────────────────────────────
# kmp-compose-design-system/-extended's own generated templates always put
# one component per file (verified: 27 + 18 separate file headings, zero bundling
# across both skills) — but that convention was never stated as a rule, and nothing
# checked it for a real project's own component files. Scoped to designsystem/
# components/ paths so a legitimate multi-composable feature file (Screen + Content,
# or a screen with private helper composables) isn't caught by mistake.

_TOP_LEVEL_COMPOSABLE_NAME_RE = re.compile(
    r"@Composable\s*\n?\s*(?:private\s+|internal\s+|public\s+)?fun\s+(\w+)\s*\("
)


def _is_design_system_component_path(path: Path) -> bool:
    p = path.as_posix().lower()
    return "designsystem" in p or "/components/" in p


def _detect_combined_component_file(root: Path) -> list[str]:
    """Flag a designsystem/components file defining 3+ top-level component-style
    composables — excludes Preview functions and Screen/Content pairs, which
    legitimately live together per this collection's own MVI convention.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        if not _is_design_system_component_path(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        matches = [
            m for m in _TOP_LEVEL_COMPOSABLE_NAME_RE.finditer(text)
            if "Preview" not in m.group(1)
            and not m.group(1).endswith("Screen")
            and not m.group(1).endswith("Content")
        ]
        if len(matches) < 3:
            continue
        names = [m.group(1) for m in matches]
        line_no, snippet = _at(text, matches[0].start())
        findings.append(
            f"combined component file [MEDIUM]: {path.relative_to(root)}:{line_no} — "
            f"defines {len(names)} components ({', '.join(names)}) in one file; "
            f"keep one component per file, matching kmp-compose-design-system's "
            f"own generated file layout\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── Combined design-system style file ────────────────────────────────────────────
# Same bundling problem as _detect_combined_component_file, one directory over:
# styles/ButtonStyles.kt holds exactly ButtonVariant, matched 1:1 with
# components/AppButton.kt. Nothing stopped a styles/AllStyles.kt from bundling
# ButtonVariant/CardVariant/BadgeVariant into one file the same way components/ could.

_VARIANT_SEALED_TYPE_RE = re.compile(r"\bsealed\s+(?:class|interface)\s+(\w*Variant)\b")


def _detect_combined_style_file(root: Path) -> list[str]:
    """Flag a designsystem/styles file defining 2+ *Variant sealed types."""
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        p = path.as_posix().lower()
        if "designsystem" not in p or "/styles/" not in p:
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        matches = list(_VARIANT_SEALED_TYPE_RE.finditer(text))
        if len(matches) < 2:
            continue
        names = [m.group(1) for m in matches]
        line_no, snippet = _at(text, matches[0].start())
        findings.append(
            f"combined style file [MEDIUM]: {path.relative_to(root)}:{line_no} — "
            f"defines {len(names)} variant types ({', '.join(names)}) in one file; "
            f"keep one component's variants per file, matching this project's own "
            f"styles/<Component>Styles.kt layout\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── ViewModel handling too many distinct Intent variants ────────────────────────
# _detect_viewmodel_size only measures line count — a terse ViewModel handling 20+
# Intent variants in short when-branches can dodge that threshold while still doing
# far too much. Counts data class/data object declarations nested inside a
# `sealed interface Intent { ... }` (or `sealed class`) block via brace depth.

_INTENT_SEALED_TYPE_RE = re.compile(r"\bsealed\s+(?:class|interface)\s+Intent\b[^{]*\{")
_INTENT_VARIANT_RE = re.compile(r"\bdata\s+(?:class|object)\s+\w+")
_INTENT_COUNT_THRESHOLD = 15


def _detect_viewmodel_too_many_intents(root: Path) -> list[str]:
    """Flag a ViewModel/Contract file whose sealed Intent type has 15+ variants."""
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not (path.stem.endswith("ViewModel") or path.stem.endswith("Contract")
                or _is_viewmodel_file(text)):
            continue
        match = _INTENT_SEALED_TYPE_RE.search(text)
        if not match:
            continue
        depth = 1
        i = match.end()
        while i < len(text) and depth > 0:
            if text[i] == "{":
                depth += 1
            elif text[i] == "}":
                depth -= 1
            i += 1
        body = text[match.end():i]
        count = len(_INTENT_VARIANT_RE.findall(body))
        if count < _INTENT_COUNT_THRESHOLD:
            continue
        line_no, snippet = _at(text, match.start())
        findings.append(
            f"viewmodel too many intents [MEDIUM]: {path.relative_to(root)}:{line_no} "
            f"— {count} Intent variants in one sealed type; a ViewModel handling this "
            f"many distinct user actions is likely doing more than one screen's worth "
            f"of work even if it stays under the line-count threshold — consider "
            f"splitting into separate screens/ViewModels per kmp-mvi's "
            f"orchestration decision order\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── ViewModel exposing multiple StateFlow properties instead of one UiState ────
# MVI's whole contract is one State per screen. A ViewModel exposing state1/state2/
# state3 as separate public StateFlows is often the same god-ViewModel smell wearing
# a different shape — the fix is combine() into one State, not multiple flows a
# screen has to collect independently. `state` and `effect` are the standard MVI
# pair and excluded.

_PUBLIC_STATEFLOW_PROPERTY_RE = re.compile(r"(?m)^\s*val\s+(\w+)\s*:\s*StateFlow\s*<")


def _detect_viewmodel_multiple_stateflows(root: Path) -> list[str]:
    """Flag a ViewModel exposing 2+ public StateFlow properties beyond `state`."""
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not (path.stem.endswith("ViewModel") or _is_viewmodel_file(text)):
            continue
        matches = [
            m for m in _PUBLIC_STATEFLOW_PROPERTY_RE.finditer(text)
            if m.group(1) != "state"
        ]
        if len(matches) < 2:
            continue
        names = [m.group(1) for m in matches]
        line_no, snippet = _at(text, matches[0].start())
        findings.append(
            f"viewmodel multiple stateflows [MEDIUM]: {path.relative_to(root)}:{line_no} "
            f"— exposes {len(names)} StateFlow properties beyond 'state' "
            f"({', '.join(names)}); MVI's contract is one State per screen — combine() "
            f"these into one State instead of making the screen collect multiple flows\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── ViewModel injecting a Repository directly instead of a UseCase ─────────────
# kmp-mvi's own changelog: "the boundary rule (ViewModel only ever
# depends on :domain) is bright-line and mechanically checkable" — it wasn't actually
# checked. _detect_module_layer_violation can't catch this either: _ALLOWED_DEPS
# explicitly permits presenter -> api at the module level (legitimate for other
# reasons), so a ViewModel injecting a Repository interface directly doesn't fail
# that check. This is a file-level check instead: a *ViewModel's primary constructor
# parameter typed *Repository.

_VM_CLASS_CTOR_RE = re.compile(r"\bclass\s+(\w*ViewModel)\s*\(([^)]*)\)\s*(?::[^{]*)?\{", re.DOTALL)
_CTOR_PARAM_TYPE_RE = re.compile(r"\bval\s+\w+\s*:\s*(\w+)")


def _detect_viewmodel_injects_repository(root: Path) -> list[str]:
    """Flag a ViewModel's primary constructor taking a *Repository param directly —
    per kmp-mvi's rule, a ViewModel depends on :domain (use cases),
    never :api/:data (repositories) directly, with no trivial-case exception.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for match in _VM_CLASS_CTOR_RE.finditer(text):
            vm_name = match.group(1)
            params = _split_top_level(match.group(2))
            repo_params = [
                pm.group(1)
                for p in params
                if (pm := _CTOR_PARAM_TYPE_RE.search(p)) and pm.group(1).endswith("Repository")
            ]
            if not repo_params:
                continue
            line_no, snippet = _at(text, match.start())
            findings.append(
                f"viewmodel injects repository [HIGH]: {path.relative_to(root)}:{line_no} "
                f"— '{vm_name}' takes {', '.join(repo_params)} directly in its "
                f"constructor; per kmp-mvi, a ViewModel depends on a "
                f"use case (:domain), never a repository (:api/:data) directly — no "
                f"trivial-pass-through exception\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── ViewModel name vs its own Intent set (semantic naming drift hint) ──────────
# Non-blocking by design: a token-overlap heuristic will misfire on legitimately
# generic names. Kept out of audit_project()'s findings list and surfaced through a
# separate hints() channel in main() so it never gates CI or /kmp-verify — it's a
# nudge to manually check the name still matches the behavior, not an enforced rule.

_INTENT_VARIANT_RE = re.compile(r"\bdata\s+(?:object|class)\s+(\w+)\s*[:(]")
_INTENT_BLOCK_RE = re.compile(r"sealed\s+interface\s+Intent\b[^{]*\{(.*?)\n\s*\}", re.DOTALL)
_CAMEL_WORD_RE = re.compile(r"[A-Z][a-z0-9]*|[a-z0-9]+")


def _camel_words(name: str) -> set[str]:
    return {w.lower() for w in _CAMEL_WORD_RE.findall(name) if len(w) > 2}


def _detect_name_behavior_drift(root: Path) -> list[str]:
    """Hint-only: flag a ViewModel whose name shares no words with any of its own
    Intent variant names. Reads a sibling <Base>Contract.kt if present (the
    kmp-mvi Contract-pattern convention), else the ViewModel file
    itself. Needs at least 2 intents to fire — too little signal below that.
    """
    findings: list[str] = []
    for path in root.rglob("*ViewModel.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        base = path.stem[: -len("ViewModel")]
        if not base:
            continue
        contract_path = path.parent / f"{base}Contract.kt"
        source_path = contract_path if contract_path.is_file() else path
        try:
            text = source_path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue

        block_match = _INTENT_BLOCK_RE.search(text)
        search_text = block_match.group(1) if block_match else text
        intents = _INTENT_VARIANT_RE.findall(search_text)
        if len(intents) < 2:
            continue

        base_words = _camel_words(base)
        intent_words: set[str] = set()
        for intent in intents:
            intent_words |= _camel_words(intent)
        if not base_words or base_words & intent_words:
            continue

        shown = ", ".join(intents[:5]) + ("..." if len(intents) > 5 else "")
        findings.append(
            f"name-behavior drift (hint) [INFO]: {path.relative_to(root)} — "
            f"'{base}ViewModel' shares no words with its own Intents ({shown}); "
            f"verify the name still describes what this screen does — non-blocking, "
            f"manual check only"
        )
    return findings


# ── Vague class-name suffix (hint) ───────────────────────────────────────────
# Non-blocking by design, same reasoning as name-behavior drift above: Manager/
# Processor/Helper/Info/Data are a well-known naming smell (Clean Code) — they say a
# class "does stuff" without saying what, but a well-scoped, small class using one of
# these suffixes for a genuinely bounded concern (this repo's own offline-first skill
# ships a SyncManager interface) is not automatically wrong. A pure regex can't judge
# whether the name is actually vague for *this* class's real responsibility — that
# needs a reader, so this stays a nudge, never a blocking finding.

_VAGUE_CLASS_DECL_RE = re.compile(
    r"(?m)^(?!.*\b(?:data|enum|sealed|annotation)\s+class\b)"
    r"(?:[\w@()]+\s+)*(?:class|interface|object)\s+(\w*(?:Manager|Processor|Helper|Info|Data))\b"
)


def _detect_vague_class_name_suffix(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for match in _VAGUE_CLASS_DECL_RE.finditer(text):
            name = match.group(1)
            line_no, snippet = _at(text, match.start())
            findings.append(
                f"vague class name suffix (hint) [INFO]: {path.relative_to(root)}:{line_no} "
                f"— '{name}' uses a filler suffix (Manager/Processor/Helper/Info/Data) "
                f"that names what the class *is* without saying what it *does*; per "
                f"Clean Code's naming guidance, consider a name describing its actual "
                f"responsibility (e.g. a Coordinator, Service, Repository, or Store) — "
                f"non-blocking, a well-scoped small class with this suffix can still be "
                f"the right call\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Patch-up fix instead of root-cause fix (hints) ──────────────────────────
# Non-blocking by design, same reasoning as the two hints above: "is this a real fix
# or a band-aid" is fundamentally a judgment call a regex can't fully make. What's
# mechanically detectable is two specific, well-known shapes of the pattern — an
# empty/log-only catch block (silences the symptom, never addresses why it threw) and
# an unjustified @Suppress (silences a real finding with no comment saying why it's a
# false positive vs a known, accepted gap). A TODO/FIXME/STOPSHIP left nearby (Detekt's
# own ForbiddenComment rule already flags these separately, active by default) is a
# real corroborating signal — noted in the finding when present, not required to fire.

_CATCH_BLOCK_RE = re.compile(r"\bcatch\s*\([^)]*\)\s*\{")
_LOG_ONLY_STATEMENT_RE = re.compile(r"^\s*(?:Log\.\w+|logger\.\w+|println|print)\s*\(")
_TODO_MARKER_RE = re.compile(r"\b(?:TODO|FIXME|STOPSHIP)\b")
_SUPPRESS_RE = re.compile(r'@Suppress\(\s*"([^"]+)"')


def _detect_empty_catch_block(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for match in _CATCH_BLOCK_RE.finditer(text):
            brace_open = text.index("{", match.start())
            brace_close = _find_matching_brace(text, brace_open)
            if brace_close is None:
                continue
            body = text[brace_open + 1: brace_close]
            body_lines = [ln for ln in body.splitlines() if ln.strip()]
            non_comment_lines = [ln for ln in body_lines if not ln.strip().startswith("//")]
            is_swallowed = not non_comment_lines or (
                len(non_comment_lines) == 1 and _LOG_ONLY_STATEMENT_RE.match(non_comment_lines[0])
            )
            if not is_swallowed:
                continue
            line_no, snippet = _at(text, match.start())
            todo_note = ""
            if _TODO_MARKER_RE.search(body):
                todo_note = " — a TODO/FIXME left in the block corroborates this is a known gap, not handled"
            findings.append(
                f"patch not root-cause fix (hint) [INFO]: {path.relative_to(root)}:{line_no} "
                f"— empty or log-only catch block; silences the symptom without "
                f"addressing why the exception was thrown{todo_note}. Non-blocking — a "
                f"deliberate best-effort no-op is sometimes genuinely correct, verify "
                f"manually\n"
                f"    {line_no} | {snippet}"
            )
    return findings


def _detect_unjustified_suppress(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
        except OSError:
            continue
        for i, line in enumerate(lines):
            match = _SUPPRESS_RE.search(line)
            if not match:
                continue
            same_line_comment = "//" in line[match.end():]
            prev_line_comment = i > 0 and lines[i - 1].strip().startswith("//")
            if same_line_comment or prev_line_comment:
                continue  # has a nearby justification comment
            todo_note = ""
            if (i > 0 and _TODO_MARKER_RE.search(lines[i - 1])) or _TODO_MARKER_RE.search(line):
                todo_note = " — a nearby TODO/FIXME corroborates this is a known gap, not a verified false positive"
            findings.append(
                f"patch not root-cause fix (hint) [INFO]: {path.relative_to(root)}:{i + 1} "
                f"— @Suppress(\"{match.group(1)}\") with no comment explaining why it's a "
                f"false positive (vs silencing a real finding){todo_note}. Non-blocking — "
                f"add a one-line justification comment, or fix the underlying issue "
                f"instead of suppressing it\n"
                f"    {i + 1} | {line.strip()}"
            )
    return findings


# ── Raw HTTP bypassing an established Ktor client ──────────────────────────────
# Real bug: kmp-network-layer only checked for a module literally
# named :core:network. A new server module or feature under a different name found no
# match, and an agent defaulted to a hand-written raw HTTP call instead of reusing the
# project's actual (differently-named) Ktor client. This detector catches the result —
# raw platform HTTP APIs alongside an established NetworkResult<T>/safeRequest pattern —
# regardless of what the network module is actually called.

_ESTABLISHED_NETWORK_LAYER_RE = re.compile(r"\bNetworkResult<|\bsafeRequest\b")
_RAW_HTTP_API_RE = re.compile(
    r"\bHttpURLConnection\b|\.openConnection\(\)|\bNSURLSession\b|\bURLSession\.shared\b"
)


def _detect_raw_http_bypass(root: Path) -> list[str]:
    """Flag raw platform HTTP APIs (HttpURLConnection, NSURLSession, etc.) used
    anywhere in a project that already has an established Ktor client — detected by
    content (NetworkResult<T> / safeRequest), not by a fixed module path or name.

    Only fires when an established client signal exists elsewhere in the project;
    a project with no Ktor client at all is out of scope for this detector.
    """
    findings: list[str] = []
    has_established_client = False
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if _ESTABLISHED_NETWORK_LAYER_RE.search(text):
            has_established_client = True
            break
    if not has_established_client:
        return findings

    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        match = _RAW_HTTP_API_RE.search(text)
        if not match:
            continue
        line_no, snippet = _at(text, match.start())
        findings.append(
            f"raw http bypasses established ktor client [HIGH]: {path.relative_to(root)}:{line_no} "
            f"— this project already has an established Ktor client (NetworkResult<T>/"
            f"safeRequest found elsewhere) but this file uses a raw platform HTTP API "
            f"instead of reusing it. If this is a new server module or feature with a "
            f"different name, extend the existing client — never bypass it with a raw call\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── WHAT-comment inside a loop or conditional ───────────────────────────────────
# kmp-code-quality's inline-block rule: a // comment inside a loop or
# conditional should explain WHY, never WHAT — a WHAT comment is a sign the block should
# be extracted into a named function/variable instead. Heuristic (regex, not AST): flags
# a // comment starting with an action verb (Loop/Check/Calculate/...) attached to a
# for/while/if/when on the same or next line, unless a WHY-marker (workaround/hack/
# because/...) is present. False positives are possible — LOW severity, human review.

_WHAT_COMMENT_VERB_RE = re.compile(
    r"^(?:loop\s+through|iterate|check\s+if|skip\s+if|calculate|compute|build|create|"
    r"parse|convert|filter|sort|validate|update|increment|decrement|set|get|"
    r"return\s+early\s+if)\b",
    re.IGNORECASE,
)
_WHY_MARKER_RE = re.compile(
    r"\b(?:workaround|hack|why|because|bug|note:|fixme|todo)\b", re.IGNORECASE
)
_CONTROL_FLOW_KEYWORD_RE = re.compile(r"\b(?:for|while|if|when)\s*\(")


def _line_comment_index(line: str) -> int:
    """Index of the first `//` that actually starts a comment, or -1.

    A plain `line.find("//")` also matches the `//` inside a URL string literal, which
    produced findings pointing at lines with no comment on them at all — e.g.
    `val base = "https://build.example.com"` next to an `if` was reported as "this //
    comment narrates what the block does". Skips over double-quoted and single-quoted
    literals, honouring backslash escapes.

    Single-line scope only: a `//` inside a multi-line raw string (`\"\"\"`) still slips
    through, since this scans one line without cross-line state. That's a narrower gap
    than the URL case and hasn't been observed in practice.
    """
    i, n = 0, len(line)
    quote = ""
    while i < n:
        ch = line[i]
        if quote:
            if ch == "\\":
                i += 2
                continue
            if ch == quote:
                quote = ""
        elif ch in ('"', "'"):
            quote = ch
        elif ch == "/" and i + 1 < n and line[i + 1] == "/":
            return i
        i += 1
    return -1


def _detect_what_comment_in_control_flow(root: Path) -> list[str]:
    """Flag // comments that narrate WHAT a loop/conditional does instead of WHY.

    Heuristic only — matches a // comment starting with an action verb, attached to a
    for/while/if/when on the same or next line, with no WHY-marker present. Intended as
    a nudge for human review (or /clean-comments), not an auto-fix.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
        except OSError:
            continue
        for i, line in enumerate(lines):
            idx = _line_comment_index(line)
            if idx == -1:
                continue
            comment_text = line[idx + 2 :].strip()
            if not _WHAT_COMMENT_VERB_RE.match(comment_text):
                continue
            if _WHY_MARKER_RE.search(comment_text):
                continue
            code_part = line[:idx]
            next_line = lines[i + 1] if i + 1 < len(lines) else ""
            if not (
                _CONTROL_FLOW_KEYWORD_RE.search(code_part)
                or _CONTROL_FLOW_KEYWORD_RE.match(next_line.strip())
            ):
                continue
            findings.append(
                f"what-comment in control flow [LOW]: {path.relative_to(root)}:{i + 1} "
                f"— this // comment narrates what the block does; per "
                f"kmp-code-quality's inline-block rule, extract a named "
                f"function/variable instead so the code reads as its own explanation, or "
                f"keep the comment only if it's actually explaining a non-obvious why\n"
                f"    {i + 1} | {line.strip()}"
            )
    return findings


# ── Long stacked // comment block, no docs/reference pointer ────────────────────
# kmp-code-quality's Comment & KDoc Conventions table says a // block
# that "grows past ~4 lines" should be split: keep a one-sentence WHY inline, move the
# rest to docs/reference/ with a pointer comment. That rule was documented but never
# mechanically checked anywhere — a real, confirmed gap (no Detekt rule, no audit
# detector), found when a user reported still seeing long stacked // blocks in their
# project after this skill shipped.

_COMMENT_LINE_RE = re.compile(r"^\s*//")
_DOCS_REFERENCE_POINTER_RE = re.compile(r"docs/reference/")
_LONG_COMMENT_BLOCK_MIN_LINES = 5

# A long block explaining genuine WHY (a hardware/API constraint, a workaround, a
# non-obvious ordering requirement) reads differently than one just narrating WHAT —
# it uses causal/justifying language. This can't be a precise classifier (it's a
# keyword heuristic, not real language understanding), but it targets a real,
# confirmed false-positive class: dense native/graphics rendering code (Vulkan/
# WebGPU/OpenGL pipeline setup) legitimately needs long inline WHY explanations,
# and moving them to docs/reference/ would separate the reasoning from the exact
# code it explains. Require 2+ distinct signal words, not just one incidental
# "must", to avoid exempting a block that only glancingly uses one of these words.
_WHY_SIGNAL_RE = re.compile(
    r"\b(because|workaround|due to|otherwise|must be|required by|"
    r"constraint|spec(?:ification)?|per the|note:|see spec|"
    r"driver|hardware|non-obvious)\b",
    re.IGNORECASE,
)
_WHY_SIGNAL_MIN_MATCHES = 2


def _detect_long_stacked_comment_block(root: Path) -> list[str]:
    """Flag a run of 5+ consecutive // comment lines with no docs/reference/ pointer —
    the exact shape kmp-code-quality's own rule says to split, made
    mechanically checkable instead of relying on human review to remember the rule.

    Exempts a block that reads as a genuine WHY explanation (2+ causal/justifying
    signal words — "because", "workaround", "driver", "constraint", etc.) rather than
    WHAT-narration — a real false-positive class found in dense native/graphics
    rendering code (Vulkan/WebGPU/OpenGL pipeline setup), where a long inline WHY
    explanation is often warranted, not lazy.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
        except OSError:
            continue

        block_start: int | None = None
        block_lines: list[str] = []

        def flush(end_index: int) -> None:
            nonlocal block_start, block_lines
            if block_start is not None and len(block_lines) >= _LONG_COMMENT_BLOCK_MIN_LINES:
                # A comment block that's the very first thing in the file (only blank
                # lines before it, if any) is a license/copyright header, not the
                # inline-WHY-comment-grew-too-long problem this rule targets — skip it.
                is_file_header = all(not lines[j].strip() for j in range(block_start))
                joined = "\n".join(block_lines)
                why_signal_count = len(_WHY_SIGNAL_RE.findall(joined))
                is_likely_why_explanation = why_signal_count >= _WHY_SIGNAL_MIN_MATCHES
                if (
                    not is_file_header
                    and not is_likely_why_explanation
                    and not _DOCS_REFERENCE_POINTER_RE.search(joined)
                ):
                    findings.append(
                        f"long stacked comment block [LOW]: {path.relative_to(root)}:{block_start + 1} "
                        f"— {len(block_lines)} consecutive // lines with no docs/reference/ "
                        f"pointer; per kmp-code-quality's Comment & KDoc "
                        f"Conventions, keep the one-sentence WHY inline and move the rest to "
                        f"docs/reference/ with a pointer comment\n"
                        f"    {block_start + 1} | {block_lines[0].strip()}"
                    )
            block_start = None
            block_lines = []

        for i, line in enumerate(lines):
            if _COMMENT_LINE_RE.match(line):
                if block_start is None:
                    block_start = i
                block_lines.append(line)
            else:
                flush(i)
        flush(len(lines))

    return findings


# A justification comment above a single Gradle dependency/config line is a distinct
# smell from the long-stacked-block check above: it's often short enough (3-4 lines) to
# duck under _LONG_COMMENT_BLOCK_MIN_LINES, and it's usually WHY-shaped (real reasoning),
# so the WHY-signal exemption above would wave it through too. The tell isn't length or
# tone, it's proportion: 3+ lines justifying one dependency declaration. Confirmed real —
# a user reported an agent-written comment of this exact shape and asked for a mechanical
# backstop, not just the "put it in the commit message" rule in kmp-code-quality.
_SINGLE_LINE_DEPENDENCY_STATEMENT_RE = re.compile(
    r"^\s*(?:implementation|api|compileOnly|runtimeOnly|ksp|kapt|"
    r"testImplementation|androidTestImplementation|debugImplementation)\([^()]*\)\s*$"
)
_JUSTIFICATION_COMMENT_MIN_LINES = 3


def _detect_justification_comment_above_single_statement(root: Path) -> list[str]:
    """Flag a 3+ line // comment block directly above one single-line Gradle dependency
    declaration — even a real, non-obvious reason doesn't need a paragraph to justify
    adding one line; that reasoning belongs in the commit message, discoverable via
    git blame exactly when someone needs it, not sitting in the file for every reader.
    """
    findings: list[str] = []
    for path in root.rglob("*.gradle.kts"):
        if _is_excluded(path, root):
            continue
        try:
            lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
        except OSError:
            continue

        block_start: int | None = None
        block_lines: list[str] = []
        for i, line in enumerate(lines):
            if _COMMENT_LINE_RE.match(line):
                if block_start is None:
                    block_start = i
                block_lines.append(line)
                continue
            if (
                block_start is not None
                and len(block_lines) >= _JUSTIFICATION_COMMENT_MIN_LINES
                and _SINGLE_LINE_DEPENDENCY_STATEMENT_RE.match(line)
            ):
                findings.append(
                    f"justification comment above single statement [LOW]: "
                    f"{path.relative_to(root)}:{block_start + 1} "
                    f"— {len(block_lines)} consecutive // lines justifying one dependency "
                    f"line; per kmp-code-quality's Comment & KDoc Conventions, put the "
                    f"reasoning in the commit message instead, unless it's a gotcha a "
                    f"future maintainer will independently re-trip on\n"
                    f"    {i + 1} | {line.strip()}"
                )
            block_start = None
            block_lines = []

    return findings


# ── Destructive-read accessor (single-writer snapshot anti-pattern) ────────────
# kmp-code-quality's Side-Effect-Free Accessors rule: a getter/consume
# function must never mutate shared state as a side effect of being read — a second
# caller in the same tick/request silently sees the already-cleared value. Heuristic-only:
# matches the exact "read field into local, clear that same field, return the local"
# 3-line shape — the real bug shape found (and fixed) in awaken's Input.consumeTypedText()/
# consumeEditActions(), before they were replaced by a single owned snapshot() call.

_DESTRUCTIVE_READ_LOCAL_RE = re.compile(r"^\s*val\s+(\w+)\s*=\s*(\w+)(?:\.\w+\(\))?\s*$")
_DESTRUCTIVE_READ_CLEAR_RE = re.compile(
    r'^\s*(\w+)\s*(?:=\s*(?:0f?|""|null|false|emptyList\(\)|emptySet\(\))|\.clear\(\))\s*$'
)
_DESTRUCTIVE_READ_RETURN_RE = re.compile(r"^\s*return\s+(\w+)\s*$")


def _detect_destructive_read_accessor(root: Path) -> list[str]:
    """Flag a function that reads a field into a local, clears that same field, then
    returns the local — fine with exactly one caller, but breaks silently the moment a
    second caller reads the same accessor in the same tick/request.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
        except OSError:
            continue
        for i in range(len(lines) - 2):
            local_match = _DESTRUCTIVE_READ_LOCAL_RE.match(lines[i])
            if not local_match:
                continue
            local_name, field_name = local_match.groups()
            clear_match = _DESTRUCTIVE_READ_CLEAR_RE.match(lines[i + 1])
            if not clear_match or clear_match.group(1) != field_name:
                continue
            return_match = _DESTRUCTIVE_READ_RETURN_RE.match(lines[i + 2])
            if not return_match or return_match.group(1) != local_name:
                continue
            findings.append(
                f"destructive read accessor [MEDIUM]: {path.relative_to(root)}:{i + 1} "
                f"— reads '{field_name}' into a local then clears it before returning; a "
                f"second caller in the same tick/request sees the already-cleared value. "
                f"Per kmp-code-quality's Side-Effect-Free Accessors rule, "
                f"expose a single snapshot()/drain() owned by one caller instead\n"
                f"    {i + 1} | {lines[i].strip()}"
            )
    return findings


# ── Value class opportunity (2+ raw String/Long ID params in one signature) ─────
# kmp-clean-architecture's "Typed Domain IDs" rule: nothing stops
# getOrder(userId, orderId) from compiling when both are raw String. This is an
# opportunity nudge, not a misuse flag — the code isn't wrong, it's just missing a
# cheap compile-time guardrail. Heuristic-only: matches parameter names ending in
# "Id" (case-insensitive) typed as String or Long within the same function signature.

_FUN_SIGNATURE_RE = re.compile(r"\bfun\s+(?:<[^>]*>\s*)?[\w.]*\s*\(([^)]*)\)", re.DOTALL)
_ID_PARAM_RE = re.compile(r"(?:^|,)\s*(?:vararg\s+)?(\w*[Ii]d)\s*:\s*(String|Long)\b")


def _split_top_level(param_str: str) -> list[str]:
    """Split a parameter list on top-level commas only — doesn't break on commas
    inside generic type arguments (Map<String, Int>) or default-value lambdas."""
    parts: list[str] = []
    depth = 0
    current: list[str] = []
    for ch in param_str:
        if ch in "<([":
            depth += 1
        elif ch in ">)]":
            depth -= 1
        if ch == "," and depth == 0:
            parts.append("".join(current))
            current = []
        else:
            current.append(ch)
    if current:
        parts.append("".join(current))
    return parts


def _detect_value_class_opportunity(root: Path) -> list[str]:
    """Flag a function signature with 2+ String/Long parameters whose names end in
    'Id' — the exact shape that lets a caller pass them in the wrong order and still
    compile. Nudge toward kmp-clean-architecture's value class rule.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for match in _FUN_SIGNATURE_RE.finditer(text):
            params = _split_top_level(match.group(1))
            id_params = [
                m.group(1)
                for p in params
                if (m := _ID_PARAM_RE.search("," + p))
            ]
            if len(id_params) < 2:
                continue
            line_no, snippet = _at(text, match.start())
            findings.append(
                f"value class opportunity [LOW]: {path.relative_to(root)}:{line_no} "
                f"— {len(id_params)} raw String/Long ID parameters ({', '.join(id_params)}) "
                f"in one signature; nothing stops them being passed in the wrong order. "
                f"Per kmp-clean-architecture's Typed Domain IDs rule, "
                f"wrap each in a @JvmInline value class\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Context parameter opportunity (same param repeated across many signatures) ──
# kmp-dependency-injection's Context Parameters section: a value
# threaded through many function signatures in the same file (a logger, a session)
# that isn't actually each function's job is a context-parameter candidate. Heuristic
# and lower-confidence than the value-class check — a repeated parameter name/type
# pair is a much weaker signal than a directly-observed bug shape, so this stays LOW
# severity and is explicitly a nudge, not a claim the code is wrong.

_CONTEXT_PARAM_OPPORTUNITY_MIN_COUNT = 5


def _detect_context_parameter_opportunity(root: Path) -> list[str]:
    """Flag a (name, type) parameter pair repeated across 5+ function signatures in
    the same file — a candidate for Kotlin 2.4's context parameters instead of
    threading the same explicit parameter through every function.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        counts: dict[tuple[str, str], int] = {}
        first_line: dict[tuple[str, str], int] = {}
        for match in _FUN_SIGNATURE_RE.finditer(text):
            for p in _split_top_level(match.group(1)):
                pm = re.match(r"\s*(?:vararg\s+)?(\w+)\s*:\s*([\w.]+)", p)
                if not pm:
                    continue
                key = (pm.group(1), pm.group(2))
                counts[key] = counts.get(key, 0) + 1
                if key not in first_line:
                    first_line[key] = _at(text, match.start())[0]
        for (name, type_name), count in counts.items():
            if count < _CONTEXT_PARAM_OPPORTUNITY_MIN_COUNT:
                continue
            line_no = first_line[(name, type_name)]
            findings.append(
                f"context parameter opportunity [LOW]: {path.relative_to(root)}:{line_no} "
                f"— '{name}: {type_name}' repeated as an explicit parameter across "
                f"{count} function signatures in this file. Per "
                f"kmp-dependency-injection's Context Parameters "
                f"section, consider threading it implicitly via context(...) instead "
                f"— only if it's a cross-cutting value, not part of each function's "
                f"actual job\n"
                f"    {line_no} | first appears here"
            )
    return findings


# ── Extensible abstract class in commonMain ─────────────────────────────────────
# commonMain APIs should be called or composed, not extended. An abstract class with
# only abstract members (no concrete implementation at all) forces every consumer into
# an inheritance chain the commonMain code itself dictates — the same shape Detekt's real
# AbstractClassCanBeInterface rule already flags ("should be an interface instead"), scoped
# here specifically to commonMain since that's where KMP's sharing advantage is lost by
# reaching for inheritance instead of interface + injection. Not scoped to any domain
# name (games, network clients, plugin systems, ...) — the smell is the shape, not the
# name. Deliberately excludes abstract classes with at least one concrete member (a real
# template-method pattern with genuinely shared logic is not this smell).

_ABSTRACT_CLASS_RE = re.compile(r"\babstract\s+class\s+(\w+)\b[^{]*\{")
_CONCRETE_FUN_BODY_RE = re.compile(r"\bfun\s+\w+\s*\([^)]*\)[^{;]*\{")
_CONCRETE_PROPERTY_RE = re.compile(r"\b(?:val|var)\s+\w+[^=\n]*=")
_ABSTRACT_MEMBER_RE = re.compile(r"\babstract\s+(?:fun|val|var)\b")


def _is_commonmain_path(path: Path) -> bool:
    return "commonmain" in path.as_posix().lower()


_HARDCODED_URL_RE = re.compile(
    r'^\s*(?:public\s+)?(?:const\s+)?val\s+\w+\s*(?::\s*String)?\s*=\s*"(https?://[^"]+)"',
    re.MULTILINE,
)


def _detect_hardcoded_base_url(root: Path) -> list[str]:
    """Flag a literal http(s):// URL assigned to a val/const val in commonMain source.

    A library-first module must be configurable per environment (dev/staging/prod) —
    see kmp-flavor-environment's BuildKonfig + AppConfig pattern. A
    URL baked in as a string literal builds and runs fine today, then becomes tech
    debt the moment a second environment (or a library consumer) needs a different
    endpoint — silent until that day, unlike a compile error.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        if not _is_commonmain_path(path):
            continue
        if "buildkonfig" in path.name.lower():
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for m in _HARDCODED_URL_RE.finditer(text):
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"hardcoded base URL [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— literal URL assigned directly instead of routed through "
                f"BuildKonfig/AppConfig; breaks the moment a second environment or a "
                f"library consumer needs a different endpoint (see "
                f"kmp-flavor-environment)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


def _detect_extensible_abstract_class_in_common(root: Path) -> list[str]:
    """Flag a public abstract class in commonMain with only abstract members — the
    exact shape Detekt's real AbstractClassCanBeInterface rule flags as "should be an
    interface instead," scoped here to commonMain specifically.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        if not _is_commonmain_path(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for match in _ABSTRACT_CLASS_RE.finditer(text):
            class_name = match.group(1)
            body_start = match.end()
            depth = 1
            i = body_start
            while i < len(text) and depth > 0:
                if text[i] == "{":
                    depth += 1
                elif text[i] == "}":
                    depth -= 1
                i += 1
            body = text[body_start : i - 1]
            has_abstract_member = bool(_ABSTRACT_MEMBER_RE.search(body))
            has_concrete_member = bool(_CONCRETE_FUN_BODY_RE.search(body)) or bool(
                _CONCRETE_PROPERTY_RE.search(body)
            )
            if has_abstract_member and not has_concrete_member:
                line_no, snippet = _at(text, match.start())
                findings.append(
                    f"extensible abstract class in commonMain [MEDIUM]: "
                    f"{path.relative_to(root)}:{line_no} — `{class_name}` has only "
                    f"abstract members, forcing every consumer to subclass it. "
                    f"commonMain APIs should be called/composed, not extended — replace "
                    f"with an interface consumers implement and inject (see "
                    f"kmp-clean-architecture's Composition Over "
                    f"Inheritance section)\n"
                    f"    {line_no} | {snippet}"
                )
    return findings


# ── Module layer-order violation ────────────────────────────────────────────────
# A module can declare a wrong-direction Gradle dependency (e.g. :ui directly on :data,
# skipping :presenter) without that ever forming a literal cycle — Gradle happily builds
# it, so nothing catches it there. The existing Detekt import-boundary rules only check
# file-level imports (does a .kt file literally import *.data.*), which can miss a
# module-level violation declared in build.gradle.kts before any file uses it yet. This
# detector parses the real Gradle module graph directly, independent of Detekt.

_LAYER_ORDER = ("model", "api", "domain", "data", "presenter", "ui")
_ALLOWED_DEPS = {
    "model": set(),
    "api": {"model"},
    "domain": {"api", "model"},
    "data": {"api", "model"},
    "presenter": {"domain", "api", "model"},
    "ui": {"presenter"},
}
_PROJECT_DEP_RE = re.compile(
    r"\b(?:implementation|api)\s*\(\s*projects\.([\w.]+)\s*\)"
)
_MODULE_PATH_RE = re.compile(r"^feature[\\/](\w+)[\\/](\w+)$")


def _module_key_from_gradle_ref(ref: str) -> str:
    """Convert a projects.feature.auth.domain reference to feature/auth/domain."""
    return ref.replace(".", "/")


def _detect_module_layer_violation(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("build.gradle.kts"):
        if _is_excluded(path, root):
            continue
        module_dir = path.parent.relative_to(root).as_posix()
        source_match = _MODULE_PATH_RE.match(module_dir)
        if not source_match:
            continue
        source_feature, source_layer = source_match.groups()
        if source_layer not in _LAYER_ORDER:
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for match in _PROJECT_DEP_RE.finditer(text):
            target_key = _module_key_from_gradle_ref(match.group(1))
            target_match = _MODULE_PATH_RE.match(target_key)
            if not target_match:
                continue  # core/* or another non-6-layer module — out of scope
            target_feature, target_layer = target_match.groups()
            if target_layer not in _LAYER_ORDER:
                continue
            line_no, snippet = _at(text, match.start())
            if target_feature != source_feature:
                findings.append(
                    f"cross-feature module dependency [HIGH]: {path.relative_to(root)}:"
                    f"{line_no} — {source_feature}/{source_layer} depends directly on "
                    f"{target_feature}/{target_layer}; cross-feature calls should go "
                    f"through a :core:api contract, not a direct feature-to-feature "
                    f"module dependency\n    {line_no} | {snippet}"
                )
                continue
            if target_layer not in _ALLOWED_DEPS.get(source_layer, set()):
                findings.append(
                    f"module layer-order violation [HIGH]: {path.relative_to(root)}:"
                    f"{line_no} — {source_feature}/{source_layer} depends directly on "
                    f"{source_feature}/{target_layer}, violating the 6-layer contract "
                    f"(:model ← :api ← :domain ← :data, :domain ← "
                    f":presenter ← :ui). Declared at the Gradle module level — this "
                    f"can exist before any file even imports the forbidden package, so "
                    f"file-level Detekt import rules alone won't catch it\n"
                    f"    {line_no} | {snippet}"
                )
    return findings


# ── Bare :core module (not split into :core:model/:core:api/etc.) ──────────────
# kmp-clean-architecture's ":core vs :feature Split" documents :core
# as a folder GROUP of separate modules (:core:model, :core:api, :core:domain,
# :core:testing, :core:ui, ...), mirroring :feature:*'s own shape — never stated as
# an enforced rule. _detect_module_layer_violation's _MODULE_PATH_RE only matches
# feature/<name>/<layer> — it never applied to :core at all, so a monolithic :core
# module (root/core/build.gradle.kts, no further nesting) went uncaught.

def _detect_bare_core_module(root: Path) -> list[str]:
    """Flag a root/core/build.gradle.kts — :core must be a folder group of separate
    modules (:core:model, :core:api, ...), never a module in its own right.
    """
    findings: list[str] = []
    for core_dir_name in ("core", "shared/core"):
        candidate = root / core_dir_name / "build.gradle.kts"
        if _is_excluded(candidate, root) or not candidate.is_file():
            continue
        findings.append(
            f"bare core module [HIGH]: {candidate.relative_to(root)} — "
            f":core has its own build.gradle.kts, making it a single monolithic "
            f"module instead of a folder group. Per kmp-clean-"
            f"architecture's \":core\" vs \":feature\" Split, split into "
            f":core:model/:core:api/:core:domain/:core:testing/:core:ui (etc.), "
            f"mirroring :feature:*'s own shape — :core itself should have no "
            f"build.gradle.kts, only its named submodules do\n"
            f"    1 | {core_dir_name}/build.gradle.kts"
        )
    return findings


# ── Unauthorized module nested under :app:* ──────────────────────────────────
# kmp-wizard's real all-targets template (verified against the live repo, not
# assumed) nests exactly four modules under app/: androidApp, desktopApp, webApp
# (thin platform entry points) and shared (the CMP composition root) — plus a
# native, non-Gradle app/iosApp/ Xcode project. :core:*/:feature:* already own
# business logic and cross-feature infrastructure; a new module dropped directly
# under app/ duplicates that job and blurs the entry-point boundary kmp-wizard
# itself draws.

_KNOWN_APP_SUBMODULES = {"androidApp", "desktopApp", "webApp", "shared"}
_APP_SUBMODULE_RE = re.compile(r"^app[\\/](\w+)$")


def _detect_unauthorized_app_submodule(root: Path) -> list[str]:
    """Flag a build.gradle.kts under app/<name>/ where <name> isn't one of
    kmp-wizard's own four entry-point modules (androidApp/desktopApp/webApp/shared).
    """
    findings: list[str] = []
    for path in root.rglob("build.gradle.kts"):
        if _is_excluded(path, root):
            continue
        module_dir = path.parent.relative_to(root).as_posix()
        match = _APP_SUBMODULE_RE.match(module_dir)
        if not match:
            continue
        name = match.group(1)
        if name in _KNOWN_APP_SUBMODULES:
            continue
        findings.append(
            f"unauthorized app submodule [HIGH]: {path.relative_to(root)} — "
            f"a module was created directly under :app:*, but only kmp-wizard's own "
            f"four entry points (androidApp, desktopApp, webApp, shared) belong there. "
            f"New feature logic goes in :feature:<name>:*, new cross-feature "
            f"infrastructure goes in :core:* — never a new :app:<name> module\n"
            f"    1 | app/{name}/build.gradle.kts"
        )
    return findings


# ── Leftover kmp-wizard demo/placeholder code ────────────────────────────────
# kmp-wizard's real all-targets template ships app/shared with a working demo screen
# (a "Click me!" button revealing Greeting().greet() text over a Compose Multiplatform
# logo image) — verified against the live template, not assumed. It must be deleted
# once real feature work starts; left in place it ships to production as dead sample
# code and confuses anyone reading :app:shared expecting only composition-root wiring.

_WIZARD_DEMO_RE = re.compile(r"\bclass\s+Greeting\b|\bcompose_multiplatform\b")


def _detect_leftover_wizard_demo_code(root: Path) -> list[str]:
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        match = _WIZARD_DEMO_RE.search(text)
        if not match:
            continue
        line_no, snippet = _at(text, match.start())
        findings.append(
            f"leftover wizard demo code [HIGH]: {path.relative_to(root)}:{line_no} — "
            f"kmp-wizard's default Greeting/\"Compose Multiplatform\" logo demo is still "
            f"present; delete it before real feature work starts — :app:shared should "
            f"only ever hold composition-root wiring (App(), theme, Koin, NavHost)\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── Design system prefix mismatch ─────────────────────────────────────────────
# "App" in the design-system skill is a template placeholder (see Step 0) — real
# projects must substitute their resolved COMPONENT_PREFIX when generating files, not
# leave literal App* names on disk. This detector catches the case where a project has
# already resolved and recorded a different prefix but App*-named declarations still
# exist under core/designsystem — i.e. the substitution was skipped during generation.

_FRONTMATTER_RE = re.compile(r"\A---\r?\n(.*?)\r?\n---\r?\n", re.DOTALL)
_FRONTMATTER_NAME_RE = re.compile(r"^name:\s*\S", re.MULTILINE)
_FRONTMATTER_DESCRIPTION_RE = re.compile(r"^description:\s*\S", re.MULTILINE)
_SKILL_MD_MAX_LINES = 500


def _detect_project_skill_standards(root: Path) -> list[str]:
    """Flag a project-owned skill at <project root>/skills/<skill-name>/ that doesn't
    meet the real, official skill anatomy (verified against anthropic-skills:skill-creator's
    own documented convention, not assumed):

      - skills/<name>/SKILL.md must exist (a skill folder with none is undiscoverable)
      - SKILL.md must open with YAML frontmatter (--- ... ---)
      - frontmatter must have both name: and description: — these are the primary
        triggering mechanism; a skill missing either can't be found or won't trigger
      - SKILL.md body should stay under ~500 lines unless it points to a references/
        subdirectory for progressive disclosure (skill-creator's own stated guideline)

    Scoped to the project's own top-level skills/ directory only — not this collection's
    deployed .claude/skills/ copies, and not this repo's own skills/ when auditing itself.
    """
    findings: list[str] = []
    skills_dir = root / "skills"
    if not skills_dir.is_dir():
        return findings

    for skill_dir in sorted(p for p in skills_dir.iterdir() if p.is_dir()):
        if _is_excluded(skill_dir, root):
            continue
        rel_dir = skill_dir.relative_to(root)
        skill_md = skill_dir / "SKILL.md"

        if not skill_md.is_file():
            findings.append(
                f"project skill missing SKILL.md [HIGH]: {rel_dir} — a skill folder "
                f"needs SKILL.md to be discoverable at all (see anthropic-skills:skill-creator's "
                f"skill anatomy)"
            )
            continue

        try:
            text = skill_md.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue

        rel = skill_md.relative_to(root)
        fm_match = _FRONTMATTER_RE.match(text)
        if not fm_match:
            findings.append(
                f"project skill missing frontmatter [HIGH]: {rel} — SKILL.md must open "
                f"with a --- ... --- YAML frontmatter block containing name and description"
            )
            continue

        frontmatter = fm_match.group(1)
        if not _FRONTMATTER_NAME_RE.search(frontmatter):
            findings.append(
                f"project skill frontmatter missing name [HIGH]: {rel} — name is the "
                f"skill identifier; without it the skill can't be reliably referenced"
            )
        if not _FRONTMATTER_DESCRIPTION_RE.search(frontmatter):
            findings.append(
                f"project skill frontmatter missing description [HIGH]: {rel} — "
                f"description is the primary triggering mechanism; a skill without one "
                f"won't reliably trigger for the tasks it's meant to handle"
            )

        body = text[fm_match.end():]
        body_lines = body.count("\n") + 1
        if body_lines > _SKILL_MD_MAX_LINES and not (skill_dir / "references").is_dir():
            findings.append(
                f"project skill exceeds 500-line guideline [MEDIUM]: {rel} — body is "
                f"~{body_lines} lines with no references/ subdirectory; skill-creator's "
                f"own guideline is to add a references/ layer with clear pointers once "
                f"approaching this size, not to let SKILL.md itself keep growing"
            )

    return findings


def _detect_project_skill_deployment_drift(root: Path) -> list[str]:
    """Flag project-owned skills under ./skills/ that were never deployed or drifted
    from their deployed `.claude/skills/` copies.

    Project-owned custom skills are authored at the repo root and then copied into the
    assistant runtime directory. If the deployed copy is missing or stale, Claude loads
    behavior that no longer matches the source of truth.
    """
    findings: list[str] = []
    skills_dir = root / "skills"
    deployed_skills_dir = root / ".claude" / "skills"
    if not skills_dir.is_dir():
        return findings

    for skill_dir in sorted(p for p in skills_dir.iterdir() if p.is_dir()):
        if _is_excluded(skill_dir, root):
            continue
        source_skill_md = skill_dir / "SKILL.md"
        if not source_skill_md.is_file():
            continue

        deployed_dir = deployed_skills_dir / skill_dir.name
        deployed_skill_md = deployed_dir / "SKILL.md"
        rel = skill_dir.relative_to(root).as_posix()
        if not deployed_skill_md.is_file():
            findings.append(
                f"project skill not deployed [MEDIUM]: {rel} — deploy it to "
                f".claude/skills/{skill_dir.name}/ so Claude loads the project-owned copy"
            )
            continue

        source_files: dict[str, str] = {}
        deployed_files: dict[str, str] = {}
        for path in skill_dir.rglob("*"):
            if path.is_file():
                source_files[path.relative_to(skill_dir).as_posix()] = path.read_text(encoding="utf-8", errors="ignore")
        for path in deployed_dir.rglob("*"):
            if path.is_file():
                deployed_files[path.relative_to(deployed_dir).as_posix()] = path.read_text(encoding="utf-8", errors="ignore")

        if set(source_files) != set(deployed_files):
            findings.append(
                f"project skill deployment drift [MEDIUM]: {rel} — deployed file set in "
                f".claude/skills/{skill_dir.name}/ does not match the project-owned source"
            )
            continue

        for rel_file, source_text in source_files.items():
            if deployed_files.get(rel_file) != source_text:
                findings.append(
                    f"project skill deployment drift [MEDIUM]: {rel}/{rel_file} — "
                    f"deployed copy under .claude/skills/{skill_dir.name}/ is stale"
                )
                break

    return findings


# ── Project-owned agent file standards + deployment drift ───────────────────────
# Mirrors _detect_project_skill_standards/_detect_project_skill_deployment_drift for
# agents/*.md (Claude-style source) and .codex/agents/*.toml (Codex's real, verified
# format — name/description/developer_instructions required, per Codex's own docs).
# Also catches the exact real bug found and fixed in docs/reference/agent-catalog.md
# this same session: a tier name (flagship-coding/balanced-coding/fast-utility/
# precision-review) written into `model:` instead of a real, resolvable model id —
# Claude Code has no concept of a tier name and the agent's model won't resolve.

_TIER_NAME_LITERALS = {"flagship-coding", "balanced-coding", "fast-utility", "precision-review"}
_AGENT_MODEL_FIELD_RE = re.compile(r"^model:\s*['\"]?([\w.-]+)['\"]?\s*$", re.MULTILINE)
_TOML_KEY_RE = {
    "name": re.compile(r'^\s*name\s*=\s*["\'].+["\']', re.MULTILINE),
    "description": re.compile(r'^\s*description\s*=\s*["\'].+["\']', re.MULTILINE),
    "developer_instructions": re.compile(r'^\s*developer_instructions\s*=', re.MULTILINE),
}


def _detect_agent_file_standards(root: Path) -> list[str]:
    """Flag a project-owned agent file (agents/*.md or .codex/agents/*.toml) that
    doesn't meet its real, verified format requirements, or a tier-name literal
    written into a Markdown agent's model: field instead of a real model id.
    """
    findings: list[str] = []

    agents_dir = root / "agents"
    if agents_dir.is_dir():
        for agent_md in sorted(agents_dir.glob("*.md")):
            if _is_excluded(agent_md, root):
                continue
            try:
                text = agent_md.read_text(encoding="utf-8", errors="ignore")
            except OSError:
                continue
            rel = agent_md.relative_to(root)
            fm_match = _FRONTMATTER_RE.match(text)
            if not fm_match:
                findings.append(
                    f"project agent missing frontmatter [HIGH]: {rel} — agent files "
                    f"need a --- ... --- YAML frontmatter block with name and description"
                )
                continue
            frontmatter = fm_match.group(1)
            if not _FRONTMATTER_NAME_RE.search(frontmatter):
                findings.append(
                    f"project agent frontmatter missing name [HIGH]: {rel}"
                )
            if not _FRONTMATTER_DESCRIPTION_RE.search(frontmatter):
                findings.append(
                    f"project agent frontmatter missing description [HIGH]: {rel}"
                )
            model_match = _AGENT_MODEL_FIELD_RE.search(frontmatter)
            if model_match and model_match.group(1) in _TIER_NAME_LITERALS:
                findings.append(
                    f"project agent uses tier name as model [HIGH]: {rel} — "
                    f"model: {model_match.group(1)} is a provider-neutral tier name, "
                    f"not a real model id; look up the real id for this tier in "
                    f"docs/reference/agent-catalog.md's Mapping Rule table (see "
                    f"kmp-expert's Project-Specific Commands/Agents/"
                    f"Skills section)"
                )

    codex_agents_dir = root / ".codex" / "agents"
    if codex_agents_dir.is_dir():
        # .codex is normally in _EXCLUDED_DIRS (avoids scanning deployed skill bundle
        # templates as if they were real project code) — that exclusion doesn't apply
        # here since .codex/agents/*.toml is exactly this detector's real target.
        for agent_toml in sorted(codex_agents_dir.glob("*.toml")):
            try:
                text = agent_toml.read_text(encoding="utf-8", errors="ignore")
            except OSError:
                continue
            rel = agent_toml.relative_to(root)
            missing = [key for key, pattern in _TOML_KEY_RE.items() if not pattern.search(text)]
            if missing:
                findings.append(
                    f"codex agent missing required field(s) [HIGH]: {rel} — "
                    f"{', '.join(missing)}; Codex CLI requires name, description, and "
                    f"developer_instructions on every subagent TOML file"
                )

    return findings


def _detect_agent_deployment_drift(root: Path) -> list[str]:
    """Flag a project-owned agents/*.md file that was never deployed to
    .claude/agents/, or has drifted from its deployed copy — same pattern as
    _detect_project_skill_deployment_drift.
    """
    findings: list[str] = []
    agents_dir = root / "agents"
    deployed_dir = root / ".claude" / "agents"
    if not agents_dir.is_dir():
        return findings

    for agent_md in sorted(agents_dir.glob("*.md")):
        if _is_excluded(agent_md, root):
            continue
        deployed_md = deployed_dir / agent_md.name
        rel = agent_md.relative_to(root).as_posix()
        if not deployed_md.is_file():
            findings.append(
                f"project agent not deployed [MEDIUM]: {rel} — deploy it to "
                f".claude/agents/{agent_md.name} so Claude loads the project-owned copy"
            )
            continue
        try:
            source_text = agent_md.read_text(encoding="utf-8", errors="ignore")
            deployed_text = deployed_md.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if source_text != deployed_text:
            findings.append(
                f"project agent deployment drift [MEDIUM]: {rel} — deployed copy "
                f"under .claude/agents/{agent_md.name} is stale"
            )

    return findings


# ── Mixed component library: ShadcnTheme + AppTheme both in real use ────────────
# kmp-shadcn-compose's SKILL.md says "Never combine with
# kmp-compose-design-system" — documented but never mechanically checked
# anywhere. Scoped to the two theme wrappers (ShadcnTheme/AppTheme call sites) rather than
# individual App*-prefixed component names: a generic `App[A-Z]\w*(` call-site pattern
# would false-positive on unrelated real identifiers (AppConfig(...), AppDatabase(...),
# etc.) that have nothing to do with either design system. The theme wrapper is the one
# call every screen in a project realistically makes, making it the highest-confidence,
# lowest-false-positive signal that both systems are genuinely wired into the same
# project rather than just mentioned in passing (e.g. a comment, a migration doc).

# Both theme wrappers' last param is a trailing content lambda with every other param
# defaulted, so real usage is commonly a parenthesis-free trailing-lambda call
# (`AppTheme { ... }`), not just `AppTheme(...)` — matching only "(" missed this shape.
_SHADCN_THEME_CALL_RE = re.compile(r"\bShadcnTheme\s*[({]")
_APP_THEME_CALL_RE = re.compile(r"\bAppTheme\s*[({]")


def _detect_mixed_design_system_usage(root: Path) -> list[str]:
    """Flag a project that calls both ShadcnTheme(...) and AppTheme(...) in real
    (non-test, non-excluded) source — the two systems are documented as mutually
    exclusive alternatives, never meant to coexist in the same project.
    """
    shadcn_files: list[Path] = []
    app_theme_files: list[Path] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if _SHADCN_THEME_CALL_RE.search(text):
            shadcn_files.append(path)
        if _APP_THEME_CALL_RE.search(text):
            app_theme_files.append(path)

    if not shadcn_files or not app_theme_files:
        return []

    shadcn_example = shadcn_files[0].relative_to(root)
    app_example = app_theme_files[0].relative_to(root)
    return [
        f"mixed component library usage [HIGH]: project calls both ShadcnTheme(...) "
        f"(e.g. {shadcn_example}) and AppTheme(...) (e.g. {app_example}) in real source "
        f"— kmp-shadcn-compose and kmp-compose-design-system "
        f"are documented mutually-exclusive alternatives, never meant to coexist. If the "
        f"project has genuinely migrated to shadcn-compose, finish the migration "
        f"(/kmp-migrate-to-shadcn) and remove the remaining AppTheme/App* usage rather "
        f"than leaving both wired in; if design-system is still the intended system, "
        f"remove the shadcn-compose dependency instead."
    ]


_COMPONENT_PREFIX_ROW_RE = re.compile(r"\|\s*Component prefix\s*\|\s*([A-Za-z][A-Za-z0-9]*)\s*\|")
_APP_PREFIXED_DECL_RE = re.compile(
    r"\b(?:class|fun|object|val|data class|sealed interface|enum class)\s+(App[A-Z]\w*)"
)


def _detect_design_system_prefix_mismatch(root: Path) -> list[str]:
    """Flag literal App*-prefixed declarations under core/designsystem when the project
    has already resolved and recorded a different COMPONENT_PREFIX in docs/design-system.md.

    Only fires when a resolved prefix is on record and it is not "App" itself — a project
    that genuinely chose "App" as its prefix is not a mismatch. Scoped to designsystem
    paths to avoid flagging unrelated App-prefixed identifiers elsewhere in the project.
    """
    doc = root / "docs" / "design-system.md"
    if not doc.exists():
        return []
    try:
        doc_text = doc.read_text(encoding="utf-8", errors="ignore")
    except OSError:
        return []
    m = _COMPONENT_PREFIX_ROW_RE.search(doc_text)
    if not m:
        return []
    resolved_prefix = m.group(1)
    if resolved_prefix in ("COMPONENT_PREFIX", "App"):
        return []  # unfilled placeholder, or the project genuinely chose "App"

    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        if "designsystem" not in path.as_posix().lower():
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        decl_match = _APP_PREFIXED_DECL_RE.search(text)
        if not decl_match:
            continue
        line_no, snippet = _at(text, decl_match.start())
        findings.append(
            f"design system prefix mismatch [HIGH]: {path.relative_to(root)}:{line_no} "
            f"— '{decl_match.group(1)}' uses the literal 'App' placeholder, but "
            f"docs/design-system.md records COMPONENT_PREFIX '{resolved_prefix}'; generate "
            f"with the resolved prefix directly instead of leaving App* names to rename "
            f"later (see design-system skill → Step 0)\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── Empty platform-specific source sets ───────────────────────────────────────
# KMP convention plugins register android/iOS/JVM/JS targets even for pure-common
# layers (:model, :api, :domain) because Gradle needs a compiled artifact per target —
# that's normal and required. The smell is a physical androidMain/iosMain/jvmMain/...
# *source directory* that was scaffolded on disk with no real content: either zero .kt
# files, or files containing nothing but a package declaration/imports/comments. An
# empty source set compiles fine without existing at all — the directory is pure
# clutter and signals unclear architecture intent ("why does :domain have an iosMain?").

_PLATFORM_MAIN_DIR_RE = re.compile(
    r"^(?:android|ios(?:Arm64|SimulatorArm64|X64)?|jvm|js|wasmJs|native|desktop|"
    r"macos|linux|mingw)Main$"
)
_KOTLIN_COMMENT_OR_TRIVIAL_RE = re.compile(
    r"^\s*(?://.*|/\*.*?\*/|package\s+[\w.]+|import\s+[\w.*]+)?\s*$",
    re.MULTILINE,
)


def _is_trivial_kotlin_file(text: str) -> bool:
    """True if a .kt file has no real declarations — only package/import/comments/blank
    lines. A block comment spanning multiple lines is not handled line-by-line here;
    good enough as a heuristic since real declarations reliably fail this check."""
    for line in text.splitlines():
        if not _KOTLIN_COMMENT_OR_TRIVIAL_RE.match(line):
            return False
    return True


def _detect_empty_platform_sourceset(root: Path) -> list[str]:
    """Flag a platform source set directory (androidMain/iosMain/jvmMain/...) that was
    scaffolded on disk but never given real content.

    Gradle does not require the directory to exist or contain files for a target to
    compile — an empty platform source set is unnecessary in every case. This is a
    lower-stakes cleanup finding (delete the directory, or add the real expect/actual
    code if there's a genuine platform need), not an architecture violation.
    """
    findings: list[str] = []
    seen_dirs: set[Path] = set()
    for kotlin_dir in root.rglob("src/*/kotlin"):
        sourceset_dir = kotlin_dir.parent
        if sourceset_dir in seen_dirs:
            continue
        seen_dirs.add(sourceset_dir)
        if not _PLATFORM_MAIN_DIR_RE.match(sourceset_dir.name):
            continue
        if _is_excluded(kotlin_dir, root):
            continue
        kt_files = [p for p in kotlin_dir.rglob("*.kt") if not _is_excluded(p, root)]
        if not kt_files:
            findings.append(
                f"empty platform source set [LOW]: {kotlin_dir.relative_to(root)} "
                f"— no .kt files; Gradle compiles this target fine without the directory "
                f"existing at all. Remove it, or add the real expect/actual code if this "
                f"module genuinely needs platform-specific logic"
            )
            continue
        non_trivial = [p for p in kt_files if not _is_trivial_kotlin_file(
            p.read_text(encoding="utf-8", errors="ignore")
        )]
        if not non_trivial:
            findings.append(
                f"empty platform source set [LOW]: {kotlin_dir.relative_to(root)} "
                f"— {len(kt_files)} file(s) contain only package/import/comments, no real "
                f"declarations; remove the directory or implement the platform code it "
                f"was scaffolded for"
            )
    return findings


# Stems that signal a top-level screen/page composable, across naming conventions.
_SCREEN_STEMS = ("Screen", "Content", "Page", "View", "Route")

# ── String-based (non-type-safe) navigation ───────────────────────────────────

# composable("home") or composable(route = "home") — string route in a NavHost.
# Type-safe form is composable<Route>{…}, which has no string first arg.
_STRING_COMPOSABLE_RE = re.compile(r'\bcomposable\s*\(\s*(?:route\s*=\s*)?"')
# startDestination = "home" — type-safe uses a route object (no quotes).
_STRING_START_DEST_RE = re.compile(r'\bstartDestination\s*=\s*"')
# navigate("home") — type-safe uses navigate(Route). A URI (contains "://") is a
# legitimate deep-link navigation, so those are excluded.
_STRING_NAVIGATE_RE = re.compile(r'\bnavigate\s*\(\s*"(?![^"]*://)')


def _detect_string_navigation(root: Path) -> list[str]:
    """Flag string-based (non-type-safe) Navigation Compose usage.

    The navigation skill mandates @Serializable type-safe routes
    (composable<Route>, navigate(Route), startDestination = Route). String routes lose
    compile-time destination/argument checking. Detected by the string-arg forms; the
    type-safe forms (which use type params or route objects) never match.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        forms = []
        anchor = None
        for rx, lbl in (
            (_STRING_COMPOSABLE_RE, 'composable("…")'),
            (_STRING_START_DEST_RE, 'startDestination = "…"'),
            (_STRING_NAVIGATE_RE, 'navigate("…")'),
        ):
            mm = rx.search(text)
            if mm:
                forms.append(lbl)
                if anchor is None:
                    anchor = mm
        if forms:
            line_no, snippet = _at(text, anchor.start())
            findings.append(
                f"string navigation [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— string-based routes ({', '.join(forms)}); switch to @Serializable "
                f"type-safe routes: composable<Route>, navigate(Route), "
                f"startDestination = Route (see navigation skill → type-safe routes)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Repository interface leaking data-layer types ─────────────────────────────

_REPO_INTERFACE_RE = re.compile(r"\binterface\s+\w*Repository\b")
_REPO_CLASS_RE = re.compile(r"\bclass\s+\w*Repository\b")
# A DTO or DB-entity type referenced by name (UserDto, ProductEntity, …).
_DATA_TYPE_TOKEN_RE = re.compile(r"\b\w+(?:Dto|Entity)\b")


def _detect_repository_leaks_data_type(root: Path) -> list[str]:
    """Flag a Repository *interface* that mentions DTO or DB-entity types.

    The repository interface belongs in :api and must speak domain types only — DTOs
    and DB entities never cross the interface boundary (they are mapped in :data). A
    `*Dto`/`*Entity` anywhere in an interface file is a boundary leak.

    Only interface files are checked — a `*RepositoryImpl` in :data legitimately uses
    DTOs/entities internally, so files declaring a Repository class are skipped.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _REPO_INTERFACE_RE.search(text):
            continue
        # Skip impl/combined files — the implementation may use DTOs/entities internally.
        if _REPO_CLASS_RE.search(text):
            continue
        leak = _DATA_TYPE_TOKEN_RE.search(text)
        if leak:
            line_no, snippet = _at(text, leak.start())
            findings.append(
                f"repository leaks data type [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— Repository interface references '{leak.group(0)}'; the interface must "
                f"speak domain types only. Return domain models (or Result<Domain>) and map "
                f"DTOs/entities in :data (see repository-pattern skill → Type Mapping)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Raw component bypassing the design system ─────────────────────────────────

# Raw Material/Foundation components that have an App* design-system equivalent.
_RAW_COMPONENT_MAP = {
    "Scaffold": "AppScaffold",
    "TopAppBar": "AppTopAppBar",
    "CenterAlignedTopAppBar": "AppTopAppBar",
    "Button": "AppButton",
    "OutlinedButton": "AppButton",
    "TextButton": "AppButton",
    "ElevatedButton": "AppButton",
    "FilledTonalButton": "AppButton",
    "Card": "AppCard",
    "ElevatedCard": "AppCard",
    "OutlinedCard": "AppCard",
    "TextField": "AppTextField",
    "OutlinedTextField": "AppTextField",
    "AlertDialog": "AppDialog",
    "ModalBottomSheet": "AppBottomSheet",
    "IconButton": "AppIconButton",
    "AssistChip": "AppChip",
    "FilterChip": "AppChip",
    "SuggestionChip": "AppChip",
    "InputChip": "AppChip",
    "Badge": "AppBadge",
}
# Longest names first so e.g. OutlinedButton matches before Button at the same position.
# Matches both call forms: Component( … ) and the trailing-lambda-only Component { … }.
_RAW_COMPONENT_RE = re.compile(
    r"\b(" + "|".join(sorted(map(re.escape, _RAW_COMPONENT_MAP), key=len, reverse=True)) + r")\s*[({]"
)
# Markers that a project HAS a design system (so raw components are a bypass, not a choice).
_DESIGN_SYSTEM_MARKER_RE = re.compile(r"\bAppTheme\b|\bfun\s+App[A-Z]\w*\s*\(")
# A file that DEFINES App* wrappers — legitimately uses raw primitives internally.
_APP_WRAPPER_DEF_RE = re.compile(r"\bfun\s+App[A-Z]")

# shadcn-compose subset — deliberately narrower than _RAW_COMPONENT_MAP. Verified against
# /kmp-migrate-to-shadcn's own Component Mapping Table: shadcn/ui is web-first and has no
# Scaffold/TopAppBar concept, so those stay raw Compose by design in a shadcn-compose
# project — including them here would produce a wrong "bypass" finding.
_SHADCN_RAW_COMPONENT_MAP = {
    "Button": "ShadcnButton",
    "OutlinedButton": "ShadcnButton",
    "TextButton": "ShadcnButton",
    "ElevatedButton": "ShadcnButton",
    "FilledTonalButton": "ShadcnButton",
    "Card": "ShadcnCard",
    "ElevatedCard": "ShadcnCard",
    "OutlinedCard": "ShadcnCard",
    "TextField": "ShadcnTextField",
    "OutlinedTextField": "ShadcnTextField",
    "AlertDialog": "ShadcnAlertDialog",
    "ModalBottomSheet": "ShadcnSheet",
    "Badge": "ShadcnBadge",
}
_SHADCN_RAW_COMPONENT_RE = re.compile(
    r"\b(" + "|".join(sorted(map(re.escape, _SHADCN_RAW_COMPONENT_MAP), key=len, reverse=True)) + r")\s*[({]"
)
_SHADCN_MARKER_RE = re.compile(r"\bShadcnTheme\b|\bfun\s+Shadcn[A-Z]\w*\s*\(")
_SHADCN_WRAPPER_DEF_RE = re.compile(r"\bfun\s+Shadcn[A-Z]")


def _project_design_system_kind(root: Path) -> str | None:
    """Return 'app' if the project has a generated/owned design system (AppTheme/App*
    wrappers), 'shadcn' if it has shadcn-compose wired (ShadcnTheme/Shadcn* wrappers),
    or None if neither is present. The two are documented as mutually exclusive
    alternatives — `_detect_mixed_design_system_usage` handles the case both fire.
    """
    has_app = False
    has_shadcn = False
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not has_app and _DESIGN_SYSTEM_MARKER_RE.search(text):
            has_app = True
        if not has_shadcn and _SHADCN_MARKER_RE.search(text):
            has_shadcn = True
        if has_app and has_shadcn:
            break
    if has_app:
        return "app"
    if has_shadcn:
        return "shadcn"
    return None


def _detect_raw_component_bypass(root: Path) -> list[str]:
    """Flag raw Material/Foundation components used where a design-system wrapper
    exists — either the generated/owned App* system or shadcn-compose's Shadcn*
    components, whichever the project actually has wired. Files that define wrappers,
    theme/token files, and previews are skipped — they legitimately build on raw
    primitives.
    """
    kind = _project_design_system_kind(root)
    if kind is None:
        return []

    if kind == "app":
        component_map, component_re, wrapper_def_re, skip_path_tokens = (
            _RAW_COMPONENT_MAP, _RAW_COMPONENT_RE, _APP_WRAPPER_DEF_RE,
            ("designsystem", "design-system"),
        )
    else:
        component_map, component_re, wrapper_def_re, skip_path_tokens = (
            _SHADCN_RAW_COMPONENT_MAP, _SHADCN_RAW_COMPONENT_RE, _SHADCN_WRAPPER_DEF_RE,
            ("designsystem", "design-system", "shadcn"),
        )

    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue
        # Skip the design-system definition layer, theme/token files, and previews.
        if wrapper_def_re.search(text):
            continue
        if any(p in path.stem for p in ("Theme", "theme", "Token", "token", "Preview")):
            continue
        if any(t in path.as_posix() for t in skip_path_tokens):
            continue

        found: dict[str, str] = {}
        anchor = None
        for m in component_re.finditer(text):
            raw = m.group(1)
            found.setdefault(raw, component_map[raw])
            if anchor is None:
                anchor = m
        if found:
            line_no, snippet = _at(text, anchor.start())
            mapping = ", ".join(f"{r}→{a}" for r, a in list(found.items())[:5])
            wrapper_skill = "shadcn-compose" if kind == "shadcn" else "design-system"
            findings.append(
                f"raw component bypass [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— raw components instead of design-system wrappers ({mapping}); use the "
                f"{'Shadcn*' if kind == 'shadcn' else 'App*'} components so styling/tokens "
                f"stay consistent (see {wrapper_skill} skill)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Hand-written ImageVector path data ────────────────────────────────────────

_IMAGEVECTOR_BUILDER_RE = re.compile(r"\bImageVector\.Builder\s*\(|\bmaterialIcon\s*\(")
_PATH_CMD_RE = re.compile(r"\b(?:moveTo|lineTo|curveTo|quadTo|horizontalLineTo|verticalLineTo)\s*\(")
_GENERATED_HEADER_RE = re.compile(r"GENERATED by convert_image_to_imagevector")
_HANDWRITTEN_VECTOR_MIN_CMDS = 10


def _detect_handwritten_imagevector(root: Path) -> list[str]:
    """Flag hand-written ImageVector.Builder blocks with substantial path data.

    Hallucinated float coordinates produce broken art. Vector path data must come from
    convert_image_to_imagevector.py, which stamps a GENERATED header. Tiny builders
    (< 10 path commands) are trivially reviewable and skipped; generated files are
    identified by the header and skipped.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _IMAGEVECTOR_BUILDER_RE.search(text):
            continue
        if _GENERATED_HEADER_RE.search(text):
            continue
        cmd_count = len(_PATH_CMD_RE.findall(text))
        if cmd_count >= _HANDWRITTEN_VECTOR_MIN_CMDS:
            m = _IMAGEVECTOR_BUILDER_RE.search(text)
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"handwritten imagevector [HIGH]: {path.relative_to(root)}:{line_no} "
                f"— ImageVector.Builder with {cmd_count} hand-written path commands and no "
                f"GENERATED header; hallucinated coordinates produce broken art — re-trace "
                f"the source with convert_image_to_imagevector.py "
                f"(see imagevector-generator skill)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Raster image assets in commonMain ─────────────────────────────────────────

_RASTER_EXTS = {".png", ".jpg", ".jpeg", ".webp"}
_RASTER_EXEMPT_TOKENS = ("/photos/", "/screenshots/", "/goldens/", "/snapshots/")


def _detect_raster_in_commonmain(root: Path) -> list[str]:
    """Flag raster images shipped in commonMain resources.

    Icons/logos/flat art should be compiled ImageVectors (theme-tintable, resolution-
    independent, no per-density buckets). Photos are exempt via assets/photos/;
    screenshot goldens are exempt via their directory names.
    """
    findings: list[str] = []
    for path in root.rglob("*"):
        if not path.is_file() or path.suffix.lower() not in _RASTER_EXTS:
            continue
        if _is_excluded(path, root):
            continue
        p = path.as_posix().lower()
        if "/commonmain/" not in p:
            continue
        if any(tok in p for tok in _RASTER_EXEMPT_TOKENS):
            continue
        findings.append(
            f"raster asset in commonMain [MEDIUM]: {path.relative_to(root)} "
            f"— PNG/JPG icons and flat art should be compiled ImageVectors "
            f"(convert_image_to_imagevector.py); photos belong under assets/photos/ "
            f"(see imagevector-generator skill)"
        )
    return findings


# ── Arbitrary weight literals ─────────────────────────────────────────────────

_WEIGHT_LITERAL_RE = re.compile(r"\.weight\s*\(\s*(\d+(?:\.\d+)?)f?\s*[),]")


def _detect_raw_weight_literal(root: Path) -> list[str]:
    """Flag arbitrary layout weights (e.g. weight(0.37f)) — guessed proportions.

    Weights must come from a closed set of simple fractions (whole numbers or .5 steps,
    e.g. 1f, 1.5f, 2f, 3f) so layouts are deterministic and reviewable. An arbitrary
    float like 0.37f is a guessed proportion the layout contract can't express.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue
        for m in _WEIGHT_LITERAL_RE.finditer(text):
            value = float(m.group(1))
            if (value * 2) != int(value * 2):  # not a whole or .5 fraction
                line_no, snippet = _at(text, m.start())
                findings.append(
                    f"raw weight literal [LOW]: {path.relative_to(root)}:{line_no} "
                    f"— weight({m.group(1)}f) is an arbitrary proportion; use simple "
                    f"fractions (1f, 1.5f, 2f, 3f) from the layout contract so the split "
                    f"is deterministic (see layout-system skill → slot grid)\n"
                    f"    {line_no} | {snippet}"
                )
                break  # one finding per file
    return findings


# ── Incomplete WindowSizeClass branch coverage ────────────────────────────────

_WSC_CLASS_REF_RE = re.compile(r"\bWindowWidthSizeClass\.(Compact|Medium|Expanded)\b")


def _detect_breakpoint_branch_missing(root: Path) -> list[str]:
    """Flag files that branch on WindowWidthSizeClass but don't cover all breakpoints.

    A file referencing some (but not all) of Compact/Medium/Expanded with no `else`
    branch silently falls through on the unhandled size. LOW severity — file-level
    heuristic; verify at the flagged when-block.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        refs = set(_WSC_CLASS_REF_RE.findall(text))
        if not refs or len(refs) == 3:
            continue
        if re.search(r"\belse\s*->", text):
            continue
        missing = sorted({"Compact", "Medium", "Expanded"} - refs)
        m = _WSC_CLASS_REF_RE.search(text)
        line_no, snippet = _at(text, m.start())
        findings.append(
            f"breakpoint branch missing [LOW]: {path.relative_to(root)}:{line_no} "
            f"— branches on WindowWidthSizeClass but never handles {', '.join(missing)} "
            f"and has no else branch; the unhandled size falls through silently\n"
            f"    {line_no} | {snippet}"
        )
    return findings


# ── Fixed width that overflows a compact phone ────────────────────────────────

# Fixed width/size in dp — overflows a compact phone (~360.dp) when >= threshold.
_FIXED_WIDTH_RE = re.compile(r"\.(width|size)\s*\(\s*(\d+)\s*\.dp")
# requiredWidth/requiredSize IGNORE incoming constraints — overflow risk at lower values.
_REQUIRED_SIZE_RE = re.compile(r"\.(requiredWidth|requiredSize)\s*\(\s*(\d+)\s*\.dp")
_COMPACT_WIDTH_DP = 360   # standard compact-phone width
_REQUIRED_OVERFLOW_DP = 200


def _detect_fixed_width_overflow(root: Path) -> list[str]:
    """Flag fixed widths likely to overflow a compact phone.

    'Compact enough' is ultimately a rendered property (see Roborazzi at 360x800), but a
    fixed `.width(360.dp)` / `.size(360.dp)` or a constraint-ignoring `.requiredWidth(…)`
    is a reliable static correlate of a non-responsive layout. The fix is `fillMaxWidth()`,
    `weight()`, or `widthIn(max = …)` so the layout adapts to the available width.
    """
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root) or _is_test_source(path):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue
        if any(p in path.stem for p in ("Theme", "theme", "Token", "token")):
            continue

        hit = None
        for m in _FIXED_WIDTH_RE.finditer(text):
            if int(m.group(2)) >= _COMPACT_WIDTH_DP:
                hit = (m, f".{m.group(1)}({m.group(2)}.dp) ≥ {_COMPACT_WIDTH_DP}.dp")
                break
        if hit is None:
            for m in _REQUIRED_SIZE_RE.finditer(text):
                if int(m.group(2)) >= _REQUIRED_OVERFLOW_DP:
                    hit = (m, f".{m.group(1)}({m.group(2)}.dp) ignores parent constraints")
                    break
        if hit:
            m, why = hit
            line_no, snippet = _at(text, m.start())
            findings.append(
                f"fixed width overflow [LOW]: {path.relative_to(root)}:{line_no} "
                f"— {why}; this overflows a compact phone — use fillMaxWidth(), weight(), "
                f"or widthIn(max = …) so the layout adapts (see adaptive-layout skill)\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Redundant title detection ─────────────────────────────────────────────────

# Matches a heading-style text call: AppText/Text with a style that looks like a
# page-level title (H1/H2/Heading/HeadlineLarge/TitleLarge/DisplaySmall).
_HEADING_STYLE_RE = re.compile(
    r"\b(AppText|Text)\s*\([^)]*style\s*=\s*[A-Za-z.]*"
    r"(?:H1|H2|Heading|HeadlineLarge|TitleLarge|DisplaySmall)\b",
)
_TOPBAR_RE = re.compile(r"\b(AppTopAppBar|TopAppBar|CenterAlignedTopAppBar)\b")
_TOPBAR_SLOT_RE = re.compile(r"\btopBar\s*=\s*\{")


def _detect_redundant_title(root: Path) -> list[str]:
    """Flag Screen/Content files that have a scaffold topBar AND a heading-style
    Text in the content body — the title is shown twice visually."""
    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        if not any(path.stem.endswith(part) for part in _SCREEN_STEMS):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        has_topbar = _TOPBAR_RE.search(text) and _TOPBAR_SLOT_RE.search(text)
        has_heading = _HEADING_STYLE_RE.search(text)
        if has_topbar and has_heading:
            line_no, snippet = _at(text, has_heading.start())
            findings.append(
                f"redundant screen title [MEDIUM]: {path.relative_to(root)}:{line_no} "
                f"— AppTopAppBar in topBar slot AND a heading-style Text in content; "
                f"the title appears twice — remove the in-body heading\n"
                f"    {line_no} | {snippet}"
            )
    return findings


# ── Missing adaptive breakpoint coverage ──────────────────────────────────────

_WINDOW_SIZE_CLASS_RE = re.compile(r"\bWindowSizeClass\b")
# Any param TYPED WindowSizeClass counts — the param name is the project's choice.
_WINDOW_SIZE_CLASS_PARAM_RE = re.compile(r"\w+\s*:\s*WindowSizeClass\b")


def _detect_missing_adaptive_coverage(root: Path) -> list[str]:
    """If any file in the project uses WindowSizeClass, every Screen composable in
    a :ui module should accept a windowSizeClass param.  Flag screens that don't."""
    # First pass — is adaptive layout in use at all?
    project_uses_adaptive = False
    for path in root.rglob("*.kt"):
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if _WINDOW_SIZE_CLASS_RE.search(text):
            project_uses_adaptive = True
            break

    if not project_uses_adaptive:
        return []

    findings: list[str] = []
    for path in root.rglob("*.kt"):
        if _is_excluded(path, root):
            continue
        # Top-level screens by name (Screen/Page/View), regardless of module path
        if not any(path.stem.endswith(s) for s in ("Screen", "Page", "View")):
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if not _is_compose_ui_file(text, path):
            continue
        if not _WINDOW_SIZE_CLASS_PARAM_RE.search(text):
            findings.append(
                f"adaptive coverage [LOW]: {path.relative_to(root)} "
                f"— project uses WindowSizeClass but this screen has no windowSizeClass param; "
                f"add windowSizeClass: WindowSizeClass and branch layout per breakpoint"
            )
    return findings


def _is_excluded(path: Path, root: Path) -> bool:
    parts = path.relative_to(root).parts
    return any(
        part in _EXCLUDED_DIRS or part.endswith(".cpp")  # excludes llama.cpp/, stable-diffusion.cpp/ submodules
        for part in parts
    )


_TEST_DIR_TOKENS = (
    "/test/", "/commontest/", "/jvmtest/", "/androidtest/", "/androidunittest/",
    "/androidinstrumentedtest/", "/iostest/", "/desktoptest/", "/unittest/", "/nativetest/",
)


def _is_test_source(path: Path) -> bool:
    """True for test source sets / test files. Production-UI smells (raw components,
    fixed widths) should not fire on test fixtures — screenshot tests set fixed canvas
    sizes and use raw components in capture blocks intentionally."""
    p = path.as_posix().lower()
    if any(t in p for t in _TEST_DIR_TOKENS):
        return True
    return path.stem.endswith("Test") or path.stem.endswith("Spec")


def iter_files(root: Path):
    for path in root.rglob("*"):
        if not path.is_file():
            continue
        if _is_excluded(path, root):
            continue
        if path.suffix in {".kt", ".kts"}:
            yield path


def _is_compose_ui_file(text: str, path: Path) -> bool:
    """A file is treated as Compose UI if it declares/imports Compose, OR lives in a
    conventional UI path. Content detection makes UI smells (hardcoded colors, spacing,
    dark-theme scatter) fire even when the project does not use a /ui/ module layout."""
    if "@Composable" in text or "androidx.compose" in text or "koinViewModel" in text:
        return True
    return any(token in path.as_posix() for token in ("/ui/", "/presentation/"))


# ── Lesson / positive pattern detection ──────────────────────────────────────

def _detect_positive_patterns(root: Path) -> list[dict]:
    """
    Scan the consumer for patterns that exceed or are absent from current skill guidance.
    Returns structured lesson candidates for upstreaming.
    Each entry: { skill, pattern, description, evidence }
    """
    lessons: list[dict] = []
    kt_all = _read_all(root, "*.kt")

    # ── Design system ──────────────────────────────────────────────────────────

    theme_kt = _read_all(root, "*Theme*.kt", "*theme*.kt")

    if "compositionLocalOf<Boolean?>" in theme_kt and "isSystemInDarkTheme()" in theme_kt:
        lessons.append({
            "skill": "kmp-compose-design-system",
            "pattern": "LocalAppDarkTheme compositionLocalOf<Boolean?> override",
            "description": (
                "Consumer defines LocalAppDarkTheme = compositionLocalOf<Boolean?> { null } "
                "so in-app theme toggles override isSystemInDarkTheme() without changing AppTheme's signature. "
                "null = follow system, true/false = force. Skill should document this as the canonical override pattern."
            ),
            "evidence": "grep -r 'compositionLocalOf<Boolean?>' in *Theme*.kt",
        })

    if "userPreference" in theme_kt and ("LocalStorage" in theme_kt or "DataStore" in theme_kt or "SharedPreferences" in theme_kt):
        lessons.append({
            "skill": "kmp-compose-design-system",
            "pattern": "ThemeSettings persistent override with cross-platform storage",
            "description": (
                "Consumer persists theme preference via LocalStorage/DataStore into a ThemeSettings object "
                "backed by mutableStateOf<Boolean?>. Survives app restart. "
                "Skill should add a 'persisting theme choice' step after LocalAppDarkTheme is wired."
            ),
            "evidence": "grep -r 'userPreference' in *Theme*.kt",
        })

    # currentIsDark() helper
    if "fun currentIsDark()" in theme_kt or "fun isDark()" in theme_kt:
        lessons.append({
            "skill": "kmp-compose-design-system",
            "pattern": "currentIsDark() single-call-site helper",
            "description": (
                "Consumer wraps the preference-or-system fallback into a @Composable fun currentIsDark(): Boolean. "
                "Reduces duplication across multiple theme entry points (Android, iOS, Desktop, Web). "
                "Skill should recommend this helper in multi-platform entry wiring (Step 7)."
            ),
            "evidence": "grep -r 'fun currentIsDark' in *Theme*.kt",
        })

    # ── MVI ───────────────────────────────────────────────────────────────────

    if "BaseViewModel" in kt_all and ("Channel.BUFFERED" in kt_all or "receiveAsFlow()" in kt_all):
        lessons.append({
            "skill": "kmp-mvi",
            "pattern": "Project-level BaseViewModel wrapping MviViewModel",
            "description": (
                "Consumer defines a thin BaseViewModel<S,E,I> that extends MviViewModel from :core, "
                "adding project-specific defaults (e.g. error handling, logging hooks). "
                "Skill should mention this as an optional layer between :core:mvi and feature ViewModels."
            ),
            "evidence": "grep -r 'class BaseViewModel' in *.kt",
        })

    if "UNDO_WINDOW_MS" in kt_all or (re.search(r"undoJob.*cancel|cancel.*undoJob", kt_all) and "delay(" in kt_all):
        lessons.append({
            "skill": "kmp-mvi",
            "pattern": "Timed undo window (soft-delete + cancel)",
            "description": (
                "Consumer implements undo via a coroutine Job: on delete intent, schedule actual deletion "
                "after UNDO_WINDOW_MS delay; an undo intent cancels the Job. "
                "Skill should add this as a named recipe under 'one-shot delete with undo'."
            ),
            "evidence": "grep -r 'UNDO_WINDOW_MS\\|undoJob' in *.kt",
        })

    # Contract pattern: all three in one sealed object
    has_intent = re.search(r"sealed (interface|class) Intent", kt_all)
    has_effect = re.search(r"sealed (interface|class) Effect", kt_all)
    has_contract_obj = re.search(r"object \w+Contract", kt_all)
    if has_intent and has_effect and has_contract_obj:
        lessons.append({
            "skill": "kmp-mvi",
            "pattern": "Contract object groups State + Intent + Effect",
            "description": (
                "Consumer colocates State (data class), Intent (sealed interface), and Effect (sealed interface) "
                "inside a single object FooContract. Improves discoverability vs three separate top-level files. "
                "Skill already recommends this but should show the grouping as the default."
            ),
            "evidence": "grep -r 'object.*Contract' in *.kt",
        })

    # ── Architecture / structure ───────────────────────────────────────────────

    if (root / "build-logic").exists() and any((root / "build-logic").rglob("*.gradle.kts")):
        lessons.append({
            "skill": "kmp-clean-architecture",
            "pattern": "build-logic/ convention plugins",
            "description": (
                "Consumer uses a build-logic/ includeBuild with convention plugins to centralize AGP/KMP "
                "configuration across modules. Eliminates copy-paste Gradle config. "
                "Skill should recommend this pattern for multi-module projects."
            ),
            "evidence": "ls build-logic/convention/src/",
        })

    # FuzzyMatcher for search UX
    if "FuzzyMatcher" in kt_all or re.search(r"fun fuzzyMatch|levenshtein|editDistance", kt_all, re.IGNORECASE):
        lessons.append({
            "skill": "kmp-clean-architecture",
            "pattern": "FuzzyMatcher utility for search",
            "description": (
                "Consumer ships a FuzzyMatcher (Levenshtein/edit-distance) utility in :core for member/item search. "
                "Purely platform-agnostic, testable, reusable. "
                "Skill could mention domain utilities like matchers as candidates for :core:util."
            ),
            "evidence": "grep -r 'FuzzyMatcher' in *.kt",
        })

    # ── CI ────────────────────────────────────────────────────────────────────

    if _has(root, ".github/workflows/governance.yml", ".github/workflows/governance.yaml"):
        lessons.append({
            "skill": "kmp-ci-github-actions",
            "pattern": "governance.yml quality gate",
            "description": (
                "Consumer has a separate governance.yml workflow (distinct from build/test) "
                "that enforces merge rules, code ownership, or audit checks. "
                "Skill should add governance workflow as an optional CI step."
            ),
            "evidence": "ls .github/workflows/governance.yml",
        })

    # Multi-surface deployment (separate deploy workflows per target)
    deploy_workflows = list((root / ".github" / "workflows").glob("deploy-*.yml")) if (root / ".github" / "workflows").exists() else []
    if len(deploy_workflows) >= 2:
        lessons.append({
            "skill": "kmp-ci-github-actions",
            "pattern": "Per-surface deploy workflows (deploy-web.yml, deploy-image.yml, …)",
            "description": (
                f"Consumer has {len(deploy_workflows)} separate deploy-*.yml workflows, one per deployment surface. "
                "Keeps CI graphs readable and allows surface-specific secrets/environments. "
                "Skill should recommend this split for multi-surface KMP projects."
            ),
            "evidence": f"ls .github/workflows/deploy-*.yml  ({len(deploy_workflows)} found)",
        })

    # ── Agent setup ───────────────────────────────────────────────────────────

    claude = root / ".claude"
    source_layout = [
        root / "agents",
        root / "rules",
        root / "hooks",
        root / "commands",
        root / "skills",
        root / "docs" / "reference" / "ai-collaboration.md",
        root / "docs" / "reference" / "agent-catalog.md",
    ]
    if (
        (claude / "AGENTS.md").exists()
        and (claude / "commands").exists()
        and (claude / "skills").exists()
        and (root / "CLAUDE.md").exists()
        and all(path.exists() for path in source_layout)
    ):
        lessons.append({
            "skill": "kmp-audit",
            "pattern": "Full Claude scaffold (project-owned sources + deployed runtime)",
            "description": (
                "Consumer keeps both the project-owned source scaffold and the deployed Claude runtime in sync. "
                "This project is a good reference for what the /kmp-setup-agents command should produce."
            ),
            "evidence": "ls CLAUDE.md agents/ rules/ hooks/ commands/ skills/ docs/reference/ai-collaboration.md docs/reference/agent-catalog.md .claude/AGENTS.md .claude/commands/ .claude/skills/",
        })

    return lessons


def harvest_project(root: Path) -> dict:
    """Return findings + positive lessons as a structured dict (for --harvest JSON output)."""
    return {
        "project": str(root),
        "findings": audit_project(root),
        "lessons": _detect_positive_patterns(root),
    }


def audit_project(root: Path) -> list[str]:
    findings: list[str] = []

    # ── Agent & consumer setup ─────────────────────────────────────────────────
    findings.extend(_detect_agent_setup(root))

    # ── Project-owned agent file standards + deployment drift ──────────────────
    findings.extend(_detect_agent_file_standards(root))
    findings.extend(_detect_agent_deployment_drift(root))

    # ── MVI base class placement ───────────────────────────────────────────────
    findings.extend(_detect_mvi_placement(root))

    # ── Design system wiring ───────────────────────────────────────────────────
    findings.extend(_detect_design_system_wiring(root))

    # ── Multi-ViewModel screen ─────────────────────────────────────────────────
    findings.extend(_detect_multi_viewmodel_screen(root))

    # ── God composable (side-effect orchestration in UI) ───────────────────────
    findings.extend(_detect_god_composable(root))

    # ── ViewModel taking another ViewModel as a constructor param ──────────────
    findings.extend(_detect_viewmodel_in_viewmodel(root))

    # ── ViewModel passed as a composable parameter ─────────────────────────────
    findings.extend(_detect_viewmodel_as_composable_param(root))

    # ── Repository injected and Flow-collected directly inside a Composable ────
    findings.extend(_detect_repository_in_composable(root))

    # ── String-based (non-type-safe) navigation ────────────────────────────────
    findings.extend(_detect_string_navigation(root))

    # ── Repository interface leaking data-layer types ──────────────────────────
    findings.extend(_detect_repository_leaks_data_type(root))

    # ── Raw component bypassing the design system ──────────────────────────────
    findings.extend(_detect_raw_component_bypass(root))

    # ── Fixed width that overflows a compact phone ─────────────────────────────
    findings.extend(_detect_fixed_width_overflow(root))

    # ── Hand-written ImageVector path data ─────────────────────────────────────
    findings.extend(_detect_handwritten_imagevector(root))

    # ── Raster image assets in commonMain ──────────────────────────────────────
    findings.extend(_detect_raster_in_commonmain(root))

    # ── Arbitrary weight literals ───────────────────────────────────────────────
    findings.extend(_detect_raw_weight_literal(root))

    # ── Incomplete WindowSizeClass branch coverage ──────────────────────────────
    findings.extend(_detect_breakpoint_branch_missing(root))

    # ── Hardcoded Android versionCode ──────────────────────────────────────────
    findings.extend(_detect_hardcoded_android_version_code(root))

    # ── Compose Styles API compliance ──────────────────────────────────────────
    findings.extend(_detect_style_default_with_body(root))
    findings.extend(_detect_style_state_wrong_enabled(root))
    findings.extend(_detect_style_param_on_screen(root))
    findings.extend(_detect_stale_compositionlocal_in_style_function(root))
    findings.extend(_detect_missing_indication_null_with_style_state(root))

    # ── Toggle/collapsible layout stability ─────────────────────────────────────
    findings.extend(_detect_toggle_icon_swap(root))
    findings.extend(_detect_bare_conditional_collapse(root))
    findings.extend(_detect_focused_state_animates_border_width(root))

    # ── Combined "one file per X" violations ────────────────────────────────────
    findings.extend(_detect_combined_lesson_file(root))
    findings.extend(_detect_combined_layout_screen_file(root))
    findings.extend(_detect_combined_sqldelight_table_file(root))

    # ── Raw HTTP bypassing an established Ktor client ───────────────────────────
    findings.extend(_detect_raw_http_bypass(root))

    # ── WHAT-comment inside a loop or conditional ────────────────────────────────
    findings.extend(_detect_what_comment_in_control_flow(root))
    findings.extend(_detect_long_stacked_comment_block(root))
    findings.extend(_detect_justification_comment_above_single_statement(root))

    # ── Destructive-read accessor (single-writer snapshot anti-pattern) ─────────
    findings.extend(_detect_destructive_read_accessor(root))

    # ── Pattern-adoption opportunities (nudges, not misuse flags) ───────────────
    findings.extend(_detect_value_class_opportunity(root))
    findings.extend(_detect_context_parameter_opportunity(root))

    # ── God class (repo-wide, not scoped to ViewModel/Composable) ───────────────
    findings.extend(_detect_god_class(root))

    # ── runBlocking in shared code, Koin cycles, unstable Compose collections ───
    findings.extend(_detect_runblocking_in_shared_code(root))
    findings.extend(_detect_koin_circular_dependency(root))
    findings.extend(_detect_compose_unstable_collection_param(root))

    # ── Library project structure conformance (kmp-library-publishing) ──────────
    findings.extend(_detect_library_missing_binary_compat_validator(root))
    findings.extend(_detect_library_missing_explicit_api(root))
    findings.extend(_detect_library_multimodule_missing_build_logic(root))

    # ── Undocumented public API (library projects only) ──────────────────────────
    findings.extend(_detect_undocumented_public_api(root))
    findings.extend(_detect_lowercase_unit_composable(root))
    findings.extend(_detect_partial_param_documentation(root))
    findings.extend(_detect_kotlin_reflect_in_common(root))
    findings.extend(_detect_god_utils_file(root))
    findings.extend(_detect_inline_unnamed_regex(root))
    findings.extend(_detect_hardcoded_ui_string(root))
    findings.extend(_detect_object_creation_in_loop(root))
    findings.extend(_detect_public_mutable_collection(root))
    findings.extend(_detect_context_leak_in_singleton(root))

    # ── Combined design-system component file ────────────────────────────────────
    findings.extend(_detect_combined_component_file(root))
    findings.extend(_detect_combined_style_file(root))

    # ── ViewModel god-class signals beyond line count ────────────────────────────
    findings.extend(_detect_viewmodel_too_many_intents(root))
    findings.extend(_detect_viewmodel_multiple_stateflows(root))
    findings.extend(_detect_viewmodel_injects_repository(root))

    # ── Extensible abstract class in commonMain ─────────────────────────────────
    findings.extend(_detect_extensible_abstract_class_in_common(root))

    # ── Module layer-order violation ────────────────────────────────────────────
    findings.extend(_detect_module_layer_violation(root))
    findings.extend(_detect_bare_core_module(root))
    findings.extend(_detect_unauthorized_app_submodule(root))
    findings.extend(_detect_leftover_wizard_demo_code(root))

    # ── Hardcoded base URL (library-first / configurability) ───────────────────
    findings.extend(_detect_hardcoded_base_url(root))

    # ── Design system prefix mismatch ──────────────────────────────────────────
    findings.extend(_detect_design_system_prefix_mismatch(root))

    # ── Mixed component library (ShadcnTheme + AppTheme both wired in) ─────────
    findings.extend(_detect_mixed_design_system_usage(root))

    # ── Project-owned skill standards (skills/<name>/SKILL.md anatomy) ─────────
    findings.extend(_detect_project_skill_standards(root))
    findings.extend(_detect_project_skill_deployment_drift(root))

    # ── Empty platform-specific source sets ────────────────────────────────────
    findings.extend(_detect_empty_platform_sourceset(root))

    # ── Redundant screen title ─────────────────────────────────────────────────
    findings.extend(_detect_redundant_title(root))

    # ── Missing adaptive breakpoint coverage ───────────────────────────────────
    findings.extend(_detect_missing_adaptive_coverage(root))

    # ── ViewModel size check (not regex-detectable, needs line count) ──────────
    vm_info = _detect_viewmodel_size(root)
    for rel_path, line_count in vm_info["large_vms"]:
        severity = "god viewmodel" if line_count >= 300 else "large viewmodel"
        findings.append(f"{severity} ({line_count} lines): {rel_path}")

    for path in iter_files(root):
        text = path.read_text(encoding="utf-8", errors="ignore")
        is_compose = _is_compose_ui_file(text, path)
        for label, pattern in PATTERNS:
            if label == "network result in ui" and not is_compose:
                continue
            if label == "data import in ui":
                if not is_compose or path.stem.endswith("ViewModel"):
                    continue
            if label == "dto leak to domain":
                # domain by path OR by a domain-layer filename (use case / interactor)
                is_domain = (
                    "/domain/" in path.as_posix()
                    or path.stem.endswith("UseCase")
                    or path.stem.endswith("Interactor")
                )
                if not is_domain:
                    continue
            if label == "navcontroller in viewmodel" and not path.stem.endswith("ViewModel"):
                continue
            if label == "magic color literal":
                if not is_compose:
                    continue
                if any(part in path.stem for part in ("Color", "Token", "Theme", "color", "token", "theme")):
                    continue
            if label == "named color in ui":
                if not is_compose:
                    continue
                if any(part in path.stem for part in ("Color", "Token", "Theme", "color", "token", "theme")):
                    continue
            if label == "hardcoded divider color":
                if not is_compose:
                    continue
            if label == "system dark theme scatter" and any(
                part in path.stem for part in ("Theme", "theme", "App")
            ):
                continue
            if label == "hardcoded spacing":
                if not is_compose:
                    continue
                if any(part in path.stem for part in ("Spacing", "spacing", "Token", "token", "Theme", "theme")):
                    continue
            mt = pattern.search(text)
            if mt:
                line_no, snippet = _at(text, mt.start())
                findings.append(
                    f"{label}: {path.relative_to(root)}:{line_no}\n    {line_no} | {snippet}"
                )

    return findings


def main() -> int:
    import json as _json

    parser = argparse.ArgumentParser(description="KMP architecture audit and adoption roadmap.")
    parser.add_argument("project_root", type=Path, help="Path to the KMP project root")
    parser.add_argument(
        "--roadmap",
        action="store_true",
        help="Output a prioritized adoption plan instead of violation findings",
    )
    parser.add_argument(
        "--harvest",
        action="store_true",
        help="Output findings + positive lessons as JSON for upstreaming to skills",
    )
    args = parser.parse_args()

    root = args.project_root.resolve()

    if args.roadmap:
        state = assess_project(root)
        plan  = build_roadmap(state)
        print_roadmap(root, state, plan)
        return 1 if plan else 0

    if args.harvest:
        result = harvest_project(root)
        print(_json.dumps(result, indent=2))
        # Exit 1 if there are HIGH findings (so CI can gate on it)
        has_high = any("[HIGH]" in f or "[HIGH]:" in f for f in result["findings"])
        return 1 if has_high else 0

    findings = audit_project(root)
    hints = (
        _detect_name_behavior_drift(root)
        + _detect_vague_class_name_suffix(root)
        + _detect_empty_catch_block(root)
        + _detect_unjustified_suppress(root)
    )

    if findings:
        print("FINDINGS:")
        for finding in findings:
            print(f"- {finding}")
        if hints:
            print("\nHINTS (non-blocking, manual review only):")
            for hint in hints:
                print(f"- {hint}")
        return 1

    print("OK: no architecture violations detected")
    if hints:
        print("\nHINTS (non-blocking, manual review only):")
        for hint in hints:
            print(f"- {hint}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
