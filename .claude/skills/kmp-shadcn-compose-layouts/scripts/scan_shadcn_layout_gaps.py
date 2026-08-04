#!/usr/bin/env python3
"""
scan_shadcn_layout_gaps.py — scan a KMP project's Compose source for shadcn-compose
LAYOUT-shape gaps: places a screen hand-rolls a pattern the library already ships a
dedicated composable for. Deliberately scoped to structural/layout gaps, not 1:1
component-name swaps (kmp-audit's own _detect_raw_component_bypass already
covers "raw Button() instead of ShadcnButton()" — this script doesn't duplicate it).

Detects:
  unwrapped_form_field         2+ label+input pairs in a file with no ShadcnField/
                                ShadcnFieldGroup wrapper anywhere in it
  hand_rolled_table            A LazyColumn/Column of repeated Row-of-ShadcnText rows
                                with no ShadcnTable anywhere in the file
  admin_shell_missing_sidebar  A nav-rail-width Column (180-340dp) beside content with
                                no ShadcnSidebar-family usage anywhere in the file
  login_form_missing_card      A Login/SignIn/Auth-named file with form fields + a
                                submit button but no ShadcnCard wrapper

All four are heuristic (regex/line-count based, not a real Kotlin parser) — a finding is
a strong prompt to look, not an automatic truth. False positives are possible on unusual
layouts, same caveat kmp-compose-design-system's own scan_design_violations.py documents.

Usage:
  python3 scan_shadcn_layout_gaps.py <project_root>
  python3 scan_shadcn_layout_gaps.py <project_root> --json
  python3 scan_shadcn_layout_gaps.py <project_root> --file path/to/Screen.kt

Exit codes:
  0 — no findings
  1 — findings present
  2 — project root does not exist
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

# ── Shared skip rules (matches kmp-audit / scan_design_violations.py conventions) ──────

_SKIP_NAME_SUFFIXES = ("ScreenshotTest.kt", "Test.kt", "Preview.kt")
_SKIP_DIR_FRAGMENTS = {
    "build", ".gradle", ".git", "vendor", "third_party", "node_modules",
    ".idea", ".kotlin", "kotlin-js-store",
    ".claude", ".codex", ".cursor", ".continue", "copilot",
}
# shadcn-compose's own component source defines these patterns — never a violation there.
_SKIP_PATH_FRAGMENTS = {"shadcncompose/components", "shadcncompose\\components"}


def _should_skip(path: Path) -> bool:
    if any(path.name.endswith(s) for s in _SKIP_NAME_SUFFIXES):
        return True
    posix = path.as_posix().lower()
    if any(frag in posix for frag in _SKIP_PATH_FRAGMENTS):
        return True
    parts = {p.lower() for p in path.parts}
    return bool(parts & _SKIP_DIR_FRAGMENTS)


# ── Detector patterns ───────────────────────────────────────────────────────────────

_LABEL_RE = re.compile(r"\b(?:ShadcnLabel|ShadcnFieldLabel)\s*\(")
_INPUT_RE = re.compile(r"\b(?:ShadcnTextField|ShadcnTextarea|ShadcnSelect|ShadcnCheckbox|ShadcnCombobox)\s*\(")
_FIELD_WRAPPER_RE = re.compile(r"\b(?:ShadcnField|ShadcnFieldGroup)\s*\(")

_ROW_OPEN_RE = re.compile(r"\bRow\s*[({]")
_SHADCN_TEXT_RE = re.compile(r"\bShadcnText\s*\(")
_LAZY_OR_COLUMN_RE = re.compile(r"\b(?:LazyColumn|Column)\s*[({]")
_TABLE_RE = re.compile(r"\bShadcnTable\s*\(")

_NAV_WIDTH_RE = re.compile(r"\.width\s*\(\s*(\d+(?:\.\d+)?)\s*\.dp")
_SIDEBAR_RE = re.compile(r"\bShadcnSidebar\w*\s*\(")
_CALL_OPEN_RE = re.compile(r"^[A-Za-z][A-Za-z0-9_.]*\s*\(")
_ROW_LINE_RE = re.compile(r"\bRow\s*[({]")

_LOGIN_NAME_RE = re.compile(r"(?i)login|signin|sign_in|auth")
_BUTTON_RE = re.compile(r"\bShadcnButton\s*\(")
_CARD_RE = re.compile(r"\bShadcnCard\s*\(")

_MIN_NAV_WIDTH_DP = 180
_MAX_NAV_WIDTH_DP = 340


def _finding(ftype: str, severity: str, path: Path, line: int, code: str, message: str) -> dict:
    return {
        "type": ftype,
        "severity": severity,
        "file": str(path),
        "line": line,
        "code": code,
        "message": message,
    }


def _scan_unwrapped_form_field(path: Path, text: str, lines: list[str]) -> list[dict]:
    if _FIELD_WRAPPER_RE.search(text):
        return []
    label_hits = list(_LABEL_RE.finditer(text))
    input_hits = list(_INPUT_RE.finditer(text))
    if len(label_hits) < 2 or len(input_hits) < 2:
        return []
    first_line = text[: input_hits[0].start()].count("\n") + 1
    return [_finding(
        "unwrapped_form_field", "warning", path, first_line,
        lines[first_line - 1].strip() if first_line - 1 < len(lines) else "",
        f"{len(label_hits)} label(s) + {len(input_hits)} input(s) with no ShadcnField/"
        "ShadcnFieldGroup wrapper anywhere in this file. Wrap each label+control pair in "
        "ShadcnField { } inside a ShadcnFieldGroup { } — see kmp-shadcn-compose-layouts "
        "Step 1/2 for the verified recipe (consistent spacing, FieldDescription/FieldError slots).",
    )]


def _scan_hand_rolled_table(path: Path, text: str, lines: list[str]) -> list[dict]:
    if _TABLE_RE.search(text):
        return []
    if not _LAZY_OR_COLUMN_RE.search(text):
        return []
    row_hits = list(_ROW_OPEN_RE.finditer(text))
    text_hits = list(_SHADCN_TEXT_RE.finditer(text))
    if len(row_hits) < 3 or len(text_hits) < 6:
        return []
    first_line = text[: row_hits[0].start()].count("\n") + 1
    return [_finding(
        "hand_rolled_table", "warning", path, first_line,
        lines[first_line - 1].strip() if first_line - 1 < len(lines) else "",
        f"{len(row_hits)} Row(...) blocks and {len(text_hits)} ShadcnText(...) calls in a "
        "Column/LazyColumn, with no ShadcnTable anywhere in this file — looks like a "
        "hand-rolled table. Consider ShadcnTable + ShadcnTableHeaderRow/ShadcnTableRow + "
        "ShadcnTableHeadCell/ShadcnTableCell instead — see kmp-shadcn-compose-layouts Step 3.",
    )]


def _enclosing_row_with_siblings(lines: list[str], width_line_idx: int) -> tuple[int, int] | None:
    """Walk back from a `.width(N.dp)` line to the nearest enclosing `Row(` whose body has
    2+ indentation-level siblings (a real nav-rail + content shape, not just one sized
    child). Returns (row_line_idx, sibling_count) or None. Relies on ktlint-consistent
    indentation, not a full brace parser -- a deliberate, documented simplification."""
    child_indent = None
    row_line_idx = None
    for j in range(width_line_idx, max(-1, width_line_idx - 15), -1):
        line = lines[j]
        stripped = line.lstrip()
        if child_indent is None and _CALL_OPEN_RE.match(stripped):
            child_indent = len(line) - len(stripped)
        if _ROW_LINE_RE.search(line):
            indent = len(line) - len(line.lstrip())
            if child_indent is not None and indent < child_indent:
                row_line_idx = j
                break
    if row_line_idx is None or child_indent is None:
        return None

    depth = 0
    started = False
    sibling_count = 0
    for j in range(row_line_idx, min(len(lines), row_line_idx + 200)):
        line = lines[j]
        for ch in line:
            if ch == "{":
                depth += 1
                started = True
            elif ch == "}":
                depth -= 1
        stripped = line.lstrip()
        indent = len(line) - len(stripped)
        if started and indent == child_indent and _CALL_OPEN_RE.match(stripped):
            sibling_count += 1
        if started and depth <= 0:
            break
    return (row_line_idx, sibling_count)


def _scan_admin_shell_missing_sidebar(path: Path, text: str, lines: list[str]) -> list[dict]:
    if _SIDEBAR_RE.search(text):
        return []
    for m in _NAV_WIDTH_RE.finditer(text):
        width = float(m.group(1))
        if not (_MIN_NAV_WIDTH_DP <= width <= _MAX_NAV_WIDTH_DP):
            continue
        width_line_idx = text[: m.start()].count("\n")
        result = _enclosing_row_with_siblings(lines, width_line_idx)
        if result is None:
            continue
        row_line_idx, sibling_count = result
        if sibling_count < 2:
            continue
        line = row_line_idx + 1
        return [_finding(
            "admin_shell_missing_sidebar", "warning", path, line,
            lines[line - 1].strip() if line - 1 < len(lines) else "",
            f"Row with {sibling_count} direct children, one fixed at {m.group(1)}.dp "
            f"(nav-rail range, {_MIN_NAV_WIDTH_DP}-{_MAX_NAV_WIDTH_DP}dp), no "
            "ShadcnSidebar-family usage anywhere in this file. Consider "
            "ShadcnSidebarProvider + ShadcnSidebar + ShadcnSidebarInset instead of a "
            "hand-rolled Row — see kmp-shadcn-compose-layouts Step 4a. If this shell "
            "genuinely needs a responsive breakpoint-driven drawer collapse (ShadcnSidebar "
            "doesn't do that on its own), the hand-rolled approach is the documented "
            "exception — see Step 4b — not a gap.",
        )]
    return []


def _scan_login_form_missing_card(path: Path, text: str, lines: list[str]) -> list[dict]:
    if not _LOGIN_NAME_RE.search(path.name):
        return []
    if _CARD_RE.search(text):
        return []
    input_hits = len(_INPUT_RE.findall(text))
    if input_hits < 2 or not _BUTTON_RE.search(text):
        return []
    btn_match = _BUTTON_RE.search(text)
    line = text[: btn_match.start()].count("\n") + 1
    return [_finding(
        "login_form_missing_card", "warning", path, line,
        lines[line - 1].strip() if line - 1 < len(lines) else "",
        f"{path.name} looks like a login/auth form ({input_hits} input(s) + a submit "
        "button) with no ShadcnCard wrapper. Real shadcn's login-01 block centers the "
        "form in a width-capped ShadcnCard — see kmp-shadcn-compose-layouts Step 1.",
    )]


_DETECTORS = (
    _scan_unwrapped_form_field,
    _scan_hand_rolled_table,
    _scan_admin_shell_missing_sidebar,
    _scan_login_form_missing_card,
)


def scan_file(path: Path) -> list[dict]:
    try:
        text = path.read_text(encoding="utf-8", errors="replace")
    except OSError:
        return []
    lines = text.splitlines()
    findings: list[dict] = []
    for detector in _DETECTORS:
        findings.extend(detector(path, text, lines))
    return findings


def scan(project_root: Path, single_file: Path | None = None) -> list[dict]:
    if single_file:
        return [] if _should_skip(single_file) else scan_file(single_file)

    all_findings: list[dict] = []
    for kt_file in sorted(project_root.rglob("*.kt")):
        if _should_skip(kt_file):
            continue
        all_findings.extend(scan_file(kt_file))
    return all_findings


def _print_summary(findings: list[dict], project_root: Path) -> None:
    if not findings:
        print("✅  No shadcn-compose layout gaps found.")
        return

    by_file: dict[str, list[dict]] = {}
    for f in findings:
        by_file.setdefault(f["file"], []).append(f)

    print(f"shadcn-compose layout gaps — {len(findings)} total\n")

    for filepath, file_findings in by_file.items():
        try:
            rel = Path(filepath).relative_to(project_root)
        except ValueError:
            rel = Path(filepath)
        print(f"  {rel}  ({len(file_findings)} issue{'s' if len(file_findings) != 1 else ''})")
        for f in file_findings:
            print(f"    ⚠️  L{f['line']:>4}  [{f['type']}]  {f['message']}")
            if f["code"]:
                print(f"              {f['code'].strip()}")
        print()


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Scan KMP Compose source for shadcn-compose layout-shape gaps."
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
