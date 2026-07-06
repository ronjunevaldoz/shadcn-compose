#!/usr/bin/env python3
"""
KMM Layout System — per-screen wireframe file creator.

Creates EXACTLY ONE screen file per invocation at docs/layout-system/<screen>.md, with the
correct section skeleton (Components table, a starting wireframe block, Interaction notes).
It never combines multiple screens into one file and never overwrites an existing screen
file (edit those in place). Bootstraps docs/layout-system/_components.md if missing.

The agent fills in the ASCII wireframe and component values — the script guarantees the
one-file-per-screen structure, naming, and location.

Usage:
  python3 create_wireframe.py --screen "Inbox" --pattern A
  python3 create_wireframe.py --screen "Login" --pattern D --root /path/to/project
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

PATTERNS = {
    "A": """\
+----------+------------------+----------------------------------------------+
| <Nav>    | <Side Panel>     | <Main Area>                                  |
| <N> dp   | <N> dp           | flex 1                                       |
+----------+------------------+----------------------------------------------+
|          |                  |                                              |
| [nav-1]* | <item>           | <primary content>                            |
| [nav-2]  | <item>           | <primary content>                            |
|          |                  |----------------------------------------------|
| [nav-3]  |                  | <action row>                                 |
+----------+------------------+----------------------------------------------+
Legend: [nav-1] = <name>  [nav-2] = <name>  [nav-3] = <name>  * = active""",
    "B": """\
+----------+------------------------------------------------------------+
| <Nav>    | <Main Area>                                                |
| <N> dp   | flex 1  (<Side Panel> not rendered)                        |
+----------+------------------------------------------------------------+
|          |                                                            |
| [nav-1]  | [tab] <Tab A>  [tab] <Tab B>  [tab] <Tab C>                |
| [nav-2]* | <content>                                                  |
| [nav-3]  |                                                            |
+----------+------------------------------------------------------------+
Legend: [nav-1] = <name>  [nav-2] = <name>  [nav-3] = <name>  * = active""",
    "C": """\
+----------+------------------------------------------------------------+
| <Nav>    | [canvas stays in place — no swap]                          |
| <N> dp   |                                                            |
+----------+------------------------------------------------------------+
|          |     +--------------------------------------------------+   |
| [nav-1]  |     | <Sheet title>                                  X |   |
| [nav-2]* |     | <content line>                                   |   |
| [nav-3]  |     +--------------------------------------------------+   |
+----------+------------------------------------------------------------+
Legend: [nav-1] = <name>  [nav-2] = <name>  [nav-3] = <name>  * = active""",
    "D": """\
+------------------------------------------------------------------------+
| <Screen Title>                                                         |
| full width                                                            |
+------------------------------------------------------------------------+
|                                                                        |
|  <header / hero content>                                               |
|  <content row>                                       [scroll]          |
|  ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~   |
|  [ <Primary action>                                                  ] |
|  <secondary action>                                                    |
+------------------------------------------------------------------------+""",
}

_COMPONENTS_TEMPLATE = """\
# Component Registry

Update this file when a component's dimensions, visibility, or behavior changes.

| Component       | Width / Height | Visibility               | Platform             | Notes              |
|-----------------|----------------|--------------------------|----------------------|--------------------|
| <Component A>   | <N> dp         | <always / screen X only> | Both / Android / iOS | <short description> |
"""


def slugify(name: str) -> str:
    s = re.sub(r"[^a-z0-9]+", "-", name.strip().lower())
    return s.strip("-")[:60] or "screen"


# Slot-grid frontmatter per pattern: which named slots exist and which render at each
# breakpoint. Weights are simple fractions from a closed set — never arbitrary floats.
PATTERN_GRIDS = {
    "A": ("[nav, side, main]",
          "{compact: [main], medium: [nav, main], expanded: [nav, side, main]}",
          "{nav: fixed, side: 1f, main: 3f}"),
    "B": ("[nav, main]",
          "{compact: [main], medium: [nav, main], expanded: [nav, main]}",
          "{nav: fixed, main: 1f}"),
    "C": ("[nav, main, sheet]",
          "{compact: [main, sheet], medium: [nav, main, sheet], expanded: [nav, main, sheet]}",
          "{nav: fixed, main: 1f, sheet: overlay}"),
    "D": ("[main]",
          "{compact: [main], medium: [main], expanded: [main]}",
          "{main: 1f}"),
}


def render(screen: str, pattern: str) -> str:
    wireframe = PATTERNS[pattern]
    slots, grid, weights = PATTERN_GRIDS[pattern]
    return f"""\
---
screen: {slugify(screen)}
pattern: {pattern}
slots: {slots}
grid: {grid}
weights: {weights}
---

# {screen.strip()}

## Components

| Component      | Width   | Visible         | Notes                     |
|----------------|---------|-----------------|---------------------------|
| <Component A>  | <N> dp  | <always / when> | <short note>              |
| <Component B>  | flex 1  | Yes             | <short note>              |

---

## Default

```
{wireframe}
```

---

## Interaction notes

- <tap / swipe / gesture> → <what happens>
- <state change> → <how it looks>
"""


def main() -> int:
    p = argparse.ArgumentParser(description="Create ONE per-screen wireframe file in docs/layout-system/.")
    p.add_argument("--screen", required=True, help="Screen name (becomes the heading + kebab filename).")
    p.add_argument("--pattern", default="A", choices=sorted(PATTERNS), help="Starting wireframe pattern (A/B/C/D).")
    p.add_argument("--root", type=Path, default=Path("."), help="Consumer project root (default: .).")
    args = p.parse_args()

    ls_dir = args.root / "docs" / "layout-system"
    ls_dir.mkdir(parents=True, exist_ok=True)

    # Bootstrap the shared registry once.
    components = ls_dir / "_components.md"
    if not components.exists():
        components.write_text(_COMPONENTS_TEMPLATE, encoding="utf-8")
        print(f"✅  Bootstrapped registry: {components}")

    screen_file = ls_dir / f"{slugify(args.screen)}.md"
    if screen_file.exists():
        print(f"⚠  {screen_file} already exists — edit it in place (not overwriting).", file=sys.stderr)
        return 1

    screen_file.write_text(render(args.screen, args.pattern), encoding="utf-8")
    print(f"✅  Created screen file: {screen_file}")
    print("    One file per screen — run create_wireframe.py again for the next screen.")
    print("    Now fill in the component table and the ASCII wireframe (keep all rows the same width).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
