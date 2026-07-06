#!/usr/bin/env python3
"""
KMM Lessons — per-lesson file creator.

Writes EXACTLY ONE lesson file per invocation to docs/lessons/YYYY-MM-DD-<slug>.md.
It never appends to an existing file and never combines multiple lessons into one file —
each finding gets its own file, matching what kotlin-multiplatform-skill-harvester reads
(docs/lessons/*.md, one Lesson per file).

Usage:
  python3 create_lesson.py \
    --skill kotlin-multiplatform-mvi \
    --type correction \
    --severity high \
    --title "Effect replayed on nav back" \
    --followed "The skill said to emit effects via SharedFlow." \
    --broke "On nav back the effect replayed because replay=1 was set." \
    --correct "Use Channel(BUFFERED).receiveAsFlow() for one-shot effects." \
    --evidence "feature/todo/presenter/TodoViewModel.kt:34" \
    --proposed "Add to Common Anti-Patterns: never replay>0 for effects."

Body fields are optional — omitted ones become a TODO placeholder you fill in.
If a file with the same date+slug already exists, a numeric suffix is added so the
existing lesson is never overwritten.
"""
from __future__ import annotations

import argparse
import datetime as _dt
import re
import sys
from pathlib import Path

VALID_SEVERITIES = ("high", "medium", "low")
VALID_TYPES = ("correction", "gap", "better-pattern", "deprecation", "confirmation")


def slugify(title: str) -> str:
    s = title.strip().lower()
    s = re.sub(r"[^a-z0-9]+", "-", s)
    return s.strip("-")[:60] or "lesson"


def unique_path(lessons_dir: Path, date: str, slug: str) -> Path:
    base = f"{date}-{slug}"
    candidate = lessons_dir / f"{base}.md"
    n = 2
    while candidate.exists():
        candidate = lessons_dir / f"{base}-{n}.md"
        n += 1
    return candidate


def render(args, date: str) -> str:
    def field(value: str | None, placeholder: str) -> str:
        return value.strip() if value and value.strip() else f"_TODO: {placeholder}_"

    return f"""\
---
skill: {args.skill}
date: {date}
severity: {args.severity}
type: {args.type}
---

## {args.title.strip()}

## What we followed

{field(args.followed, "what the skill said to do, or was silent about")}

## What broke / what we discovered

{field(args.broke, "the symptom — what actually happened, and why")}

## Correct pattern

{field(args.correct, "the right way to do it; minimal code example if non-obvious")}

## Evidence

{field(args.evidence, "file:line, error message, test name, or commit ref")}

## Proposed skill change

{field(args.proposed, "section to amend (e.g. Common Anti-Patterns) and new/correction/section")}
"""


def main() -> int:
    p = argparse.ArgumentParser(description="Create ONE per-lesson file in docs/lessons/.")
    p.add_argument("--skill", required=True, help="Skill directory name, or 'unknown'.")
    p.add_argument("--type", required=True, choices=VALID_TYPES)
    p.add_argument("--severity", required=True, choices=VALID_SEVERITIES)
    p.add_argument("--title", required=True, help="Short specific title (becomes the slug + first heading).")
    p.add_argument("--followed", help="What the skill said / was silent about.")
    p.add_argument("--broke", help="What broke or what you discovered.")
    p.add_argument("--correct", help="The correct pattern.")
    p.add_argument("--evidence", help="file:line / error / test / commit.")
    p.add_argument("--proposed", help="Where in the skill this should land.")
    p.add_argument("--root", type=Path, default=Path("."), help="Consumer project root (default: .).")
    p.add_argument("--date", help="Override date (YYYY-MM-DD); defaults to today.")
    args = p.parse_args()

    date = (args.date or _dt.date.today().isoformat()).strip()
    if not re.fullmatch(r"\d{4}-\d{2}-\d{2}", date):
        print(f"error: --date must be YYYY-MM-DD, got '{date}'", file=sys.stderr)
        return 2

    lessons_dir = args.root / "docs" / "lessons"
    lessons_dir.mkdir(parents=True, exist_ok=True)

    path = unique_path(lessons_dir, date, slugify(args.title))
    path.write_text(render(args, date), encoding="utf-8")

    print(f"✅  Created lesson: {path}")
    print("    One finding per file — run create_lesson.py again for the next finding.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
