#!/usr/bin/env python3
"""
scan_design_violations.py — scan a KMP project's Compose source files for
design-system usage violations.

Detects:
  hardcoded_color      Color(0xFF...) or Color(r, g, b) — use appTheme.colors.*
  hardcoded_dp         Literal dp values in layout modifiers — use AppSpacing tokens
  hardcoded_string     User-facing string literals in Compose UI — use strings.xml / stringResource()
  material_theme       MaterialTheme.colors/typography/shapes — use appTheme.*
  direct_textstyle     TextStyle(...) construction — use AppTextStyle enum
  nested_container     Card { Card { or Surface { Surface { — redundant nesting
  layout_inconsistency Mixed flat/card/tabbed patterns across *Content.kt files in the same feature ui/ dir
  preview_coverage     Missing preview stub, multi-device preview coverage, or Roborazzi screenshot test

Usage:
  python3 scan_design_violations.py <project_root>
  python3 scan_design_violations.py <project_root> --json
  python3 scan_design_violations.py <project_root> --file path/to/Foo.kt

Exit codes:
  0 — no violations found
  1 — violations found
  2 — project root does not exist
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from collections import Counter
from pathlib import Path


# ── Violation patterns (line-by-line) ───────────────────────────────────────

_PATTERNS: list[tuple[str, str, str, re.Pattern]] = [
    (
        "hardcoded_color",
        "error",
        "Use appTheme.colors.* token instead of a hardcoded Color()",
        re.compile(
            r"\bColor\s*\(\s*0x[A-Fa-f0-9]{6,8}"                         # Color(0xFFRRGGBB)
            r"|\bColor\s*\(\s*\d+(?:\.\d+)?f?\s*,\s*\d+(?:\.\d+)?f?\s*,",  # Color(r, g, b)
        ),
    ),
    (
        "hardcoded_dp",
        "warning",
        "Use AppSpacing tokens (appTheme.spacing.*) instead of literal dp values",
        re.compile(
            # Modifier.padding/height/width/size/offset with a literal ≥ 2 dp
            r"(?:\.padding|\.height|\.width|\.size|\.offset)\s*\(\s*"
            r"(?:[2-9]|\d{2,})(?:\.\d+)?\.dp"
            # Spacer with literal dp
            r"|Spacer\s*\(\s*modifier\s*=\s*Modifier\s*\.\s*(?:height|width)\s*\(\s*"
            r"(?:[2-9]|\d{2,})(?:\.\d+)?\.dp",
        ),
    ),
    (
        "hardcoded_string",
        "error",
        "Use strings.xml / stringResource() instead of hardcoded user-facing text",
        re.compile(
            r"\b(?:Text|AppText|AppButton|AppBadge|AppChip|AppLabel|AppTextField|Button|OutlinedButton|FilledButton|ElevatedButton|TextField|OutlinedTextField|BasicTextField)\s*\([^)]*\"(?:[^\"\\]|\\.)+\""
            r"|\b(?:contentDescription|label|title|placeholder|supportingText)\s*=\s*\"(?:[^\"\\]|\\.)+\"",
        ),
    ),
    (
        "material_theme",
        "error",
        "Use appTheme.colors / appTheme.typography / appTheme.shapes instead of MaterialTheme.*",
        re.compile(r"\bMaterialTheme\s*\.\s*(?:colors|typography|shapes)\s*\."),
    ),
    (
        "direct_textstyle",
        "error",
        "Use AppTextStyle enum values instead of constructing TextStyle() directly",
        # Negative lookbehind so AppTextStyle( is not flagged
        re.compile(r"(?<![A-Za-z])TextStyle\s*\("),
    ),
]

# Containers where nesting is a structural smell.
# Matches both `Card(` and `Card {` (Kotlin trailing-lambda form).
_CONTAINER_OPEN_RE = re.compile(r"\b(Card|Surface)\b\s*[\({]")

# ── Layout classification ─────────────────────────────────────────────────────

_TABBED_RE = re.compile(r"\bTabRow\b")
# AppCard( or Card( — excludes Card used as a type annotation (Card:)
_CARD_LAYOUT_RE = re.compile(r"\b(?:App)?Card\s*\(")

_MULTI_DEVICE_WIDTHS = ("360", "673", "1280")

# Files / directories that are design-system source — allowed to use primitives directly
_SKIP_NAME_SUFFIXES = (
    "Styles.kt", "Theme.kt", "Tokens.kt",
    "Colors.kt", "Typography.kt", "Spacing.kt", "Shapes.kt",
    "ScreenshotTest.kt",
)
_SKIP_DIR_FRAGMENTS = {"designsystem", "design_system", "theme"}


def _should_skip(path: Path) -> bool:
    if any(path.name.endswith(s) for s in _SKIP_NAME_SUFFIXES):
        return True
    parts = {p.lower() for p in path.parts}
    return bool(parts & _SKIP_DIR_FRAGMENTS)


def _scan_patterns(path: Path, lines: list[str]) -> list[dict]:
    findings = []
    for lineno, line in enumerate(lines, start=1):
        stripped = line.strip()
        if stripped.startswith("//") or stripped.startswith("*"):
            continue
        for vtype, severity, message, pattern in _PATTERNS:
            if pattern.search(line):
                findings.append({
                    "type": vtype,
                    "severity": severity,
                    "file": str(path),
                    "line": lineno,
                    "code": line.rstrip(),
                    "message": message,
                })
    return findings


def _scan_nested_containers(path: Path, lines: list[str]) -> list[dict]:
    """Flag nested Card/Surface using a brace-depth stack."""
    findings = []
    depth = 0
    # stack: (container_type, brace_depth_when_opened)
    stack: list[tuple[str, int]] = []

    for lineno, line in enumerate(lines, start=1):
        stripped = line.strip()
        if stripped.startswith("//") or stripped.startswith("*"):
            continue

        # Detect container opens BEFORE counting braces on this line
        # (the opening { typically follows the container call on the same line)
        for m in _CONTAINER_OPEN_RE.finditer(line):
            ctype = m.group(1)
            open_types = [t for t, _ in stack]
            if ctype in open_types:
                findings.append({
                    "type": "nested_container",
                    "severity": "warning",
                    "file": str(path),
                    "line": lineno,
                    "code": line.rstrip(),
                    "message": (
                        f"Nested {ctype}() — remove the outer wrapper or "
                        "restructure with a flat layout"
                    ),
                })
            stack.append((ctype, depth))

        # Count braces
        for ch in line:
            if ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
                # Pop containers that have now closed
                while stack and stack[-1][1] >= depth:
                    stack.pop()

    return findings


def _has_preview_stub(content_file: Path) -> bool:
    preview_name = f"{content_file.stem}Preview.kt"
    return content_file.with_name(preview_name).exists() or (
        content_file.parent / "previews" / preview_name
    ).exists()


def _preview_file_for_content(content_file: Path) -> Path | None:
    preview_name = f"{content_file.stem}Preview.kt"
    same_dir = content_file.with_name(preview_name)
    if same_dir.exists():
        return same_dir
    nested = content_file.parent / "previews" / preview_name
    if nested.exists():
        return nested
    return None


def _screenshot_test_for_content(content_file: Path) -> Path | None:
    screenshot_name = f"{content_file.stem}ScreenshotTest.kt"
    base = Path(
        content_file.as_posix().replace("/src/commonMain/kotlin/", "/src/jvmTest/kotlin/")
    )
    candidates = (
        base.with_name(screenshot_name),
        base.parent / "previews" / screenshot_name,
    )
    for candidate in candidates:
        if candidate.exists():
            return candidate
    return None


def _looks_like_feature_content(path: Path) -> bool:
    parts = path.parts
    return "feature" in parts and "ui" in parts and path.name.endswith("Content.kt")


def _looks_like_design_component(path: Path) -> bool:
    parts = path.parts
    return "components" in parts and path.name.endswith(".kt") and not path.name.endswith("Preview.kt")


def _scan_preview_coverage(project_root: Path) -> list[dict]:
    findings: list[dict] = []

    candidate_files = []
    for kt_file in sorted(project_root.rglob("*.kt")):
        if _should_skip(kt_file) and not _looks_like_design_component(kt_file):
            continue
        if _looks_like_feature_content(kt_file) or _looks_like_design_component(kt_file):
            candidate_files.append(kt_file)

    for source_file in candidate_files:
        preview_file = _preview_file_for_content(source_file)
        if preview_file is None:
            findings.append({
                "type": "preview_coverage",
                "severity": "error",
                "file": str(source_file),
                "line": 1,
                "code": source_file.name,
                "message": (
                    f"Missing preview stub for {source_file.name}. "
                    f"Create {source_file.stem}Preview.kt (or previews/{source_file.stem}Preview.kt)."
                ),
            })
        else:
            preview_text = preview_file.read_text(encoding="utf-8", errors="replace")
            if "@MultiDevicePreview" not in preview_text and not all(
                width in preview_text for width in _MULTI_DEVICE_WIDTHS
            ):
                findings.append({
                    "type": "preview_coverage",
                    "severity": "error",
                    "file": str(preview_file),
                    "line": 1,
                    "code": preview_file.name,
                    "message": (
                        "Preview file is missing multi-device coverage. "
                        "Use @MultiDevicePreview or explicit 360/673/1280 dp preview sizes."
                    ),
                })

        if _looks_like_feature_content(source_file):
            screenshot_file = _screenshot_test_for_content(source_file)
            if screenshot_file is None:
                findings.append({
                    "type": "preview_coverage",
                    "severity": "error",
                    "file": str(source_file),
                    "line": 1,
                    "code": source_file.name,
                    "message": (
                        f"Missing Roborazzi screenshot test for {source_file.name}. "
                        f"Create {source_file.stem}ScreenshotTest.kt under src/jvmTest/kotlin/."
                    ),
                })
            else:
                screenshot_text = screenshot_file.read_text(encoding="utf-8", errors="replace")
                if "captureRoboImage" not in screenshot_text:
                    findings.append({
                        "type": "preview_coverage",
                        "severity": "error",
                        "file": str(screenshot_file),
                        "line": 1,
                        "code": screenshot_file.name,
                        "message": "Roborazzi screenshot test must call captureRoboImage().",
                    })
                if not all(width in screenshot_text for width in _MULTI_DEVICE_WIDTHS):
                    findings.append({
                        "type": "preview_coverage",
                        "severity": "error",
                        "file": str(screenshot_file),
                        "line": 1,
                        "code": screenshot_file.name,
                        "message": "Roborazzi screenshot test must cover phone, tablet, and desktop sizes.",
                    })
                if "darkTheme = true" not in screenshot_text and "AppTheme(darkTheme = true)" not in screenshot_text:
                    findings.append({
                        "type": "preview_coverage",
                        "severity": "error",
                        "file": str(screenshot_file),
                        "line": 1,
                        "code": screenshot_file.name,
                        "message": "Roborazzi screenshot test must capture both light and dark variants.",
                    })

    return findings


def scan_file(path: Path) -> list[dict]:
    try:
        text = path.read_text(encoding="utf-8", errors="replace")
    except OSError:
        return []
    lines = text.splitlines()
    return _scan_patterns(path, lines) + _scan_nested_containers(path, lines)


def _classify_layout(content: str) -> str:
    """Return the dominant layout pattern for a *Content.kt file."""
    if _TABBED_RE.search(content):
        return "tabbed"
    if _CARD_LAYOUT_RE.search(content):
        return "card"
    return "flat"


def scan_layout_consistency(project_root: Path) -> list[dict]:
    """Cross-screen check: all *Content.kt files in the same ui/ dir must use the same layout pattern."""
    findings: list[dict] = []

    dir_files: dict[Path, list[Path]] = {}
    for kt_file in sorted(project_root.rglob("*Content.kt")):
        if _should_skip(kt_file):
            continue
        dir_files.setdefault(kt_file.parent, []).append(kt_file)

    for ui_dir, files in dir_files.items():
        if len(files) < 2:
            continue

        classifications: dict[Path, str] = {}
        for f in files:
            try:
                content = f.read_text(encoding="utf-8", errors="replace")
            except OSError:
                continue
            classifications[f] = _classify_layout(content)

        if len(set(classifications.values())) < 2:
            continue  # all consistent

        majority, _ = Counter(classifications.values()).most_common(1)[0]
        for f, layout in classifications.items():
            if layout != majority:
                findings.append({
                    "type": "layout_inconsistency",
                    "severity": "warning",
                    "file": str(f),
                    "line": 1,
                    "code": f"# detected layout: {layout}",
                    "message": (
                        f"Layout pattern '{layout}' differs from the rest of "
                        f"{ui_dir.name}/ (majority: '{majority}'). "
                        "All *Content.kt files in the same feature ui/ dir should use "
                        "the same top-level pattern (flat / card / tabbed)."
                    ),
                })

    return findings


def scan(project_root: Path, single_file: Path | None = None) -> list[dict]:
    if single_file:
        return [] if _should_skip(single_file) else scan_file(single_file)

    all_findings: list[dict] = []
    for kt_file in sorted(project_root.rglob("*.kt")):
        if _should_skip(kt_file):
            continue
        all_findings.extend(scan_file(kt_file))
    all_findings.extend(scan_layout_consistency(project_root))
    all_findings.extend(_scan_preview_coverage(project_root))
    return all_findings


def _print_summary(findings: list[dict], project_root: Path) -> None:
    if not findings:
        print("✅  No design violations found.")
        return

    by_file: dict[str, list[dict]] = {}
    for f in findings:
        by_file.setdefault(f["file"], []).append(f)

    errors = sum(1 for f in findings if f["severity"] == "error")
    warnings = sum(1 for f in findings if f["severity"] == "warning")
    print(
        f"Design violations — {len(findings)} total  "
        f"({errors} error{'s' if errors != 1 else ''}, "
        f"{warnings} warning{'s' if warnings != 1 else ''})\n"
    )

    sev_icon = {"error": "❌", "warning": "⚠️ "}

    for filepath, file_findings in by_file.items():
        try:
            rel = Path(filepath).relative_to(project_root)
        except ValueError:
            rel = Path(filepath)
        print(f"  {rel}  ({len(file_findings)} issue{'s' if len(file_findings) != 1 else ''})")
        for f in file_findings:
            icon = sev_icon.get(f["severity"], "  ")
            print(f"    {icon} L{f['line']:>4}  [{f['type']}]  {f['message']}")
            print(f"              {f['code'].strip()}")
        print()


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Scan KMP Compose source files for design-system usage violations."
    )
    parser.add_argument("project_root", type=Path, help="Root of the KMP project")
    parser.add_argument("--json", action="store_true", help="Output findings as JSON array")
    parser.add_argument(
        "--file", type=Path, metavar="PATH",
        help="Scan a single file instead of the whole project",
    )
    args = parser.parse_args()

    root = args.project_root.resolve()
    if not root.exists():
        print(f"error: {root} does not exist", file=sys.stderr)
        return 2

    single = args.file.resolve() if args.file else None
    findings = scan(root, single_file=single)

    if args.json:
        print(json.dumps(findings, indent=2))
    else:
        _print_summary(findings, root)

    return 1 if findings else 0


if __name__ == "__main__":
    raise SystemExit(main())
