#!/usr/bin/env python3
"""
fetch_component_signature.py — pull a Shadcn* component's REAL current signature
directly from the shadcn-compose GitHub repo, so verifying a component's real API
costs one command instead of remembering the right file path and grep pattern.

Exists because assuming a component's shape by analogy (to Compose Material, to HTML,
to another Shadcn* component in the same family) has already produced two real bugs in
a consumer project: a hallucinated `singleLine` parameter on ShadcnTextField, and
assuming the tabs component was named "ShadcnTabs" when it's actually "ShadcnTabsList".

Handles two real cases that break a naive fetch-by-guessed-filename approach:
  - A component defined in a differently-named file (ShadcnTabsList lives inside
    ShadcnTabs.kt, not ShadcnTabsList.kt) — falls back to searching every component
    file, not just the obvious one.
  - Nested parens in default parameter values (e.g. ShadcnCard's
    `header: (@Composable () -> Unit)? = null`) — uses a balanced-paren scan, not a
    single-level regex, so the extracted signature isn't truncated at the first `)`.

Usage:
    python3 fetch_component_signature.py ShadcnTextarea
    python3 fetch_component_signature.py ShadcnTabsList
"""
from __future__ import annotations

import json
import re
import sys
import urllib.request
from urllib.error import HTTPError, URLError

REPO = "ronjunevaldoz/shadcn-compose"
COMPONENTS_PATH = "shadcn/core/src/commonMain/kotlin/io/github/ronjunevaldoz/shadcncompose/components"


def _fetch(url: str) -> str:
    req = urllib.request.Request(url, headers={"User-Agent": "kmp-agent-skills"})
    with urllib.request.urlopen(req, timeout=15) as resp:
        return resp.read().decode("utf-8")


def _list_component_files() -> list[str]:
    url = f"https://api.github.com/repos/{REPO}/git/trees/main?recursive=1"
    data = json.loads(_fetch(url))
    return sorted(
        item["path"]
        for item in data["tree"]
        if item["path"].startswith(COMPONENTS_PATH) and item["path"].endswith(".kt")
    )


def _fetch_raw(path: str) -> str:
    url = f"https://raw.githubusercontent.com/{REPO}/main/{path}"
    return _fetch(url)


def _find_fun_start(text: str, name: str) -> int | None:
    pattern = re.compile(r"\bfun\s+(?:<[^>]+>\s*)?" + re.escape(name) + r"\s*\(")
    m = pattern.search(text)
    return m.start() if m else None


def _extract_signature(text: str, fun_start: int) -> str:
    paren_start = text.index("(", fun_start)
    depth = 0
    i = paren_start
    while i < len(text):
        if text[i] == "(":
            depth += 1
        elif text[i] == ")":
            depth -= 1
            if depth == 0:
                break
        i += 1
    params_end = i + 1

    rest = text[params_end : params_end + 200]
    brace_idx = rest.find("{")
    tail = rest[:brace_idx].rstrip() if brace_idx != -1 else rest.split("\n")[0]
    signature = text[fun_start:params_end] + tail

    # Prepend the KDoc immediately above, if the gap between it and `fun` is only
    # whitespace and annotations (e.g. @Composable) — never a stale, unrelated doc.
    prefix = ""
    kdoc_end = text.rfind("*/", 0, fun_start)
    if kdoc_end != -1:
        kdoc_start = text.rfind("/**", 0, kdoc_end)
        between = text[kdoc_end + 2 : fun_start]
        if kdoc_start != -1 and re.fullmatch(r"[\s\n]*(@\w+(\([^)]*\))?\s*\n?)*[\s\n]*", between):
            prefix = text[kdoc_start : kdoc_end + 2] + "\n"
    return prefix + signature


def find_signature(component_name: str) -> tuple[str, str] | None:
    """Return (file_path, signature) for the first file containing a real
    `fun <component_name>(...)` declaration."""
    obvious_path = f"{COMPONENTS_PATH}/{component_name}.kt"
    try:
        text = _fetch_raw(obvious_path)
        fun_start = _find_fun_start(text, component_name)
        if fun_start is not None:
            return obvious_path, _extract_signature(text, fun_start)
    except (HTTPError, URLError):
        pass

    for path in _list_component_files():
        if path == obvious_path:
            continue
        try:
            text = _fetch_raw(path)
        except (HTTPError, URLError):
            continue
        fun_start = _find_fun_start(text, component_name)
        if fun_start is not None:
            return path, _extract_signature(text, fun_start)
    return None


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: python3 fetch_component_signature.py <ComponentName>", file=sys.stderr)
        return 2
    name = sys.argv[1]
    try:
        result = find_signature(name)
    except (HTTPError, URLError) as e:
        print(f"Network error reaching GitHub: {e}", file=sys.stderr)
        return 1
    if result is None:
        print(
            f"No `fun {name}(...)` found anywhere under {COMPONENTS_PATH}/.\n"
            f"Check the component catalog for the correct name: "
            f"https://github.com/{REPO}/blob/main/docs/components.md",
            file=sys.stderr,
        )
        return 1
    path, signature = result
    print(f"// {path}")
    print(signature)
    return 0


if __name__ == "__main__":
    sys.exit(main())
