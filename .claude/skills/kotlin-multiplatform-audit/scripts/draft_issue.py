#!/usr/bin/env python3
"""
draft_issue.py — render and optionally submit a GitHub issue from an audit finding.

Usage:
  python3 draft_issue.py --title "..." --evidence "..." --recommendation "..." --skill "..."
  python3 draft_issue.py ... --submit
  python3 draft_issue.py ... --submit --dry-run   (print gh command, don't run it)
  python3 draft_issue.py ... --submit --repo owner/repo --labels "skill-bug,priority: high"
"""
from __future__ import annotations

import argparse
import shlex
import subprocess
import sys
from pathlib import Path

DEFAULT_REPO = "ronjunevaldoz/kmm-agent-skills"


def render_issue(title: str, evidence: str, recommendation: str, skill: str, kind: str) -> str:
    heading = "Question" if kind == "question" else "Issue"
    return f"""# {title}

## Type
{heading}

## Evidence
{evidence}

## Recommended follow-up
{recommendation}

## Suggested skill
{skill}

---
Suggested by {skill}
"""


def build_gh_command(
    title: str,
    body: str,
    repo: str,
    labels: list[str],
) -> list[str]:
    cmd = ["gh", "issue", "create", "--repo", repo, "--title", title, "--body", body]
    for label in labels:
        cmd += ["--label", label]
    return cmd


def submit_issue(
    title: str,
    body: str,
    repo: str,
    labels: list[str],
    dry_run: bool,
) -> int:
    cmd = build_gh_command(title, body, repo, labels)
    if dry_run:
        print("DRY RUN — would execute:")
        print(" ".join(shlex.quote(c) for c in cmd))
        return 0
    result = subprocess.run(cmd, capture_output=False)
    return result.returncode


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Render a GitHub-ready issue draft from an audit finding."
    )
    parser.add_argument("--title", required=True, help="Issue title")
    parser.add_argument("--evidence", required=True, help="Evidence that triggered the finding")
    parser.add_argument("--recommendation", required=True, help="Suggested fix or follow-up")
    parser.add_argument("--skill", required=True, help="Skill name to attribute")
    parser.add_argument("--kind", choices=("issue", "question"), default="issue", help="Draft type")
    parser.add_argument("--output", type=Path, help="Write rendered body to a file")
    parser.add_argument("--submit", action="store_true", help="Submit the issue via gh CLI")
    parser.add_argument(
        "--repo", default=DEFAULT_REPO,
        help=f"GitHub repo to file the issue in (default: {DEFAULT_REPO})",
    )
    parser.add_argument(
        "--labels", default="",
        help="Comma-separated labels to apply (e.g. 'skill-bug,priority: high')",
    )
    parser.add_argument(
        "--dry-run", action="store_true",
        help="With --submit: print the gh command without running it",
    )
    args = parser.parse_args()

    content = render_issue(args.title, args.evidence, args.recommendation, args.skill, args.kind)

    if args.output:
        args.output.write_text(content, encoding="utf-8")
    elif not args.submit:
        print(content, end="")

    if args.submit:
        labels = [l.strip() for l in args.labels.split(",") if l.strip()]
        return submit_issue(args.title, content, args.repo, labels, args.dry_run)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
