#!/usr/bin/env python3
"""
validate_keyword_routing.py — verify every skill directory has at least one row
in the expert SKILL.md Skill Invocation Map.

Exit codes:
  0 — all skills covered
  1 — one or more skills missing from the invocation map
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

SKILL_MAP_SECTION = "## Skill Invocation Map"

# Skills that intentionally have no invocation-map row because they are
# meta-tools loaded by commands, not by keyword matching.
SKIP_INVOCATION = {
    "kotlin-multiplatform-audit",   # loaded by /run-audit, not keyword routing
    "kotlin-multiplatform-expert",  # the routing skill itself
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


def validate_keyword_routing(repo_root: Path) -> list[str]:
    skills_dir = repo_root / "skills"
    expert_path = skills_dir / "kotlin-multiplatform-expert" / "SKILL.md"

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
        # `kotlin-multiplatform-roborazzi` or bare names like `jni-kotlin-pro`.
        if name not in map_text:
            errors.append(f"missing from Skill Invocation Map: {name}")

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
