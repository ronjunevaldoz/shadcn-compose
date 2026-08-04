#!/usr/bin/env python3
"""
harvest_lessons.py — parse docs/lessons/*.md from consumer projects and
produce a grouped amendment report for the kmp-agent-skills collection.
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import dataclass, field, asdict
from pathlib import Path

KNOWN_SKILLS = {
    "kmp-compose-accessibility",
    "kmp-compose-adaptive-layout",
    "kmp-analytics",
    "kmp-audit",
    "kmp-biometric-auth",
    "kmp-ci-github-actions",
    "kmp-clean-architecture",
    "kmp-code-quality",
    "kmp-compose-animation",
    "kmp-compose-slot-api",
    "kmp-compose-state-container",
    "kmp-compose-state-hoisting",
    "kmp-crash-reporting",
    "kmp-datastore",
    "kmp-deep-linking",
    "kmp-dependency-injection",
    "kmp-compose-design-system",
    "kmp-compose-design-system-extended",
    "kmp-expect-actual",
    "kmp-expert",
    "kmp-feature-flags",
    "kmp-feature-scaffold",
    "kmp-flavor-environment",
    "kmp-form-validation",
    "kmp-compose-graphics-modifiers",
    "kmp-image-loading",
    "kmp-imagevector-generator",
    "kmp-jni-pro",
    "kmp-kotlin-rpc",
    "kmp-ktor-auth-service",
    "kmp-legal-docs",
    "kmp-lessons",
    "kmp-logging",
    "kmp-mongodb-database",
    "kmp-mvi",
    "kmp-navigation",
    "kmp-network-layer",
    "kmp-offline-first",
    "kmp-paging",
    "kmp-permissions",
    "kmp-presenter-module",
    "kmp-compose-preview-driven-development",
    "kmp-project-docs-maintainer",
    "kmp-push-notifications",
    "kmp-release",
    "kmp-repository-pattern",
    "kmp-roborazzi",
    "kmp-shared-resources",
    "kmp-skill-harvester",
    "kmp-sqldelight-setup",
    "kmp-unit-testing",
    "kmp-workmanager",
    "kmp-xcframework-spm",
    "kmp-library-publishing",
    "unknown",
}

VALID_SEVERITIES = {"high", "medium", "low"}
VALID_TYPES = {"correction", "gap", "better-pattern", "deprecation", "confirmation"}
SEVERITY_ORDER = {"high": 0, "medium": 1, "low": 2}


@dataclass
class Lesson:
    file: str
    skill: str
    date: str
    severity: str
    type: str
    summary: str  # first non-empty line of body after frontmatter
    errors: list[str] = field(default_factory=list)


def _parse_frontmatter(text: str) -> tuple[dict[str, str], str]:
    """Return (frontmatter dict, body). Body is everything after the closing ---."""
    if not text.startswith("---"):
        return {}, text
    end = text.find("\n---", 3)
    if end == -1:
        return {}, text
    fm_block = text[3:end].strip()
    body = text[end + 4:].strip()
    fm: dict[str, str] = {}
    for line in fm_block.splitlines():
        if ":" in line:
            k, _, v = line.partition(":")
            fm[k.strip()] = v.strip()
    return fm, body


def _first_heading(body: str) -> str:
    """Extract the first non-empty content line that is not a markdown heading."""
    skip_next_blank = False
    for line in body.splitlines():
        stripped = line.strip()
        if stripped.startswith("#"):
            skip_next_blank = True
            continue
        if not stripped:
            skip_next_blank = False
            continue
        return stripped[:120]
    return "(no summary)"


def parse_lesson(path: Path) -> Lesson:
    text = path.read_text(encoding="utf-8")
    fm, body = _parse_frontmatter(text)
    errors: list[str] = []

    skill = fm.get("skill", "").strip() or "unknown"
    date = fm.get("date", "").strip() or "unknown"
    severity = fm.get("severity", "").strip().lower()
    lesson_type = fm.get("type", "").strip().lower()
    summary = _first_heading(body)

    if skill not in KNOWN_SKILLS:
        errors.append(f"unknown skill '{skill}' — will be routed to 'unknown'")
        skill = "unknown"
    if severity not in VALID_SEVERITIES:
        errors.append(f"invalid severity '{severity}' — expected high/medium/low")
        severity = "low"
    if lesson_type not in VALID_TYPES:
        errors.append(f"invalid type '{lesson_type}' — expected {'/'.join(VALID_TYPES)}")
        lesson_type = "gap"

    return Lesson(
        file=str(path),
        skill=skill,
        date=date,
        severity=severity,
        type=lesson_type,
        summary=summary,
        errors=errors,
    )


def collect_lessons(project_roots: list[Path], min_severity: str) -> list[Lesson]:
    min_order = SEVERITY_ORDER.get(min_severity, 2)
    lessons: list[Lesson] = []
    for root in project_roots:
        lessons_dir = root / "docs" / "lessons"
        if not lessons_dir.exists():
            continue
        for md in sorted(lessons_dir.glob("*.md")):
            lesson = parse_lesson(md)
            if SEVERITY_ORDER.get(lesson.severity, 2) <= min_order:
                lessons.append(lesson)
    return lessons


def group_by_skill(lessons: list[Lesson]) -> dict[str, list[Lesson]]:
    groups: dict[str, list[Lesson]] = {}
    for lesson in sorted(lessons, key=lambda l: SEVERITY_ORDER.get(l.severity, 2)):
        groups.setdefault(lesson.skill, []).append(lesson)
    return groups


def action_for(lesson: Lesson) -> str:
    mapping = {
        "correction": "add to error-patterns or anti-patterns table",
        "gap": "add new section or cross-skill note",
        "better-pattern": "update anti-patterns with before/after",
        "deprecation": "update version reference + freshness note",
        "confirmation": "no change needed — evidence skill is working",
    }
    return mapping.get(lesson.type, "review manually")


def text_report(groups: dict[str, list[Lesson]], total: int) -> str:
    lines: list[str] = ["## Harvest Report", ""]
    amendment_count = sum(
        1 for lessons in groups.values()
        for l in lessons if l.type != "confirmation"
    )
    lines.append(f"Lessons parsed: {total}  |  Proposed amendments: {amendment_count}")
    lines.append("")

    for skill, lessons in sorted(groups.items()):
        lines.append(f"### {skill} ({len(lessons)} lesson{'s' if len(lessons) != 1 else ''})")
        for lesson in lessons:
            tag = f"[{lesson.severity.upper()} {lesson.type}]"
            action = action_for(lesson)
            lines.append(f"- {tag} {lesson.summary}")
            lines.append(f"  → {action}")
            lines.append(f"  file: {lesson.file}")
            if lesson.errors:
                for err in lesson.errors:
                    lines.append(f"  ⚠ {err}")
        lines.append("")

    if not groups:
        lines.append("No lessons found under docs/lessons/ in the provided project roots.")

    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Harvest docs/lessons/*.md from consumer projects and report proposed skill amendments."
    )
    parser.add_argument("projects", nargs="+", type=Path, help="Consumer project root(s)")
    parser.add_argument("--format", choices=["text", "json"], default="text")
    parser.add_argument(
        "--min-severity",
        choices=["high", "medium", "low"],
        default="low",
        help="Only include lessons at or above this severity",
    )
    args = parser.parse_args()

    lessons = collect_lessons(args.projects, args.min_severity)
    groups = group_by_skill(lessons)

    if args.format == "json":
        output = {
            "total": len(lessons),
            "amendment_count": sum(
                1 for ll in groups.values() for l in ll if l.type != "confirmation"
            ),
            "by_skill": {
                skill: [asdict(l) for l in ll]
                for skill, ll in groups.items()
            },
        }
        print(json.dumps(output, indent=2))
    else:
        print(text_report(groups, len(lessons)))

    has_errors = any(l.errors for l in lessons)
    return 1 if has_errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
