#!/usr/bin/env python3
"""
validate_keyword_routing.py — verify every skill directory has at least one row
in the expert SKILL.md Skill Invocation Map, and that no two skills documented as
mutually-exclusive alternatives (one skill's description says "alternative to
<other-skill>") share a trigger keyword — that collision made both skills match
the same prompt despite only one being appropriate at a time (real case: a bare
`shadcn` keyword in kmp-compose-design-system collided with
kmp-shadcn-compose's own trigger keywords).

Exit codes:
  0 — all skills covered, no alternative-pair keyword collisions
  1 — one or more skills missing from the invocation map, or a collision found
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

SKILL_MAP_SECTION = "## Skill Invocation Map"
ALTERNATIVE_RE = re.compile(r"(?:[Aa]lternative to|instead of)\s+`?([a-z][a-z0-9-]+)", re.IGNORECASE)
TRIGGER_KEYWORDS_RE = re.compile(r"\*\*Trigger keywords:\*\*(.+?)(?:\n\s*\n|\Z)", re.DOTALL)

# Skills that intentionally have no invocation-map row because they are
# meta-tools loaded by commands, not by keyword matching.
SKIP_INVOCATION = {
    "kmp-audit",   # loaded by /run-audit, not keyword routing
    "kmp-expert",  # the routing skill itself
}


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def extract_invocation_map_text(expert_text: str) -> str:
    """Return only the text inside the Skill Invocation Map section."""
    start = expert_text.find(SKILL_MAP_SECTION)
    if start == -1:
        return ""
    end = expert_text.find("\n---", start)
    return expert_text[start:end] if end != -1 else expert_text[start:]


def _trigger_keywords(skill_text: str) -> set[str]:
    m = TRIGGER_KEYWORDS_RE.search(skill_text)
    if not m:
        return set()
    raw = m.group(1).replace("\n", " ")
    return {kw.strip().rstrip(".").lower() for kw in raw.split(",") if kw.strip()}


def _alternative_targets(skill_text: str, all_names: set[str]) -> set[str]:
    """Skill names this skill's description names as an alternative to."""
    targets: set[str] = set()
    for match in ALTERNATIVE_RE.finditer(skill_text):
        candidate = match.group(1).rstrip("'s").rstrip("-")
        if candidate in all_names:
            targets.add(candidate)
    return targets


def validate_keyword_routing(repo_root: Path) -> list[str]:
    skills_dir = repo_root / "skills"
    expert_path = skills_dir / "kmp-expert" / "SKILL.md"

    skill_dirs = sorted(
        p.parent for p in skills_dir.glob("*/SKILL.md") if p.is_file()
    )
    skill_names = {p.name for p in skill_dirs}

    expert_text = read_text(expert_path)
    map_text = extract_invocation_map_text(expert_text)

    errors: list[str] = []

    if not map_text:
        errors.append("Skill Invocation Map section not found in expert SKILL.md")
        return errors

    for name in sorted(skill_names - SKIP_INVOCATION):
        # Strip prefix for the backtick lookup — map rows use full names like
        # `kmp-roborazzi` or bare names like `jni-kotlin-pro`.
        if name not in map_text:
            errors.append(f"missing from Skill Invocation Map: {name}")

    # ── Alternative-pair keyword collisions ─────────────────────────────────
    skill_texts = {p.name: read_text(p / "SKILL.md") for p in skill_dirs}
    seen_pairs: set[tuple[str, str]] = set()
    for name, text in skill_texts.items():
        for target in _alternative_targets(text, skill_names):
            if target == name or target not in skill_texts:
                continue
            pair = tuple(sorted((name, target)))
            if pair in seen_pairs:
                continue
            seen_pairs.add(pair)
            shared = _trigger_keywords(text) & _trigger_keywords(skill_texts[target])
            if shared:
                errors.append(
                    f"keyword collision between documented alternatives "
                    f"{pair[0]} / {pair[1]}: {sorted(shared)}"
                )

    return errors


def main(argv: list[str] | None = None) -> int:
    import argparse

    parser = argparse.ArgumentParser(
        description="Validate every skill has keyword routing in the expert invocation map."
    )
    parser.add_argument(
        "--repo-root",
        type=Path,
        default=Path(__file__).resolve().parents[3],
        help="Path to the repo root (defaults to the current repo)",
    )
    args = parser.parse_args(argv)

    errors = validate_keyword_routing(args.repo_root.resolve())

    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        return 1

    skill_count = len({
        p.parent for p in (args.repo_root / "skills").glob("*/SKILL.md")
        if p.is_file() and p.parent.name not in SKIP_INVOCATION
    })
    print(f"OK: {skill_count} skills have keyword routing in the invocation map")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
