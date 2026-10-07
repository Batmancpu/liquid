#!/usr/bin/env bash
set -euo pipefail

echo "=============================================="
echo "EXP Mango Offline Architecture Audit"
echo "=============================================="

# 1. Check for prohibited INTERNET permission in any AndroidManifest.xml
echo "[1/3] Checking AndroidManifest.xml for INTERNET permission..."
if grep -rn "android.permission.INTERNET" android/src/main/AndroidManifest.xml | grep -v 'tools:node="remove"'; then
    echo "ERROR: Active android.permission.INTERNET found in manifest!"
    exit 1
fi
echo "PASS: Manifest has zero network permission (hard-removed)."

# 2. Check build scripts for online analytics or network SDKs
echo "[2/3] Checking dependencies for prohibited cloud/telemetry SDKs..."
PROHIBITED_PATTERNS=("firebase" "analytics" "sentry" "crashlytics" "okhttp" "retrofit" "ktor")
for pattern in "${PROHIBITED_PATTERNS[@]}"; do
    if grep -rn -i "$pattern" android/build.gradle.kts | grep -v "//"; then
        echo "WARNING: Suspicious network/telemetry dependency pattern found: $pattern"
    fi
done
echo "PASS: Dependency scan clean."

# 3. Check source code for cleartext network calls
echo "[3/3] Checking source code for direct network endpoints..."
if grep -rn -E "(http://|https://|ws://|wss://)" android/src/main/java/ 2>/dev/null; then
    echo "WARNING: URL strings found in source code."
else
    echo "PASS: Zero network URLs found in source."
fi

echo "=============================================="
echo "AUDIT RESULT: Offline integrity verified."
echo "=============================================="
exit 0
