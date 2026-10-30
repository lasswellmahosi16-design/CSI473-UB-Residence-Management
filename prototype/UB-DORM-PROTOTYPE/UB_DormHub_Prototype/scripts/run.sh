#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if ! command -v javac >/dev/null 2>&1; then echo 'Please install JDK 21 (javac not found).'; exit 1; fi
mkdir -p build/classes
find src -name '*.java' -print0 | xargs -0 javac --release 21 -encoding UTF-8 -d build/classes
java -Ddormhub.webRoot=web -cp build/classes ub.dormhub.webapp.PrototypeServer
