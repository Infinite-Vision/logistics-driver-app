# Persistent WebSocket Service Implementation

## Overview
Implemented a foreground service that maintains WebSocket connection across screen navigation and app backgrounding. The service survives even if the app is killed and automatically restarts to maintain driver's online status.

## Key Features

### ✅ Persistent Connection
- WebSocket connection remains active while navigating between screens
- Service continues running even when app is in background
- Connection survives app kills (with START_STICKY)
- State is preserved in SharedPreferences

### ✅ Foreground Service
- Runs as a foreground service with notification
- Cannot be killed by Android system (as easily)
- Displays "Driver Mode: Online" notification
- "Go Offline" action button in notification

### ✅ State Management
- Online/offline state stored in SharedPreferences
- Service automatically restores state after restart
- UI updates reflect service state via broadcasts

### ✅ Background Permissions
- Runs with FOREGROUND_SERVICE permission
- FOREGROUND_SERVICE_LOCATION type
- Works even when app is in backgroundor killed
- `stopWithTask="false"` ensures service survives app closure

## Files Created/Modified

### 1. DriverLocationService.kt (NEW)
**Location:** `app/src/main/java/com/example/logistics_driver_app/data/service/DriverLocationService.kt`

**Purpose:** Foreground service managing WebSocket connection and location tracking

**Key Methods:**
```kotlin
// Start service and go online
DriverLocationService.startService(context)

// Stop service and go offline
DriverLocationService.stopService(context)

// Check if driver is currently online
DriverLocationService.isDriverOnline(context)
```

**Service Lifecycle:**
1. `onStartCommand()` - Starts foreground service with notification
2. `goOnline()` - Connects to WebSocket, starts location tracking
3. `goOffline()` - Disconnects WebSocket, stops location tracking
4. `onDestroy()` - Service will restart if driver is online (START_STICKY)

**Broadcast Events:**
- `com.example.logistics_driver_app.DRIVER_ONLINE` - Sent when WebSocket connects
- `com.example.logistics_driver_app.DRIVER_OFFLINE` - Sent when WebSocket disconnects
- `com.example.logistics_driver_app.NEW_ORDER` - Sent when new order received

### 2. AndroidManifest.xml (MODIFIED)
Added service declaration:
```xml
<service
    android:name=".data.service.DriverLocationService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="location"
    android:stopWithTask="false" />
```

**Key Attributes:**
- `foregroundServiceType="location"` - Declares this is a location tracking service
- `stopWithTask="false"` - Service continues even after app task is removed

### 3. DriverHomeFragment.kt (MODIFIED)
**Changes:**
- Added BroadcastReceivers for service events
- Replaced direct WebSocket calls with service start/stop
- UI updates now happen via broadcasts
- State is restored on fragment recreation

**BroadcastReceivers:**
```kotlin
// Listens for driver going online
driverOnlineReceiver -> updateUIForOnlineState()

// Listens for driver going offline  
driverOfflineReceiver -> updateUIForOfflineState()

// Listens for new orders
newOrderReceiver -> showTripRequestBottomSheet()
```

**Lifecycle:**
- `onViewCreated()` - Registers broadcast receivers, restores state
- `onDestroyView()` - Unregisters receivers (service continues running)

## How It Works

### Going Online Flow
1. User slides "Go Online" slider
2. `updateOnlineStatus()` calls `DriverLocationService.startService()`
3. Service starts as foreground with notification
4. Service calls `goOnline()`:
   - Saves online state to SharedPreferences
   - Starts location updates
   - Connects to WebSocket
5. When WebSocket connects:
   - Service sends `DRIVER_ONLINE` broadcast
   - Fragment receives broadcast and updates UI

### Navigating Away
1. User navigates to another screen
2. Fragment's `onDestroyView()` is called
3. Unregisters broadcast receivers
4. **Service continues running in background**
5. WebSocket connection stays alive
6. Location updates continue to be sent

### Returning Home
1. User returns to Driver Home screen
2. Fragment's `onViewCreated()` is called
3. Checks `DriverLocationService.isDriverOnline(context)`
4. If online, calls `updateUIForOnlineState()`
5. Registers broadcast receivers again
6. UI shows online state with animations

