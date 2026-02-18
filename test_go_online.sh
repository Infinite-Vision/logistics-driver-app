#!/bin/bash

# Test script for Go Online functionality
# This captures all relevant logs for debugging

echo "========================================"
echo "Go Online Test - Log Capture"
echo "========================================"
echo ""
echo "Instructions:"
echo "1. Make sure app is built and installed"
echo "2. Open the app and navigate to Driver Home"
echo "3. Slide to Go Online"
echo "4. Watch the logs below"
echo ""
echo "Press Ctrl+C to stop"
echo "========================================"
echo ""

# Clear logcat buffer
adb logcat -c

# Start capturing logs with relevant tags
adb logcat -v time \
  DriverHomeFragment:* \
  DriverLocationService:* \
  DriverWebSocketManager:* \
  MainActivity:* \
  *:E
