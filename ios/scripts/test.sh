#!/usr/bin/env bash
# Runs the test suites on a simulator.
# Usage: scripts/test.sh [unit|ui|all] ["iPhone 17"]
#   unit → ShopDemoTests   (Swift Testing + XCTest + ViewInspector)
#   ui   → ShopDemoUITests (XCUITest end-to-end)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SUITE="${1:-all}"
DEVICE_NAME="${2:-${SIMULATOR:-iPhone 17}}"

case "$SUITE" in
  unit) ONLY=(-only-testing:ShopDemoTests) ;;
  ui)   ONLY=(-only-testing:ShopDemoUITests) ;;
  all)  ONLY=() ;;
  *) echo "usage: $0 [unit|ui|all] [simulator name]" >&2; exit 1 ;;
esac

RESULTS="$ROOT/ShopDemo/build/TestResults-$SUITE.xcresult"
rm -rf "$RESULTS"

xcodebuild test \
  -project "$ROOT/ShopDemo/ShopDemo.xcodeproj" \
  -scheme ShopDemo \
  -destination "platform=iOS Simulator,name=$DEVICE_NAME" \
  -derivedDataPath "$ROOT/ShopDemo/build" \
  -resultBundlePath "$RESULTS" \
  ${ONLY[@]+"${ONLY[@]}"} \
  | { command -v xcbeautify >/dev/null && xcbeautify || cat; }

echo "Results: $RESULTS  (open with: open \"$RESULTS\")"
