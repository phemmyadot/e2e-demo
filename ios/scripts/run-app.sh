#!/usr/bin/env bash
# Builds ShopDemo, installs it on a simulator and launches it.
# Usage: scripts/run-app.sh ["iPhone 17"]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEVICE_NAME="${1:-${SIMULATOR:-iPhone 17}}"
BUNDLE_ID="com.example.shopdemo"
DERIVED="$ROOT/ShopDemo/build"

UDID="$(xcrun simctl list devices available | grep -F "    $DEVICE_NAME (" | tail -1 | sed -E 's/.*\(([0-9A-F-]{36})\).*/\1/')"
[[ -n "$UDID" ]] || { echo "No available simulator named '$DEVICE_NAME'" >&2; exit 1; }

xcrun simctl boot "$UDID" 2>/dev/null || true
open -a Simulator

xcodebuild -project "$ROOT/ShopDemo/ShopDemo.xcodeproj" -scheme ShopDemo -configuration Debug \
  -destination "id=$UDID" -derivedDataPath "$DERIVED" build -quiet

xcrun simctl install "$UDID" "$DERIVED/Build/Products/Debug-iphonesimulator/ShopDemo.app"
xcrun simctl launch "$UDID" "$BUNDLE_ID"
