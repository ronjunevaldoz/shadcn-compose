#!/usr/bin/env python3
"""generate_structure_diagram.py — render actual vs canonical KMP module structure
so a developer can visually verify the project still matches the App (6-layer
feature/*) or Library (library/library-testing/sample) convention this collection
scaffolds.

Informational only — never blocks. For hard gates on layer violations, use
audit_project.py's module-layer-violation and bare-core-module checks.
"""
from __future__ import annotations

import argparse
import re
from pathlib import Path

_LAYER_ORDER = ("model", "api", "domain", "data", "presenter", "ui")
_LIBRARY_MODULES = ("library", "library-testing", "sample")
_EXCLUDED_DIRS = {".git", "build", ".gradle", "node_modules", "vendor", "third_party", ".idea"}
_FEATURE_RE = re.compile(r"^feature/(\w+)/(\w+)$")


def _is_excluded(path: Path, root: Path) -> bool:
    parts = path.relative_to(root).parts
    return any(part in _EXCLUDED_DIRS for part in parts)


def _find_modules(root: Path) -> list[str]:
    modules = []
    for build_file in root.rglob("build.gradle.kts"):
        if _is_excluded(build_file, root):
            continue
        rel = build_file.parent.relative_to(root).as_posix()
        if rel == ".":
            continue
        modules.append(rel)
    return sorted(modules)


def _detect_project_type(root: Path) -> str:
    if (root / "library").is_dir() or (root / "library-testing").is_dir():
        return "library"
    return "app"


def gather(root: Path) -> dict:
    modules = _find_modules(root)
    project_type = _detect_project_type(root)
    result: dict = {"project_type": project_type, "modules": modules}

    if project_type == "app":
        features: dict[str, set[str]] = {}
        core_modules: list[str] = []
        other_modules: list[str] = []
        for rel in modules:
            m = _FEATURE_RE.match(rel)
            if m:
                features.setdefault(m.group(1), set()).add(m.group(2))
            elif rel == "core" or rel.startswith("core/"):
                core_modules.append(rel)
            else:
                other_modules.append(rel)
        result["features"] = features
        result["core_modules"] = core_modules
        result["other_modules"] = other_modules
    else:
        library_modules = [
            m for m in modules if m in _LIBRARY_MODULES or any(m.startswith(f"{n}/") for n in _LIBRARY_MODULES)
        ]
        other_modules = [m for m in modules if m not in library_modules]
        result["library_modules"] = library_modules
        result["other_modules"] = other_modules

    return result


def build_diagram(state: dict) -> str:
    project_type = state["project_type"]
    lines = [f"# Project structure — {project_type.upper()}", ""]

    if project_type == "app":
        features: dict[str, set[str]] = state["features"]
        lines.append("## feature/*")
        if not features:
            lines.append("- (none found)")
        for feature in sorted(features):
            present = features[feature]
            lines.append(f"- feature/{feature}/")
            for layer in _LAYER_ORDER:
                mark = "OK" if layer in present else "MISSING"
                lines.append(f"    :{layer:<10} {mark}")
            for layer in sorted(present - set(_LAYER_ORDER)):
                lines.append(f"    :{layer:<10} UNEXPECTED (not part of the 6-layer contract)")

        lines.append("")
        lines.append("## core/*")
        core_modules = state["core_modules"]
        if core_modules:
            lines.extend(f"- {rel}" for rel in core_modules)
        else:
            lines.append("- (none found)")

        if state["other_modules"]:
            lines.append("")
            lines.append("## other modules")
            lines.extend(f"- {rel}" for rel in state["other_modules"])
    else:
        lines.append("## Expected library layout")
        present_names = {rel.split("/", 1)[0] for rel in state["library_modules"]}
        for name in _LIBRARY_MODULES:
            mark = "OK" if name in present_names else "MISSING"
            lines.append(f"- {name}/  {mark}")

        if state["other_modules"]:
            lines.append("")
            lines.append("## other modules")
            lines.extend(f"- {rel}" for rel in state["other_modules"])

    lines.append("")
    lines.append(
        "Informational only — for hard gates, see audit_project.py's "
        "module-layer-violation and bare-core-module checks."
    )
    return "\n".join(lines)


def build_mermaid(state: dict) -> str:
    lines = ["```mermaid", "graph TD"]
    project_type = state["project_type"]

    if project_type == "app":
        for feature, present in sorted(state["features"].items()):
            safe = re.sub(r"\W", "_", feature)
            lines.append(f'  subgraph feat_{safe}["feature/{feature}"]')
            prev = None
            for layer in _LAYER_ORDER:
                node = f"{safe}_{layer}"
                status = "" if layer in present else " (missing)"
                lines.append(f'    {node}["{layer}{status}"]')
                if prev:
                    lines.append(f"    {prev} --> {node}")
                prev = node
            lines.append("  end")
        for rel in state["core_modules"]:
            safe = re.sub(r"\W", "_", rel)
            lines.append(f'  {safe}["{rel}"]')
    else:
        prev = None
        for name in _LIBRARY_MODULES:
            present = any(m == name or m.startswith(f"{name}/") for m in state["library_modules"])
            status = "" if present else " (missing)"
            lines.append(f'  {name}["{name}{status}"]')
            if prev:
                lines.append(f"  {prev} --> {name}")
            prev = name

    lines.append("```")
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description="Visualize actual vs canonical KMP module structure.")
    parser.add_argument("project_root", type=Path, help="Path to the KMP project root")
    parser.add_argument("--mermaid", action="store_true", help="Also emit a Mermaid graph block")
    args = parser.parse_args()
    root = args.project_root.resolve()
    state = gather(root)
    print(build_diagram(state))
    if args.mermaid:
        print()
        print(build_mermaid(state))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
