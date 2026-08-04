#!/usr/bin/env bash
# install-act.sh — install nektos/act, so GitHub Actions workflows can be dry-run
# locally before pushing. Never touches GitHub, uses zero CI minutes — a local
# verification step only, not a replacement for real CI.
#
# Safe to run directly: standard Homebrew package install, no config changes.

set -euo pipefail

if command -v act &>/dev/null; then
  echo "  ✅  act already installed ($(act --version 2>/dev/null)) — nothing to do."
  exit 0
fi

if ! command -v brew &>/dev/null; then
  echo "  ❌  Homebrew not found. act's documented install is 'brew install act'." >&2
  echo "      See https://nektosact.com for other install methods." >&2
  exit 1
fi

echo "Installing act via brew..."
brew install act

if ! command -v docker &>/dev/null && ! docker info &>/dev/null 2>&1; then
  echo ""
  echo "  ⚠️  act runs workflows inside Docker containers — Docker (Desktop or"
  echo "      Colima) must be installed and running for act to work. Not installed"
  echo "      here; install it yourself if you don't already have it."
fi

echo ""
echo "  ✅  Installed. Verify with: act --version"
