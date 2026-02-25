#!/bin/bash

# Logistics Driver App - Log Capture Script
# This script captures detailed logs from the app during testing

echo "=========================================="
echo "Logistics Driver App - Log Capture"
echo "=========================================="
echo ""
echo "Starting log capture..."
echo "Testing WebSocket connection and location tracking"
echo ""
echo "Logs will be displayed AND saved to: debug_logs.txt"
echo ""
echo "What to do:"
echo "1. Open the app on your device/emulator"
echo "2. Navigate to Driver Home screen"
echo "3. Click 'Go Online' button to connect WebSocket"
echo "4. Watch for WebSocket connection and location update logs"
echo "5. Click 'Go Offline' to disconnect"
echo ""
echo "Press Ctrl+C to stop logging"
echo ""
echo "=========================================="
echo ""

# Clear old logs
echo "Clearing old logs..."
adb logcat -c
sleep 1

# Start capturing logs with color and save to file
echo "Capturing logs now..."
echo ""

adb logcat -v time \
  LogisticsApp:D \
  MainActivity:D \
  VerificationProgress:D \
  AppStateViewModel:D \
  OkHttp:D \
  PhoneViewModel:D \
  OTPViewModel:D \
  OTPVerificationFragment:D \
  DriverWebSocketManager:D \
  LocationTrackingService:D \
  DriverHomeFragment:D \
  AndroidRuntime:E \
  System.err:E \
  "*:S" \
  | tee debug_logs.txt