### Going Offline Flow
1. User slides "Go Offline" slider
2. `updateOfflineStatus()` calls `DriverLocationService.stopService()`
3. Service calls `goOffline()`:
   - Saves offline state to SharedPreferences
   - Stops location updates
   - Disconnects WebSocket
4. Service sends `DRIVER_OFFLINE` broadcast
5. Fragment receives broadcast and updates UI
6. Service stops itself

### App Killed/Restarted
1. Android kills app or user force-stops it
2. Service is destroyed (if running)
3. Android restarts service (START_STICKY)
4. Service's `onStartCommand()` is called with null intent
5. Service checks SharedPreferences for `KEY_IS_ONLINE`
6. If true:
   - Calls `go Online()` to restore connection
   - Reconnects to WebSocket
   - Resumes location tracking
7. If false:
   - Service stops itself

## Testing Instructions

### Test 1: Basic Online/Offline
1. Open app, navigate to Driver Home
2. Slide "Go Online"
3. **Expected:** 
   - Notification appears "Driver Mode: Online"
   - Status shows "You are Online"
   - Animations start
4. Slide "Go Offline"
5. **Expected:**
   - Notification disappears
   - Status shows "You are Offline"
   - Animations stop

### Test 2: Navigation Persistence
1. Go online (notification appears)
2. Navigate to Menu or other screen
3. **Expected:**
   - Notification still shows
   - Check logcat: "Service continues in background"
4. Navigate back to Driver Home
5. **Expected:**
   - UI shows "You are Online"
   - Animations resume
   - Status restored correctly

### Test 3: Background Operation
1. Go online
2. Press Home button (minimize app)
3. **Expected:**
   - Notification still visible
   - Service keeps running
4. Wait 30+ seconds
5. Check logcat for location updates:
   ```
   DriverLocationService: [LOCATION] Updated: 12.9716, 77.5946
   DriverWebSocketManager: Location sent: 12.9716, 77.5946
   ```
6. Open app again
7. **Expected:**
   - Driver Home shows "You are Online"

### Test 4: App Kill Recovery
1. Go online
2. Force stop the app:
   ```bash
   adb shell am force-stop com.example.logistics_driver_app
   ```
3. **Expected:**
   - App closes
   - Service is killed
   - Android restarts service (may take a few seconds)
4. Check logcat:
   ```
   DriverLocationService: [RESTART] Service restarted - restoring online state
   DriverWebSocketManager: Connecting to WebSocket...
   DriverWebSocketManager: WebSocket connected
   ```
5. Open app again
6. **Expected:**
   - Driver Home shows "You are Online"
   - Connection is active

### Test 5: Notification Action
1. Go online
2. Pull down notification shade
3. Tap "Go Offline" button in notification
4. **Expected:**
   - Service stops
   - Notification disappears
   - If on Driver Home, UI updates to "You are Offline"

## Monitoring Commands

### Monitor Service and WebSocket
```bash
# Watch service and WebSocket logs
adb logcat -v time -s DriverLocationService:D DriverWebSocketManager:D DriverHomeFragment:D

# Check if service is running
adb shell dumpsys activity services | grep DriverLocationService

# Monitor broadcasts
adb logcat -v time | grep "DRIVER_ONLINE\\|DRIVER_OFFLINE\\|NEW_ORDER"
```

### Expected Log Pattern

**When Going Online:**
```
DriverHomeFragment: [WEBSOCKET] Driver going ONLINE - Starting connection...
DriverHomeFragment: [SERVICE] Starting DriverLocationService to go ONLINE
DriverLocationService: [LIFECYCLE] onStartCommand - Action: ACTION_GO_ONLINE
DriverLocationService: [WEBSOCKET] Going ONLINE
DriverLocationService: [LOCATION] Location updates started successfully
DriverLocationService: [WEBSOCKET] Attempting to connect...
DriverWebSocketManager: Connecting to WebSocket: ws://43.205.235.73:8080/ws/driver/location
DriverWebSocketManager: WebSocket connected
DriverLocationService: [WEBSOCKET] Connected - Driver is now ONLINE
DriverHomeFragment: [BROADCAST] Driver went ONLINE
DriverHomeFragment: [UI] Updating UI to ONLINE state
```

