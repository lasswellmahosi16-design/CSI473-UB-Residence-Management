#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/classes
find src tests -name '*.java' -print0 | xargs -0 javac --release 21 -encoding UTF-8 -d build/classes
java -cp build/classes ub.dormhub.tests.VerificationServiceTest
if command -v python3 >/dev/null 2>&1; then python3 tests/api_smoke_test.py; else echo 'Python 3 not installed; HTTP smoke tests skipped.'; fi
