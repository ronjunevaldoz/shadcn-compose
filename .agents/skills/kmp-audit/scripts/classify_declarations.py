#!/usr/bin/env python3
"""
classify_declarations.py — classify every Kotlin declaration as
core / sugar / helper / sample-local / deprecated.

This is a *classifier*, not a smell detector. `_detect_god_utils_file` in
audit_project.py asks "is this file named Utils.kt and is it a grab-bag?" — a filename
question. This asks the taxonomy's real question, per declaration: what role does this
thing actually play in the API surface?

The categories and their Kotlin mechanisms come from kmp-code-quality's
"Code categorization" section, not from anything invented here:

  | Category       | Mechanism                   | How it's decided here          |
  |----------------|-----------------------------|--------------------------------|
  | deprecated     | @Deprecated                 | exact — annotation present     |
  | sample-local   | sample/demo module          | exact — path                   |
  | helper         | internal / private          | exact — visibility keyword     |
  | sugar          | public, delegates into core | heuristic — see below          |
  | core           | public, everything else      | residual                       |

Three of the five are exactly decidable. `sugar` is the only judgment call, and it is
deliberately conservative: a public declaration counts as sugar only when its body is a
single expression that calls something else, which is the mechanical form of the rule
"a sugar function must call the same core function a caller could reach directly."
Confidence is `high` when the callee is declared in the same file (provably a
delegation), `medium` when it isn't (could be delegating to another module, or could be
a genuine one-liner implementation).

Regex-based, single-file scope — no compiler, no cross-module resolution. Treat the
output as a map to review, not a verdict.

Exit codes:
  0 — classified (always, unless --strict and a problem row was emitted)
  1 — --strict and at least one problem row
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from collections import Counter
from pathlib import Path

_EXCLUDED_DIR_PARTS = {"build", ".gradle", ".git", "node_modules", "__pycache__"}
_SAMPLE_PATH_RE = re.compile(r"(^|/)(samples?|demo|demos|example|examples)(/|$)", re.IGNORECASE)
_TEST_PATH_RE = re.compile(r"(^|/)(test|androidTest|commonTest|jvmTest|iosTest)[^/]*(/|$)")

_ANNOTATION_RE = re.compile(r"^\s*@(\w+)")
_DEPRECATED_RE = re.compile(r"^\s*@Deprecated\b")
_REPLACE_WITH_RE = re.compile(r"\bReplaceWith\s*\(")

# A declaration line: optional visibility, optional modifier soup, then the kind + name.
_DECL_RE = re.compile(
    r"^(?P<indent>\s*)"
    r"(?:(?P<vis>public|internal|private|protected)\s+)?"
    r"(?:(?:inline|infix|operator|suspend|expect|actual|abstract|open|override|"
    r"external|sealed|data|value|annotation|enum|tailrec|const|lateinit|companion)\s+)*"
    r"(?P<kind>class|interface|object|fun|val|var)\s+"
    r"(?:<[^>]*>\s*)?"
    r"(?:(?P<receiver>[\w.]+)(?:<[^>]*>)?\.)?"
    r"(?P<name>\w+)"
)

# Body is a single expression that is (mostly) one call: `= foo(...)`, `= foo(...).bar()`
_EXPRESSION_BODY_CALL_RE = re.compile(r"=\s*(?P<callee>[\w.]+)\s*\(")


def _is_excluded(path: Path) -> bool:
    return any(part in _EXCLUDED_DIR_PARTS for part in path.parts)


def _declared_names(text: str) -> set[str]:
    names = set()
    for line in text.splitlines():
        m = _DECL_RE.match(line)
        if m:
            names.add(m.group("name"))
    return names


def classify_file(path: Path, rel: str) -> list[dict]:
    try:
        text = path.read_text(encoding="utf-8", errors="ignore")
    except OSError:
        return []

    local_names = _declared_names(text)
    is_sample = bool(_SAMPLE_PATH_RE.search(rel))
    rows: list[dict] = []
    pending_annotations: list[str] = []
    # Brace depth inside a function body. A `val`/`var` in there is a local variable, not
    # API surface — without this, every `val x = ...` inside a method body was classified
    # as `core`, which in real code buries the actual declarations in noise.
    fun_body_depth = 0

    for i, line in enumerate(text.splitlines(), 1):
        stripped = line.strip()
        if not stripped or stripped.startswith(("//", "*", "/*")):
            continue

        if fun_body_depth > 0:
            fun_body_depth += line.count("{") - line.count("}")
            continue

        ann = _ANNOTATION_RE.match(line)
        if ann:
            pending_annotations.append(stripped)
            continue

        m = _DECL_RE.match(line)
        if not m:
            if stripped:
                pending_annotations = []
            continue

        annotations = pending_annotations
        pending_annotations = []

        deprecated_line = next((a for a in annotations if _DEPRECATED_RE.match(a)), None)
        # Kotlin's default visibility is public when the keyword is omitted.
        visibility = m.group("vis") or "public"
        name, kind = m.group("name"), m.group("kind")

        problem = ""
        if deprecated_line is not None:
            classification, confidence, why = "deprecated", "high", "@Deprecated present"
            if not _REPLACE_WITH_RE.search(deprecated_line):
                problem = (
                    "deprecated without ReplaceWith — the taxonomy requires a real "
                    "migration path, otherwise this is dead code, not a deprecation"
                )
        elif is_sample:
            classification, confidence, why = "sample-local", "high", "in a sample/demo path"
            if visibility == "public" and kind in ("class", "interface", "object", "fun"):
                problem = (
                    "public declaration in a sample module — sample code should never be "
                    "part of a callable/published surface"
                )
        elif visibility in ("internal", "private", "protected"):
            classification, confidence, why = "helper", "high", f"{visibility} visibility"
        else:
            call = _EXPRESSION_BODY_CALL_RE.search(line)
            if call and kind == "fun":
                callee = call.group("callee").split(".")[-1]
                if callee == name:
                    # `fun request(url) = request(url, 30_000)` — a same-name delegation
                    # is the textbook overload-sugar shape, not a weaker signal.
                    classification, confidence = "sugar", "high"
                    why = f"overload delegating to another {name}()"
                elif callee in local_names:
                    classification, confidence = "sugar", "high"
                    why = f"expression body delegating to {callee}() in the same file"
                else:
                    classification, confidence = "sugar", "medium"
                    why = f"single-expression body calling {callee}()"
            else:
                classification, confidence, why = "core", "medium", "public, not a delegation"

        rows.append({
            "file": rel, "line": i, "name": name, "kind": kind,
            "visibility": visibility, "classification": classification,
            "confidence": confidence, "why": why, "problem": problem,
        })

        # A `fun` with a block body opens a scope whose contents are locals, not API.
        # A class/object body is NOT skipped — its members are exactly what we classify.
        if kind == "fun":
            opened = line.count("{") - line.count("}")
            if opened > 0:
                fun_body_depth = opened
    return rows


def classify(root: Path) -> list[dict]:
    rows: list[dict] = []
    for path in sorted(root.rglob("*.kt")):
        if _is_excluded(path):
            continue
        rel = str(path.relative_to(root))
        if _TEST_PATH_RE.search(rel):
            continue
        rows.extend(classify_file(path, rel))
    return rows


def render(rows: list[dict]) -> str:
    counts = Counter(r["classification"] for r in rows)
    problems = [r for r in rows if r["problem"]]
    out = ["DECLARATION CLASSIFICATION", ""]
    if not rows:
        return "No Kotlin declarations found."
    for cat in ("core", "sugar", "helper", "sample-local", "deprecated"):
        if counts.get(cat):
            out.append(f"  {cat:<13} {counts[cat]:>4}")
    out.append("")
    low = [r for r in rows if r["confidence"] == "medium"]
    out.append(f"  {len(rows)} declarations, {len(low)} classified with medium confidence")
    if problems:
        out += ["", f"PROBLEMS ({len(problems)})", ""]
        for r in problems:
            out.append(f"  {r['file']}:{r['line']}  {r['name']} — {r['problem']}")
    return "\n".join(out)


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n")[1])
    ap.add_argument("root", nargs="?", default=".")
    ap.add_argument("--json", action="store_true")
    ap.add_argument("--strict", action="store_true", help="exit 1 if any problem row")
    args = ap.parse_args()

    rows = classify(Path(args.root).resolve())
    if args.json:
        print(json.dumps(rows, indent=2))
    else:
        print(render(rows))
    return 1 if args.strict and any(r["problem"] for r in rows) else 0


if __name__ == "__main__":
    sys.exit(main())
