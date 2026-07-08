#!/usr/bin/env bash
# Fails if any `Style { ... }` block reads `shadcnTheme` directly inside its own
# lambda body -- a real, previously-shipped bug class (see .claude/AGENTS.md's
# "Style blocks can resolve outside normal composition" note and
# docs/visual-testing.md's blind-spot writeup for the incident this codified).
#
# `Style { }` blocks are not guaranteed to run in a normal composition position, so
# reading a CompositionLocal-backed @Composable getter (shadcnTheme) from inside one
# can capture a stale snapshot instead of the live value -- symptom: the property
# renders correctly once, then never updates on a live theme change (e.g. dark-mode
# toggle), even though every sibling that reads shadcnTheme from a real composable
# position updates correctly. Confirmed live in ShadcnInputGroup/ShadcnTextField.
#
# The fix is always the same: read `shadcnTheme.colors`/`.shapes`/etc. once into a
# plain `val` in the enclosing @Composable *before* the Style{} block, then reference
# that captured val -- not `shadcnTheme` again -- from inside the block.
#
# Usage: scripts/check_style_block_theme_reads.sh
# Exit 0: clean. Exit 1: violations found, printed as file:line.

set -euo pipefail

cd "$(dirname "$0")/.."

violations=0

# Scans every Style { ... } block (brace-depth matched, so it correctly spans
# multi-line blocks) in every .kt file under library/src and app/shared/src, and
# flags any block whose body contains a `shadcnTheme.` read. Comments (both // and
# /* */, including multi-line KDoc) are blanked out first, line-for-line, so a
# comment merely *mentioning* Style{} or shadcnTheme.-- like this script's own
# module doc, or .claude/AGENTS.md's prose quoted in a code comment -- can never
# produce a false positive or mask a real one.
while IFS= read -r -d '' file; do
  result=$(awk '
    # Pass 1: blank out comment text, preserving line count and non-comment code.
    {
      line = $0
      out = ""
      i = 1
      n = length(line)
      while (i <= n) {
        if (in_block) {
          close_pos = index(substr(line, i), "*/")
          if (close_pos > 0) {
            i += close_pos + 1
            in_block = 0
          } else {
            i = n + 1
          }
          continue
        }
        two = substr(line, i, 2)
        if (two == "//") {
          i = n + 1
        } else if (two == "/*") {
          in_block = 1
          i += 2
        } else {
          out = out substr(line, i, 1)
          i += 1
        }
      }
      cleaned[NR] = out
    }
    END {
      depth = 0
      for (ln = 1; ln <= NR; ln++) {
        line = cleaned[ln]
        if (depth == 0 && line ~ /Style *\{/) {
          depth = 1
          start = ln
          buf = line
          opens = gsub(/\{/, "{", line)
          closes = gsub(/\}/, "}", line)
          depth += opens - closes - 1
          if (depth <= 0) {
            if (buf ~ /shadcnTheme\./) print FILENAME ":" start
            depth = 0
          }
          continue
        }
        if (depth > 0) {
          buf = buf "\n" line
          opens = gsub(/\{/, "{", line)
          closes = gsub(/\}/, "}", line)
          depth += opens - closes
          if (depth <= 0) {
            if (buf ~ /shadcnTheme\./) print FILENAME ":" start
            depth = 0
          }
        }
      }
    }
  ' "$file")

  if [ -n "$result" ]; then
    echo "$result"
    violations=$((violations + 1))
  fi
done < <(find library/src app/shared/src -name "*.kt" -print0)

if [ "$violations" -gt 0 ]; then
  echo
  echo "Found $violations Style { } block(s) reading shadcnTheme directly inside their own body."
  echo "Fix: capture the needed theme sub-object as a plain val before the Style { } block instead."
  exit 1
fi

echo "OK: no Style { } block reads shadcnTheme directly inside its own body."
