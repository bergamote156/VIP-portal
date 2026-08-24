#!/bin/bash

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

die() { echo -e "${RED}$1${NC}"; exit 1; }
ok() { echo -e "${GREEN}$1${NC}"; }

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

[ -d frontend/dist ] && rm -rf frontend/dist || true
[ -d vip-portal/src/main/webapp/new_front ] && rm -rf vip-portal/src/main/webapp/new_front || true

ok "Clean frontend ended"
