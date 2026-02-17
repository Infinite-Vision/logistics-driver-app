# WebSocket Implementation Guide

## Overview

The app now supports real-time communication with the backend using WebSocket. This enables:
- Driver going online/offline
- Real-time location tracking
- Receiving new order assignments
- Server acknowledgments and error handling

## Architecture

### Components

1. **WebSocketMessage.kt** - Data classes for WebSocket messages
2. **WebSocketListener.kt** - Interface for WebSocket event callbacks
3. **DriverWebSocketManager.kt** - Manages WebSocket connections
4. **LocationTrackingService.kt** - Integrates location tracking with WebSocket

## Usage

### 1. Basic WebSocket Connection (Go Online without location)

```kotlin
val webSocketManager = DriverWebSocketManager.getInstance(context)

webSocketManager.connectDriver(object : WebSocketListener {
    override fun onConnected() {
        Log.d(TAG, "Driver is now ONLINE")
        // Update UI to show online status
    }
    
    override fun onDisconnected(code: Int, reason: String) {
        Log.d(TAG, "Driver is now OFFLINE")
        // Update UI to show offline status
    }
    
    override fun onAckReceived(message: String) {
        Log.d(TAG, "Server acknowledged: $message")
    }
    
    override fun onNewOrderReceived(order: NewOrderPayload) {
        // Handle new order assignment
        showOrderDialog(order)
    }
    
    override fun onError(errorMessage: String) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
    
    override fun onConnectionError(throwable: Throwable) {
        Log.e(TAG, "Connection error: ${throwable.message}")
    }
})

// When user goes offline
webSocketManager.disconnect()
```

### 2. Location Tracking with WebSocket (Recommended)

```kotlin
val locationService = LocationTrackingService.getInstance(context)

locationService.startTracking(object : LocationTrackingListener {
    override fun onLocationUpdated(latitude: Double, longitude: Double) {
        Log.d(TAG, "Location: $latitude, $longitude")
        // Update map UI
    }
    
    override fun onDriverOnline() {
        Log.d(TAG, "Driver is ONLINE")
        // Update UI - show "Online" badge
        statusTextView.text = "Online"
        statusTextView.setBackgroundColor(Color.GREEN)
    }
    
    override fun onDriverOffline() {
        Log.d(TAG, "Driver is OFFLINE")
        // Update UI - show "Offline" badge
        statusTextView.text = "Offline"
        statusTextView.setBackgroundColor(Color.GRAY)
    }
    
    override fun onNewOrderReceived(order: NewOrderPayload) {
        // Show new order notification
        showNewOrderNotification(order)
        
        // Navigate to order details screen
        val bundle = Bundle().apply {
            putLong("orderId", order.orderId)
            putString("pickup", order.pickup)
            putString("drop", order.drop)
            putDouble("distance", order.distanceKm)
            putDouble("fare", order.estimatedFare)
        }
        findNavController().navigate(R.id.orderDetailsFragment, bundle)
    }
    
    override fun onError(message: String) {
        Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
    }
})

// When user goes offline
locationService.stopTracking()
```

### 3. Fragment Implementation Example

```kotlin
class HomeFragment : Fragment() {
    
    private lateinit var locationService: LocationTrackingService
    private var isOnline = false
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        locationService = LocationTrackingService.getInstance(requireContext())
        
        // Toggle button for going online/offline
        binding.btnGoOnline.setOnClickListener {
            if (isOnline) {
                goOffline()
            } else {
                goOnline()
            }
        }
        
        // Check if already online
        if (locationService.isOnline()) {
            updateOnlineStatus(true)
        }
    }
    
    private fun goOnline() {
        locationService.startTracking(object : LocationTrackingListener {
            override fun onLocationUpdated(latitude: Double, longitude: Double) {
                // Update map with current location
                updateMapLocation(latitude, longitude)
            }
            
            override fun onDriverOnline() {
                updateOnlineStatus(true)
            }
            
            override fun onDriverOffline() {
                updateOnlineStatus(false)
            }
            
            override fun onNewOrderReceived(order: NewOrderPayload) {
                handleNewOrder(order)
            }
            
            override fun onError(message: String) {
                showError(message)
            }
        })
    }
    
    private fun goOffline() {
        locationService.stopTracking()
        updateOnlineStatus(false)
    }
    
    private fun updateOnlineStatus(online: Boolean) {
        isOnline = online
        binding.btnGoOnline.text = if (online) "Go Offline" else "Go Online"
        binding.statusIndicator.setBackgroundResource(
            if (online) R.drawable.status_online else R.drawable.status_offline
        )
    }
    
    private fun handleNewOrder(order: NewOrderPayload) {
        // Show order dialog with accept/reject options
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("New Order")
            .setMessage("""
                Pickup: ${order.pickup}
                Drop: ${order.drop}
                Distance: ${order.distanceKm} km
                Fare: ₹${order.estimatedFare}
            """.trimIndent())
            .setPositiveButton("Accept") { _, _ ->
                acceptOrder(order.orderId)
            }
            .setNegativeButton("Reject", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        locationService.removeListener()
    }
}
```

