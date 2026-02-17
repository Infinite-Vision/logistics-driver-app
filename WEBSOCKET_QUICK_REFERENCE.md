# WebSocket Implementation - Quick Reference

## ✅ What's Been Added

### 1. Core WebSocket Components

**Location**: `app/src/main/java/com/example/logistics_driver_app/data/websocket/`

- **WebSocketMessage.kt** - Message data classes (LocationPayload, NewOrderPayload, etc.)
- **WebSocketListener.kt** - Event callback interface  
- **DriverWebSocketManager.kt** - WebSocket connection manager

### 2. Location Tracking Service

**Location**: `app/src/main/java/com/example/logistics_driver_app/data/service/`

- **LocationTrackingService.kt** - Integrates GPS location with WebSocket

### 3. Dependencies Added

**Location**: `app/build.gradle.kts`

```kotlin
implementation("com.google.android.gms:play-services-location:21.3.0")
```

### 4. Configuration Updated

**Location**: `app/src/main/java/com/example/logistics_driver_app/Common/util/Constants.kt`

```kotlin
const val WS_BASE_URL = "ws://43.205.235.73:8080"
const val WS_DRIVER_PATH = "/ws/driver"
const val WS_DRIVER_LOCATION_PATH = "/ws/driver/location"
const val LOCATION_UPDATE_INTERVAL = 30L
```

## 🚀 Quick Start - 3 Steps

### Step 1: Initialize Service (in Fragment/Activity)

```kotlin
private lateinit var locationService: LocationTrackingService

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    locationService = LocationTrackingService.getInstance(requireContext())
}
```

### Step 2: Go Online (Connect WebSocket + Start Location)

```kotlin
private fun goOnline() {
    locationService.startTracking(object : LocationTrackingListener {
        override fun onDriverOnline() {
            // Driver is ONLINE - Update UI
            binding.statusText.text = "Online"
        }
        
        override fun onDriverOffline() {
            // Driver is OFFLINE - Update UI
            binding.statusText.text = "Offline"
        }
        
        override fun onLocationUpdated(latitude: Double, longitude: Double) {
            // Location sent to server automatically
        }
        
        override fun onNewOrderReceived(order: NewOrderPayload) {
            // Show new order dialog
            showOrderDialog(order)
        }
        
        override fun onError(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    })
}
```

### Step 3: Go Offline (Disconnect WebSocket + Stop Location)

```kotlin
private fun goOffline() {
    locationService.stopTracking()
}
```

## 📋 Complete Flow

```
User clicks "Go Online"
    ↓
locationService.startTracking()
    ↓
Connects to: ws://43.205.235.73:8080/ws/driver/location
    ↓
Server responds: {"type":"ACK", "payload":{"message":"CONNECTED"}}
    ↓
Driver status → ONLINE on server
    ↓
onDriverOnline() callback triggered
    ↓
Location updates start (every 30 seconds)
    ↓
Client sends: {"type":"LOCATION", "payload":{lat, lng, timestamp}}
    ↓
Server responds: {"type":"ACK", "payload":{"message":"LOCATION_RECEIVED"}}
    ↓
Server assigns order
    ↓
Client receives: {"type":"NEW_ORDER", "payload":{orderId, pickup, drop, ...}}
    ↓
onNewOrderReceived() callback triggered
    ↓
Show order dialog to driver
    ↓
User clicks "Go Offline"
    ↓
locationService.stopTracking()
    ↓
WebSocket disconnects
    ↓
Driver status → OFFLINE on server
```

## 🎯 Integration into DriverHomeFragment

**File**: `DriverHomeFragment.kt`

### Add Property

```kotlin
private lateinit var locationService: LocationTrackingService
```

### Initialize in onViewCreated()

```kotlin
locationService = LocationTrackingService.getInstance(requireContext())
```

### Update goOnline() method

```kotlin
private fun goOnline() {
    locationService.startTracking(object : LocationTrackingListener {
        override fun onDriverOnline() {
            isOnline = true
            binding.tvStatus.text = "You are Online"
        }
        
        override fun onDriverOffline() {
            isOnline = false
            binding.tvStatus.text = "You are Offline"
        }
        
        override fun onLocationUpdated(latitude: Double, longitude: Double) {
            // Location automatically sent to server
        }
        
        override fun onNewOrderReceived(order: NewOrderPayload) {
            showNewOrderDialog(order)
        }
        
        override fun onError(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    })
}
```

### Update goOffline() method

```kotlin
private fun goOffline() {
    locationService.stopTracking()
    isOnline = false
    binding.tvStatus.text = "You are Offline"
}
```

### Add cleanup in onDestroyView()

```kotlin
override fun onDestroyView() {
    super.onDestroyView()
    locationService.removeListener()
}
```

### Add new order dialog

```kotlin
private fun showNewOrderDialog(order: NewOrderPayload) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle("New Order")
        .setMessage("""
            Pickup: ${order.pickup}
            Drop: ${order.drop}
            Distance: ${order.distanceKm} km
            Fare: ₹${order.estimatedFare}
        """.trimIndent())
        .setPositiveButton("Accept") { _, _ ->
            // Call API to accept
        }
        .setNegativeButton("Reject", null)
        .show()
}
```

## 📱 Testing Checklist

- [ ] Click "Go Online" → Driver status becomes ONLINE on server
- [ ] Wait 30 seconds → Location update sent to server
- [ ] Backend assigns order → NEW_ORDER dialog appears
- [ ] Click "Go Offline" → Driver status becomes OFFLINE on server
- [ ] Check server logs → Should see location updates every 30s
- [ ] Disconnect internet → Auto-reconnect when connection restored
- [ ] Kill app when online → Location tracking stops, driver goes OFFLINE

## 🔧 Configuration Options

### Change Location Update Interval

In `DriverWebSocketManager.kt`:
```kotlin
companion object {
    private const val LOCATION_UPDATE_INTERVAL = 45000L // 45 seconds
}
```

Or dynamically:
```kotlin
webSocketManager.startLocationUpdates(intervalSeconds = 45) { ... }
```

### Use HTTPS/WSS

Change in `Constants.kt`:
```kotlin
const val WS_BASE_URL = "wss://43.205.235.73:8080" // Secure WebSocket
```

## 🐛 Common Issues

**Issue**: WebSocket not connecting
- **Solution**: Check JWT token in SharedPreference
- **Solution**: Verify network permissions
- **Solution**: Check BASE_URL accessibility

**Issue**: Location not updating
- **Solution**: Grant location permissions
- **Solution**: Enable GPS on device
- **Solution**: Use `connectDriverLocation()` not `connectDriver()`

**Issue**: Not receiving NEW_ORDER
- **Solution**: Verify driver is ONLINE on backend
- **Solution**: Check WebSocket connection is active
- **Solution**: Verify order assignment logic on backend

## 📚 Documentation Files

- **WEBSOCKET_IMPLEMENTATION.md** - Complete implementation guide
- **WebSocketIntegrationExample.kt** - Code examples  
- **Constants.kt** - Configuration values

## 🎉 Benefits

✅ **Real-time Communication** - Instant order assignments  
✅ **Automatic Location Tracking** - Updates sent every 30 seconds  
✅ **Auto-Reconnection** - Handles network issues gracefully  
✅ **Battery Efficient** - Uses FusedLocationProvider  
✅ **Type-Safe** - Kotlin data classes for messages  
✅ **Easy Integration** - Just 6 lines of code to add to existing fragment  
✅ **Production Ready** - Error handling, logging, lifecycle management
