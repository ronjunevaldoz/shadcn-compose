#!/usr/bin/env bash
# run-ci-locally.sh — dry-run this project's ci.yml locally via act, before pushing.
#
# Real limitation, not a guess: act runs every job inside a Linux Docker container.
# There is no macOS Docker image, so a job with `runs-on: macos-latest` (this
# project's iOS test job) cannot be faithfully emulated — it will fail to map to an
# image or run incorrectly under a substituted Linux one. act has no CLI flag to
# exclude a job by name (-j only *includes* one job at a time), so this script
# can't skip it for you automatically — it only warns which job(s) will fail, and
# tells you the real, documented way to make that job skip itself when run locally:
# add `if: ${{ !env.ACT }}` to that job in ci.yml (act sets env.ACT=true only when
# running locally; that condition is simply absent on real GitHub runners, so
# nothing changes there). That's a one-time edit to the workflow file itself, not
# something this script does for you.
#
# Usage: bash run-ci-locally.sh [workflow-file]   (defaults to .github/workflows/ci.yml)

set -euo pipefail

WORKFLOW="${1:-.github/workflows/ci.yml}"

if ! command -v act &>/dev/null; then
  echo "  ❌  act not found. Run install-act.sh first." >&2
  exit 1
fi

if [[ ! -f "$WORKFLOW" ]]; then
  echo "  ❌  $WORKFLOW not found. Pass the workflow file path as an argument." >&2
  exit 1
fi

# Jobs whose runs-on targets macOS — act can't emulate these under Docker.
MACOS_JOBS=$(awk '
  /^[[:space:]]*[a-zA-Z0-9_-]+:[[:space:]]*$/ { job=$1; sub(/:$/, "", job) }
  /runs-on:.*macos/ { print job }
' "$WORKFLOW")

if [[ -n "$MACOS_JOBS" ]]; then
  echo "  ⚠️  These job(s) target a macOS runner — act cannot emulate them (no macOS"
  echo "      Docker image exists) and they will fail or behave incorrectly below:"
  echo "$MACOS_JOBS" | sed 's/^/      - /'
  echo "      To make one skip itself locally: add \`if: \${{ !env.ACT }}\` to that"
  echo "      job in $WORKFLOW — it only affects local act runs, real GitHub runners"
  echo "      are unaffected."
  echo ""
fi

echo "Running $WORKFLOW locally via act (push event)..."
act push -W "$WORKFLOW"

echo ""
echo "  ✅  Local dry-run finished. This does not replace real CI."