**During Background Operation:**
```
DriverLocationService: [LOCATION] Updated: 12.9716, 77.5946
DriverWebSocketManager: Location sent: 12.9716, 77.5946
DriverWebSocketManager: ACK: LOCATION_RECEIVED
```

**When Going Offline:**
```
DriverHomeFragment: [WEBSOCKET] Driver going OFFLINE - Disconnecting...
DriverHomeFragment: [SERVICE] Stopping DriverLocationService to go OFFLINE
DriverLocationService: [WEBSOCKET] Going OFFLINE
DriverWebSocketManager: WebSocket closed: 1000 - Going offline
DriverLocationService: [WEBSOCKET] Disconnected: Going offline
DriverHomeFragment: [BROADCAST] Driver went OFFLINE
DriverHomeFragment: [UI] Updating UI to OFFLINE state
```

## State Persistence

**SharedPreferences Key:**
- `driver_is_online` (Boolean) - Stores whether driver is currently online

**When State is Saved:**
- When going online: `putBoolean(KEY_IS_ONLINE, true)`
- When going offline: `putBoolean(KEY_IS_ONLINE, false)`

**When State is Checked:**
- Service restart: Determines if should reconnect
- Fragment creation: Restores UI state
- `DriverLocationService.isDriverOnline()` call

## Notification Details

**Channel:**
- ID: `driver_location_service`
- Name: "Driver Location Service"
- Importance: LOW (doesn't make sound/vibration)

**Notification Content:**
- Title: "Driver Mode: Online"
- Text: Dynamic (shows connection status)
  - "Connecting..."
  - "Online - Tracking location"
  - "New Order! [pickup address]"
  - "Error: [error message]"
  - "Disconnected - Reconnecting..."

**Actions:**
- "Go Offline" button - Stops service

**Icons:**
- Main: `ic_location` (location pin icon)
- Action: `ic_close` (X close icon)

## Important Notes

### Battery Optimization
Users may need to disable battery optimization for the app:
```kotlin
// TODO: Add battery optimization check and request exemption
// Settings > Battery > Battery Optimization > All apps > Logistics Driver > Don't optimize
```

### Android 13+ Notification Permission
App requests `POST_NOTIFICATIONS` permission in manifest. Users must grant this on Android 13+.

### Background Location
- App uses `ACCESS_FOREGROUND_LOCATION` while service is active
- `ACCESS_BACKGROUND_LOCATION` enables tracking when app is not visible
- Users must grant  "Allow all the time" for full functionality

## Architecture Benefits

1. **Separation of Concerns:** Service handles connection, Fragment handles UI
2. **Loose Coupling:** Broadcast receivers allow communication without tight coupling
3. **Lifecycle Safety:** Service survives fragment lifecycle events
4. **State Management:** SharedPreferences persists state across app restarts
5. **User Experience:** Seamless online/offline state across navigation

## Future Enhancements

### Battery Optimization Handling
```kotlin
// Check and request battery optimization exemption
fun requestBatteryOptimizationExemption(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
}
```

### Auto-Start on Boot
```xml
<!-- Add to AndroidManifest.xml -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<receiver android:name=".BootReceiver" android:enabled="true" android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

### Service Binding for Direct Communication
Currently uses broadcasts. Could optionally add bound service for direct method calls.

## Summary

✅ **Persistent WebSocket connection** - Survives navigation and backgrounding  
✅ **Foreground service** - Cannot be easily killed by system  
✅ **State preservation** - SharedPreferences maintains online/offline status  
✅ **Auto-restart** - Service restores connection after app kill
✅ **Broadcast communication** - UI updates reflect service state  
✅ **Background permissions** - Runs continuously with proper permissions  
✅ **Notification control** - User can go offline from notification  

The driver app now maintains WebSocket connection persistently, ensuring real-time order reception and location tracking continue regardless of user navigation or app state.
