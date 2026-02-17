# WebSocket Connection Log Capture Guide

## Overview
This guide helps you capture and monitor real-time WebSocket connection logs for the driver app.

## Prerequisites
- Device connected via USB (Device: 811aacf ✓)
- USB debugging enabled
- App installed on device

## Log Capture Instructions

### 1. Start Log Capture
Run in terminal:
```bash
./capture_logs.sh
```

This will:
- Clear old logs
- Start monitoring WebSocket-related activity
- Save logs to `debug_logs.txt`
- Display logs in real-time

### 2. Trigger WebSocket Connection
On your device:
1. Open the Logistics Driver App
2. Login if needed
3. Navigate to Driver Home screen
4. **Click "Go Online" button** to start WebSocket connection
5. Watch the terminal for connection logs

### 3. Expected Log Sequence

#### When Going Online:
```
DriverWebSocketManager: Connecting to WebSocket: ws://43.205.235.73:8080/ws/driver/location
DriverWebSocketManager: WebSocket connected
LocationTrackingService: Starting location tracking
LocationTrackingService: Location updates started successfully
LocationTrackingService: WebSocket connected - Driver is now ONLINE
LocationTrackingService: Location updated: 12.9716, 77.5946
DriverWebSocketManager: Location sent: 12.9716, 77.5946
DriverWebSocketManager: ACK: Location received
```

#### Location Updates (every 30 seconds):
```
LocationTrackingService: Location updated: 12.9716, 77.5946
DriverWebSocketManager: Location sent: 12.9716, 77.5946
DriverWebSocketManager: ACK: Location received
```

#### New Order Received:
```
DriverWebSocketManager: NEW_ORDER received: Order ID 123
LocationTrackingService: New order received: 123
```

#### When Going Offline:
```
LocationTrackingService: Stopping location tracking
DriverWebSocketManager: WebSocket closed: 1000 - Going offline
LocationTrackingService: WebSocket disconnected - Driver is now OFFLINE
LocationTrackingService: Location tracking stopped - Driver is now OFFLINE
```

### 4. Error Scenarios

#### No Session Token:
```
DriverWebSocketManager: No session token available
```
**Solution:** Login to the app first

#### Location Permission Denied:
```
LocationTrackingService: Location permissions not granted
```
**Solution:** Grant location permissions in app settings

#### WebSocket Connection Failed:
```
DriverWebSocketManager: WebSocket error: Failed to connect to /43.205.235.73:8080
```
**Solution:** Check internet connection and server availability

#### Invalid Location:
```
DriverWebSocketManager: Invalid latitude: 0.0
DriverWebSocketManager: Cannot send location: WebSocket not connected
```
**Solution:** Wait for GPS to acquire location fix

## Monitoring Tips

### Quick Status Check
```bash
# Check recent WebSocket logs
adb logcat -d -v time | grep -E "DriverWebSocket|LocationTracking" | tail -20
```

### Check Connection Status
```bash
# Look for "ONLINE" or "OFFLINE" status
adb logcat -d | grep -E "ONLINE|OFFLINE"
```

### Watch Real-time
```bash
# Follow logs as they happen
adb logcat -v time -s DriverWebSocketManager:D LocationTrackingService:D
```

## Log Files
- **debug_logs.txt** - Captured logs from script
- **websocket_status.txt** - Recent WebSocket status snapshot

## Troubleshooting

### No Logs Appearing?
1. Verify device connected: `adb devices`
2. Check app is running: `adb shell ps | grep logistics_driver`
3. Verify logging tags match class names
4. Try clearing logcat: `adb logcat -c` then retry

### WebSocket Not Connecting?
1. Check JWT token in SharedPreference
2. Verify server URL: ws://43.205.235.73:8080
3. Test network connectivity
4. Check server is running

### Location Not Updating?
1. Enable GPS/Location services on device
2. Grant location permissions to app
3. Move device to get GPS fix
4. Check Google Play Services installed

## Quick Test Procedure

1. **Start capture:**
   ```bash
   ./capture_logs.sh
   ```

2. **On device:** Open app → Login → Go to Driver Home

3. **Click "Go Online"** - Should see:
   - "Connecting to WebSocket"
   - "WebSocket connected"
   - "Driver is now ONLINE"

4. **Wait 30 seconds** - Should see:
   - "Location updated"
   - "Location sent"

5. **Click "Go Offline"** - Should see:
   - "Stopping location tracking"
   - "Driver is now OFFLINE"

## Log Level Reference

- **D (Debug):** Normal operation (connection, location updates)
- **W (Warning):** Non-critical issues (already connected, not enabled)
- **E (Error):** Failures (no token, connection failed, invalid data)

Press **Ctrl+C** in terminal to stop log capture.
