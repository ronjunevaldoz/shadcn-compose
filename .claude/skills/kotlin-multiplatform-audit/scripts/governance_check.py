#!/usr/bin/env python3
"""
governance_check.py — KMM skills governance gate for consumer KMP projects.

Orchestrates:
  1. scan_design_violations.py  (design system + layout consistency)
  2. audit_project.py           (architecture smells)

Reads .kmm-skills from the project root to identify the declared skills version.

Usage:
    python3 governance_check.py <project_root>
    python3 governance_check.py <project_root> --fail-on HIGH|MEDIUM|LOW
    python3 governance_check.py <project_root> --json

Exit codes:
    0 — clean (no findings at or above threshold)
    1 — findings at or above threshold
    2 — project root does not exist
"""
from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).parent
REPO_ROOT = SCRIPT_DIR.parent.parent.parent
SCAN_VIOLATIONS = REPO_ROOT / "skills/kotlin-multiplatform-design-system/scripts/scan_design_violations.py"
AUDIT_PROJECT = REPO_ROOT / "skills/kotlin-multiplatform-audit/scripts/audit_project.py"

SEVERITY_RANK: dict[str, int] = {"HIGH": 2, "MEDIUM": 1, "LOW": 0}
_SCAN_SEVERITY_MAP: dict[str, str] = {"error": "HIGH", "warning": "MEDIUM"}
SKILLS_VERSION_PATTERN = re.compile(r"^v?\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?$")


def read_skills_version(project_root: Path) -> str | None:
    skills_file = project_root / ".kmm-skills"
    if not skills_file.exists():
        return None
    try:
        data = json.loads(skills_file.read_text(encoding="utf-8"))
        return data.get("version")
    except (json.JSONDecodeError, OSError):
        return None


def validate_skills_version_pin(project_root: Path) -> list[dict]:
    skills_file = project_root / ".kmm-skills"
    if not skills_file.exists():
        return [{
            "source": "skills_version_pin",
            "severity": "HIGH",
            "type": "missing_version_pin",
            "file": ".kmm-skills",
            "line": 0,
            "message": "Missing .kmm-skills version pin. Pin the skills collection to a release tag before running governance.",
        }]

    version = read_skills_version(project_root)
    if not version:
        return [{
            "source": "skills_version_pin",
            "severity": "HIGH",
            "type": "invalid_version_pin",
            "file": ".kmm-skills",
            "line": 0,
            "message": "Malformed .kmm-skills file. Pin the skills collection to a release tag such as v1.25.11.",
        }]

    if not SKILLS_VERSION_PATTERN.match(version):
        return [{
            "source": "skills_version_pin",
            "severity": "HIGH",
            "type": "mutable_version_pin",
            "file": ".kmm-skills",
            "line": 0,
            "message": f"Mutable skills ref '{version}' detected. Use a release tag such as v1.25.11 instead of a branch or alias.",
        }]

    return []


def run_scan_violations(project_root: Path) -> list[dict]:
    if not SCAN_VIOLATIONS.exists():
        return []
    result = subprocess.run(
        [sys.executable, str(SCAN_VIOLATIONS), str(project_root), "--json"],
        capture_output=True,
        text=True,
    )
    if not result.stdout.strip():
        return []
    try:
        raw = json.loads(result.stdout)
    except json.JSONDecodeError:
        return []
    findings = []
    for f in raw:
        findings.append({
            "source": "scan_design_violations",
            "severity": _SCAN_SEVERITY_MAP.get(f.get("severity", "warning"), "MEDIUM"),
            "type": f.get("type", "unknown"),
            "file": f.get("file", ""),
            "line": f.get("line", 0),
            "message": f.get("message", ""),
        })
    return findings


def run_audit_project(project_root: Path) -> list[dict]:
    if not AUDIT_PROJECT.exists():
        return []
    result = subprocess.run(
        [sys.executable, str(AUDIT_PROJECT), str(project_root)],
        capture_output=True,
        text=True,
    )
    findings = []
    for line in result.stdout.splitlines():
        line = line.strip()
        if not line.startswith("- "):
            continue
        label_path = line[2:]
        if ": " in label_path:
            label, path = label_path.split(": ", 1)
        else:
            label, path = label_path, ""
        findings.append({
            "source": "audit_project",
            "severity": "HIGH",
            "type": label,
            "file": path,
            "line": 0,
            "message": f"Architecture smell: {label}",
        })
    return findings


def print_report(findings: list[dict], threshold: str, version: str | None) -> None:
    if version:
        print(f"KMM Skills governance — targeting v{version}")
    else:
        print("KMM Skills governance — no .kmm-skills version file found")
    print()

    if not findings:
        print("✅  No violations found.")
        return

    for sev in ("HIGH", "MEDIUM", "LOW"):
        group = [f for f in findings if f["severity"] == sev]
        if not group:
            continue
        icon = {"HIGH": "❌", "MEDIUM": "⚠️ ", "LOW": "ℹ️ "}[sev]
        print(f"{icon} {sev} ({len(group)})")
        for f in group:
            loc = f"{f['file']}:{f['line']}" if f.get("line") else f.get("file", "")
            print(f"   {f['type']}: {loc}")
            if f.get("message"):
                print(f"       {f['message']}")
        print()


def main() -> int:
    parser = argparse.ArgumentParser(description="KMM skills governance check")
    parser.add_argument("project_root", type=Path)
    parser.add_argument(
        "--fail-on",
        choices=["HIGH", "MEDIUM", "LOW"],
        default="HIGH",
        help="Minimum severity that causes a non-zero exit (default: HIGH)",
    )
    parser.add_argument(
        "--json",
        action="store_true",
        dest="json_output",
        help="Emit findings as JSON array",
    )
    args = parser.parse_args()

    root = args.project_root.resolve()
    if not root.exists():
        print(f"error: {root} does not exist", file=sys.stderr)
        return 2

    threshold = SEVERITY_RANK[args.fail_on]
    version = read_skills_version(root)

    all_findings = validate_skills_version_pin(root) + run_scan_violations(root) + run_audit_project(root)
    failing = [f for f in all_findings if SEVERITY_RANK.get(f["severity"], 0) >= threshold]

    if args.json_output:
        print(json.dumps(all_findings, indent=2))
        return 1 if failing else 0

    print_report(all_findings, args.fail_on, version)

    if failing:
        print(f"❌  {len(failing)} finding(s) at or above {args.fail_on} — governance check failed.")
        return 1

    print(f"✅  No findings at {args.fail_on} level or above.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