### 4. ViewModel Integration

```kotlin
class DriverViewModel(application: Application) : AndroidViewModel(application) {
    
    private val locationService = LocationTrackingService.getInstance(application)
    
    private val _isOnline = MutableLiveData<Boolean>(false)
    val isOnline: LiveData<Boolean> = _isOnline
    
    private val _currentLocation = MutableLiveData<Pair<Double, Double>>()
    val currentLocation: LiveData<Pair<Double, Double>> = _currentLocation
    
    private val _newOrder = MutableLiveData<NewOrderPayload>()
    val newOrder: LiveData<NewOrderPayload> = _newOrder
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    private val trackingListener = object : LocationTrackingListener {
        override fun onLocationUpdated(latitude: Double, longitude: Double) {
            _currentLocation.postValue(Pair(latitude, longitude))
        }
        
        override fun onDriverOnline() {
            _isOnline.postValue(true)
        }
        
        override fun onDriverOffline() {
            _isOnline.postValue(false)
        }
        
        override fun onNewOrderReceived(order: NewOrderPayload) {
            _newOrder.postValue(order)
        }
        
        override fun onError(message: String) {
            _error.postValue(message)
        }
    }
    
    fun goOnline() {
        locationService.startTracking(trackingListener)
    }
    
    fun goOffline() {
        locationService.stopTracking()
    }
    
    fun getCurrentLocation(): Pair<Double, Double>? {
        return locationService.getCurrentLocation()
    }
    
    override fun onCleared() {
        super.onCleared()
        locationService.removeListener()
    }
}
```

## WebSocket Message Format

### Client → Server (Location Update)

```json
{
  "type": "LOCATION",
  "payload": {
    "latitude": 12.9716,
    "longitude": 77.5946,
    "timestamp": "2026-02-16T10:30:00.000Z"
  }
}
```

### Server → Client (ACK)

```json
{
  "type": "ACK",
  "payload": {
    "message": "CONNECTED"
  }
}
```

or

```json
{
  "type": "ACK",
  "payload": {
    "message": "LOCATION_RECEIVED"
  }
}
```

### Server → Client (New Order)

```json
{
  "type": "NEW_ORDER",
  "payload": {
    "orderId": 101,
    "pickup": "123 Main St, Bangalore",
    "drop": "456 MG Road, Bangalore",
    "distanceKm": 12.5,
    "estimatedFare": 350.00,
    "helperRequired": false
  }
}
```

### Server → Client (Error)

```json
{
  "type": "ERROR",
  "payload": {
    "message": "Invalid latitude/longitude"
  }
}
```

## Configuration

### WebSocket URLs

- **Base URL**: `ws://43.205.235.73:8080`
- **Driver Connection**: `/ws/driver` (online status only)
- **Driver Location**: `/ws/driver/location` (online + location updates)

For HTTPS apps, use `wss://` instead of `ws://`.

### Location Update Interval

Default: 30 seconds

Can be changed in `LocationTrackingService`:
```kotlin
private const val LOCATION_UPDATE_INTERVAL = 30000L // milliseconds
```

Or when starting updates:
```kotlin
webSocketManager.startLocationUpdates(intervalSeconds = 45) { ... }
```

## Permissions Required

Already added to AndroidManifest.xml:
- `ACCESS_FINE_LOCATION` - For accurate GPS location
- `ACCESS_COARSE_LOCATION` - For approximate location
- `ACCESS_BACKGROUND_LOCATION` - For location when app is in background

## Important Notes

1. **Authentication**: JWT token is automatically included in WebSocket handshake from SharedPreference
2. **Auto-Reconnect**: WebSocket automatically reconnects on connection failure
3. **Battery Optimization**: Consider requesting battery optimization exclusion for background location
4. **Lifecycle Management**: Always call `disconnect()` or `stopTracking()` when going offline
5. **Error Handling**: Handle all error callbacks to provide user feedback

## Testing

1. **Go Online**: Click "Go Online" button → Driver status should change to ONLINE on server
2. **Location Updates**: Move around → Location should update every 30 seconds
3. **New Order**: Backend assigns order → Should receive NEW_ORDER message
4. **Go Offline**: Click "Go Offline" → WebSocket disconnects, driver status becomes OFFLINE

## Troubleshooting

**Issue**: WebSocket won't connect
- Check if JWT token exists in SharedPreference
- Verify BASE_URL is accessible
- Check network permissions in AndroidManifest.xml

**Issue**: Location not updating
- Verify location permissions are granted
- Check if GPS is enabled on device
- Ensure `connectDriverLocation()` is used (not `connectDriver()`)

**Issue**: Not receiving NEW_ORDER
- Confirm WebSocket is connected
- Check if driver status is ONLINE on backend
- Verify order assignment logic on backend
