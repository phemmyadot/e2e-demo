#!/usr/bin/env bash
# Usage: scripts/test.sh [unit|ui|baseline|benchmark|all]
#   unit       JVM tests: JUnit 4 + Mockito / mockito-kotlin          (no device)
#   ui         Instrumented: Espresso + Compose UI Test + E2E flows   (emulator/device)
#   baseline   UI Automator → app/src/release/generated/baselineProfiles/baseline-prof.txt
#   benchmark  UI Automator macrobenchmarks (startup, frame timing)
set -euo pipefail
cd "$(dirname "$0")/.."

case "${1:-all}" in
  unit)      ./gradlew :app:testDebugUnitTest ;;
  ui)        ./gradlew :app:connectedDebugAndroidTest ;;
  baseline)  ./gradlew :app:generateReleaseBaselineProfile ;;
  benchmark) ./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest ;;
  all)       ./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest ;;
  *) echo "usage: $0 [unit|ui|baseline|benchmark|all]" >&2; exit 1 ;;
esac

echo "Reports: app/build/reports/tests/ · app/build/reports/androidTests/connected/"
