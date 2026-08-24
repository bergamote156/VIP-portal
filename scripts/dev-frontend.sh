#!/bin/bash

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

die() { echo -e "${RED}$1${NC}"; exit 1; }
ok() { echo -e "${GREEN}$1${NC}"; }

BACKEND_URL="${1:-http://localhost:8080}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

command -v node >/dev/null 2>&1 || die "Node.js not found"
command -v npm >/dev/null 2>&1 || die "npm not found"

cd frontend

[ -d node_modules ] || npm install || die "Failed to install dependencies"

export VITE_BACKEND_URL="$BACKEND_URL"
ok "Dev server http://localhost:5173 (proxy -> $VITE_BACKEND_URL)"

npm run dev
