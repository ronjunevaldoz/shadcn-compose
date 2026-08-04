#!/usr/bin/env bash
# install-ponytail.sh — install the Ponytail Claude Code plugin at USER scope, so it's
# available in every project on this machine instead of just one.
#
# Uses the real, official `claude plugin` CLI subcommands (verified: `claude plugin
# --help` lists marketplace/install/list as real, non-interactive commands) — the exact
# equivalent of the /plugin marketplace add and /plugin install slash commands, not a
# workaround that edits Claude Code's plugin registry directly.
#
# Idempotent: safe to re-run. Skips the marketplace step if already registered, skips
# the install step if a user-scope, enabled ponytail@ponytail already exists.
#
# This is a persistent, global change to your Claude Code environment (installs a
# plugin, available in every future session) — run it yourself; an agent should not
# run this on your behalf without your specific confirmation of this exact action.

set -euo pipefail

PLUGIN="ponytail@ponytail"
MARKETPLACE_SOURCE="DietrichGebert/ponytail"
MARKETPLACE_NAME="ponytail"

if ! command -v claude &>/dev/null; then
  echo "  ❌  'claude' CLI not found on PATH." >&2
  exit 1
fi

echo "Checking for the ponytail marketplace..."
if claude plugin marketplace list 2>/dev/null | grep -q "❯ $MARKETPLACE_NAME"; then
  echo "  ✅  Marketplace '$MARKETPLACE_NAME' already registered."
else
  echo "  Adding marketplace: $MARKETPLACE_SOURCE"
  claude plugin marketplace add "$MARKETPLACE_SOURCE"
fi

echo ""
echo "Checking current ponytail install state..."
EXISTING_USER_INSTALL=$(
  claude plugin list --json 2>/dev/null \
    | python3 -c "
import json, sys
try:
    plugins = json.load(sys.stdin)
except Exception:
    plugins = []
for p in plugins:
    if p.get('id') == '$PLUGIN' and p.get('scope') == 'user' and p.get('enabled'):
        print('yes')
        break
else:
    print('no')
"
)

if [[ "$EXISTING_USER_INSTALL" == "yes" ]]; then
  echo "  ✅  '$PLUGIN' is already installed at user scope and enabled — nothing to do."
  exit 0
fi

echo "  Not yet installed at user scope (or currently disabled/project-scoped elsewhere)."
echo ""
echo "Installing at user scope (default scope — available in every project)..."
claude plugin install "$PLUGIN" --scope user

echo ""
echo "  ✅  Done. Verify with: claude plugin list"
