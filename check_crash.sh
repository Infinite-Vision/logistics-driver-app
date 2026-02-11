#!/bin/bash

# Check for recent crashes in the app

echo "=========================================="
echo "Checking for App Crashes"
echo "=========================================="
echo ""

# Get the package name
PACKAGE="com.example.logistics_driver_app"

echo "Looking for crashes in package: $PACKAGE"
echo ""

# Check for crash logs
echo "Recent crash logs:"
echo "===================="
adb logcat -d -s AndroidRuntime:E | grep -A 20 "$PACKAGE"

echo ""
echo ""
echo "Fatal exceptions:"
echo "===================="
adb logcat -d | grep -i "FATAL EXCEPTION" -A 30

echo ""
echo ""
echo "Navigation errors:"
echo "===================="
adb logcat -d | grep -i "navigation" | grep -i "error\|exception\|fail"

echo ""
echo ""
echo "Fragment errors:"
echo "===================="
adb logcat -d | grep -i "fragment" | grep -i "error\|exception\|not attached\|illegalstate"

echo ""
echo "Done!"
