#!/bin/bash

# Test script for Go Offline crash fix
# This captures logs specifically for the offline flow

echo "========================================"
echo "Go Offline Test - Crash Fix Validation"
echo "========================================"
echo ""
echo "Instructions:"
echo "1. Go Online first (slide right)"
echo "2. Wait for connection confirmation"
echo "3. Slide left to Go Offline"
echo "4. Watch for crash or successful offline"
echo ""
echo "Looking for:"
echo "  ✓ DRIVER_OFFLINE broadcast received"
echo "  ✓ Service onDestroy logs"
echo "  ✗ No ForegroundServiceDidNotStartInTimeException"
echo ""
echo "Press Ctrl+C to stop"
echo "========================================"
echo ""

# Clear logcat buffer
adb logcat -c

# Start capturing logs with better filtering
adb logcat -v time \
  DriverLocationService:* \
  DriverHomeFragment:* \
  MainActivity:E \
  AndroidRuntime:E \
  *:S
