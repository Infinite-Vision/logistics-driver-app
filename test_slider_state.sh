#!/bin/bash

# Comprehensive test for online/offline slider functionality

echo "=========================================="
echo "Slider State Management Test"
echo "=========================================="
echo ""
echo "Test Scenarios:"
echo "1. App starts OFFLINE → Slider at start (left)"
echo "2. Slide RIGHT to go online → Slider moves to end"
echo "3. Service starts → DRIVER_ONLINE broadcast"
echo "4. UI updates → Status: 'You are Online', Red slider"
echo "5. Slide LEFT to go offline → Slider moves to start"
echo "6. Service stops → DRIVER_OFFLINE broadcast"
echo "7. UI updates → Status: 'You are Offline', Green slider"
echo ""
echo "Watch for:"
echo "  ✓ No ForegroundServiceDidNotStartInTimeException"
echo "  ✓ Slider animates to correct position"
echo "  ✓ Status text updates correctly"
echo "  ✓ Slider color changes (green/red)"
echo "  ✓ DRIVER_ONLINE/OFFLINE broadcasts sent"
echo ""
echo "Press Ctrl+C to stop"
echo "=========================================="
echo ""

# Clear logcat
adb logcat -c

# Monitor relevant logs
adb logcat -v time \
  DriverLocationService:D \
  DriverHomeFragment:D \
  MainActivity:E \
  AndroidRuntime:E \
  *:S | grep -E "FOREGROUND|BROADCAST|UI|STATUS|Slider|ONLINE|OFFLINE|FATAL|Exception"
