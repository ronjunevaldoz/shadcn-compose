#!/usr/bin/env bash
# install-rtk.sh — install the RTK binary (safe, local package install) and preview
# the global hook-wiring step without applying it.
#
# RTK has two install phases with different authorization needs:
#   Phase 1 — `brew install rtk`: a normal local package install, no config changes.
#             Safe to run directly.
#   Phase 2 — `rtk init -g`: patches global `~/.claude/settings.json` and
#             `~/.claude/CLAUDE.md` to wire a PreToolUse hook into every future
#             session. This is a persistent, global change — it needs the user's
#             confirmation of the *specific* diff, not a generic "go ahead."
#
# This script only does Phase 1, then runs `rtk init -g --dry-run` to print the
# exact diff Phase 2 would make. It never applies Phase 2 itself — you run
# `rtk init -g` yourself after reviewing the preview.

set -euo pipefail

if command -v rtk &>/dev/null; then
  echo "  ✅  rtk already installed ($(rtk --version 2>/dev/null || echo 'version unknown')) — skipping brew install."
else
  if ! command -v brew &>/dev/null; then
    echo "  ❌  Homebrew not found. RTK's documented install is 'brew install rtk'." >&2
    exit 1
  fi
  echo "Installing rtk via brew (local package install, no config changes)..."
  brew install rtk
  echo "  ✅  Installed."
fi

echo ""
echo "Previewing the global hook-wiring step (not applying it)..."
echo ""
rtk init -g --dry-run || true

cat <<'EOF'

That preview shows exactly what `rtk init -g` would change in
~/.claude/settings.json and ~/.claude/CLAUDE.md. This script does not apply it —
review the diff above, then run it yourself if you want the hook active:

  rtk init -g

The hook only affects Bash commands in sessions started after that completes; it
does not apply retroactively to the current session.
EOF
