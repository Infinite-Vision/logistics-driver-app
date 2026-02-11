#!/bin/bash

# Logistics Driver App - Log Capture Script
# This script captures detailed logs from the app during testing

echo "=========================================="
echo "Logistics Driver App - Log Capture"
echo "=========================================="
echo ""
echo "Starting log capture..."
echo "Testing app navigation from Owner to Vehicle Details"
echo ""
echo "Logs will be displayed AND saved to: debug_logs.txt"
echo ""
echo "What to do:"
echo "1. Open the app on your device/emulator"
echo "2. Navigate to Owner Details screen"
echo "3. Fill in the form and upload documents"
echo "4. Click Continue button"
echo "5. Watch the logs below"
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

adb logcat -v time -s MainActivity:D OwnerDetailsFragment:D OnboardingViewModel:D AppStateViewModel:D | tee debug_logs.txt

