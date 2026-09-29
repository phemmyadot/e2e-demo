#!/usr/bin/env bash
# Builds, installs and launches ShopDemo on the connected emulator/device.
# Starts the ShopDemo_Pixel_API_34 emulator first if nothing is connected.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$SDK/platform-tools/adb"
AVD="${AVD:-ShopDemo_Pixel_API_34}"

if ! "$ADB" devices | grep -qE "device$"; then
  echo "Starting emulator $AVD…"
  nohup "$SDK/emulator/emulator" -avd "$AVD" -no-snapshot-save >/dev/null 2>&1 &
  "$ADB" wait-for-device
  until [[ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; do sleep 2; done
fi

"$ROOT/gradlew" -p "$ROOT" :app:installDebug -q
"$ADB" shell am start -n com.example.shopdemo/.ui.login.LoginActivity
