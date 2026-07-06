#!/usr/bin/env python3
"""
KMM Design System — component prefix derivation.

Derives a PascalCase design-system component prefix (the "App" in AppButton, AppCard,
...) from the project's actual name instead of defaulting to the generic "App" for
every project. Deterministic — same input always produces the same prefix, so the
agent never has to guess or ask unnecessarily.

Precedence used by the design-system skill (highest to lowest):
  1. COMPONENT_PREFIX already recorded in docs/design-system.md (explicit, user-chosen)
  2. --name argument (explicit override)
  3. rootProject.name in settings.gradle.kts
  4. last segment of the Gradle group ID (build.gradle.kts / gradle.properties)
  5. the project root directory name
  6. "App" (final fallback — only if nothing else yields a usable word)

Usage:
  python3 derive_component_prefix.py /path/to/project
  python3 derive_component_prefix.py /path/to/project --name "Guild Base"
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

# Generic/noise words that carry no brand identity — stripped when other words remain.
_NOISE_WORDS = {
    "app", "apps", "android", "ios", "kmp", "kmm", "shared", "compose",
    "project", "multiplatform", "mobile", "client", "core", "main",
}

_SETTINGS_NAME_RE = re.compile(r'rootProject\.name\s*=\s*["\']([^"\']+)["\']')
_GROUP_RE = re.compile(r'''group\s*=\s*["\']([^"\']+)["\']''')
_TOML_GROUP_RE = re.compile(r'''^\s*group\s*=\s*["\']([^"\']+)["\']''', re.MULTILINE)


def _split_words(raw: str) -> list[str]:
    """Split a name on kebab-case, snake_case, spaces, and camelCase/PascalCase boundaries."""
    # Insert a boundary before an uppercase letter that follows a lowercase/digit
    # (camelCase -> camel Case), then split on any non-alphanumeric run.
    spaced = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", " ", raw)
    parts = re.split(r"[^A-Za-z0-9]+", spaced)
    return [p for p in parts if p]


def _pascal_case(words: list[str]) -> str:
    return "".join(w[:1].upper() + w[1:].lower() for w in words)


def _strip_noise(words: list[str]) -> list[str]:
    stripped = [w for w in words if w.lower() not in _NOISE_WORDS]
    return stripped if stripped else words  # never strip down to nothing


def derive_from_name(raw_name: str) -> str:
    words = _split_words(raw_name)
    if not words:
        return "App"
    words = _strip_noise(words)
    prefix = _pascal_case(words)
    # Must be a legal Kotlin identifier start: letter, not a digit.
    if not prefix or not prefix[0].isalpha():
        return "App"
    return prefix


def _read_settings_name(root: Path) -> str | None:
    for fname in ("settings.gradle.kts", "settings.gradle"):
        f = root / fname
        if f.exists():
            m = _SETTINGS_NAME_RE.search(f.read_text(encoding="utf-8", errors="ignore"))
            if m:
                return m.group(1)
    return None


def _read_group_id(root: Path) -> str | None:
    for fname in ("build.gradle.kts", "build.gradle"):
        f = root / fname
        if f.exists():
            m = _GROUP_RE.search(f.read_text(encoding="utf-8", errors="ignore"))
            if m:
                return m.group(1)
    toml = root / "gradle" / "libs.versions.toml"
    if toml.exists():
        m = _TOML_GROUP_RE.search(toml.read_text(encoding="utf-8", errors="ignore"))
        if m:
            return m.group(1)
    return None


def resolve_source(root: Path, explicit_name: str | None) -> tuple[str, str]:
    """Return (raw_name, source_label) using the documented precedence (steps 2-5)."""
    if explicit_name:
        return explicit_name, "--name argument"
    settings_name = _read_settings_name(root)
    if settings_name:
        return settings_name, "settings.gradle.kts rootProject.name"
    group_id = _read_group_id(root)
    if group_id:
        return group_id.rsplit(".", 1)[-1], "Gradle group ID (last segment)"
    return root.resolve().name, "project directory name"


def main() -> int:
    p = argparse.ArgumentParser(description="Derive a design-system component prefix from the project name.")
    p.add_argument("root", type=Path, nargs="?", default=Path("."), help="Project root (default: .)")
    p.add_argument("--name", help="Explicit project name to derive from (skips file detection)")
    args = p.parse_args()

    raw_name, source = resolve_source(args.root, args.name)
    prefix = derive_from_name(raw_name)

    print(f"Source:        {source} -> \"{raw_name}\"")
    print(f"Prefix:        {prefix}")
    print(f"Example:       {prefix}Button, {prefix}Card, {prefix}TextField")
    if prefix == "App":
        print("Note: fell back to the generic \"App\" prefix — nothing more specific was found.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
