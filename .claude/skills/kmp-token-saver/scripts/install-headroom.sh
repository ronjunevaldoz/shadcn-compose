#!/usr/bin/env bash
# install-headroom.sh — install the Headroom Python package via pip, the officially
# documented install method: `pip install "headroom-ai[all]"`.
#
# This script ONLY installs the package. It deliberately does NOT:
#   - collect, store, or enter any LLM provider API key on your behalf — never handle
#     credentials for the user, even for a token-saving tool
#   - start the local proxy server (`headroom proxy --port 8787`) — that opens a port
#     and starts intercepting traffic; it's your call when to run it, not this script's
#   - modify any Claude Code, shell, or project configuration
#
# Headroom needs your own provider API key(s) and a running proxy to actually do
# anything. Both are printed as next steps below — you do those yourself.

set -euo pipefail

PIP_CMD=""
if command -v pip3 &>/dev/null; then
  PIP_CMD="pip3"
elif command -v pip &>/dev/null; then
  PIP_CMD="pip"
else
  echo "  ❌  pip not found. Headroom requires Python 3.10+ with pip." >&2
  exit 1
fi

echo "Checking current Headroom install state..."
if "$PIP_CMD" show headroom-ai &>/dev/null; then
  INSTALLED_VERSION=$("$PIP_CMD" show headroom-ai 2>/dev/null | awk '/^Version:/{print $2}')
  echo "  ✅  headroom-ai already installed (version $INSTALLED_VERSION) — nothing to do."
else
  echo "  Installing headroom-ai[all] via $PIP_CMD (includes the headroom CLI)..."
  "$PIP_CMD" install "headroom-ai[all]"
  echo "  ✅  Installed."
fi

cat <<'EOF'

Headroom is installed but not configured or running — three things left, all yours to
do, not this script's:

  1. Start the local proxy only when you're ready to use it — this script does not
     start it automatically:
       headroom proxy --port 8787
     First run also downloads the Kompress-v2-base model from HuggingFace — expect a
     delay on that first invocation.

  2. Point it at a real (or local) model backend — your own LLM provider API key, or
     a local model like Ollama. Never paste a real key into a script or committed file.

  3. Route Claude Code's own traffic through the proxy: add an `env` block to
     ~/.claude/settings.json with ANTHROPIC_BASE_URL pointed at the proxy, e.g.:
       "env": {
         "ANTHROPIC_BASE_URL": "http://localhost:8787",
         "ANTHROPIC_AUTH_TOKEN": "...",   // provider key, or a placeholder for a local backend
         "ANTHROPIC_API_KEY": ""
       }
     This is a global settings.json change, same class as RTK's hook wiring — edit it
     yourself; this script does not touch settings.json.

Nothing routes through Headroom until all three of the above are done.
EOF
