# WebSocket and Trip Flow Implementation

## Overview
This document describes the complete WebSocket and trip flow implementation based on the `websocket-and-trip-flow.html` reference file.

## ✅ Implementation Status

### 1. WebSocket Connection Flow
**Reference**: `/ws/driver/location` endpoint with token authentication

#### Implementation:
- **File**: `DriverWebSocketManager.kt`
- **Endpoint**: `ws://43.205.235.73:8080/ws/driver/location`
- **Authentication**: Bearer token in Authorization header
- **Status**: ✅ **IMPLEMENTED**

#### Features:
- ✅ Connect to `/ws/driver` (go online without location)
- ✅ Connect to `/ws/driver/location` (go online with location updates)
- ✅ Send LOCATION messages every 30 seconds
- ✅ Handle CONNECTED acknowledgment
- ✅ Handle LOCATION_RECEIVED acknowledgments
- ✅ Handle NEW_ORDER push messages
- ✅ Handle ERROR messages
- ✅ Auto-reconnection on failure
- ✅ Proper disconnect handling

#### Message Types Supported:
```kotlin
object MessageType {
    const val LOCATION = "LOCATION"           // Sent by client
    const val ACK = "ACK"                     // Received from server
    const val CONNECTED = "CONNECTED"         // Received on connection
    const val NEW_ORDER = "NEW_ORDER"         // Received when order offered
    const val ERROR = "ERROR"                 // Received on errors
}
```

#### Connection Flow:
```
1. Client connects to ws://host/ws/driver/location
2. Server sends: {"type":"ACK","payload":{"message":"CONNECTED"}}
   OR: {"type":"CONNECTED"}
3. Client sends LOCATION every 30s:
   {"type":"LOCATION","payload":{"latitude":12.9165,"longitude":79.1325,"timestamp":"..."}}
4. Server responds: {"type":"ACK","payload":{"message":"LOCATION_RECEIVED"}}
5. When order offered, server sends:
   {"type":"NEW_ORDER","payload":{orderId, pickup, drop, ...}}
```

---

### 2. Trip Flow API Endpoints
**Reference**: Backend API endpoints from HTML reference

#### Implementation:
- **File**: `ApiService.kt`
- **Models**: `TripApiModels.kt`
- **Status**: ✅ **IMPLEMENTED**

#### Endpoints Added:

##### 9.1 Accept Order
```kotlin
POST /api/v1/driver/orders/{orderId}/accept
Response: ApiResponse<TripAcceptResponse>
```

##### 9.2 Reject Order
```kotlin
POST /api/v1/driver/orders/{orderId}/reject
Response: 204 No Content
```

##### 9.3 Arrived at Pickup
```kotlin
POST /api/v1/driver/orders/{orderId}/arrived-pickup
Body: ArrivedAtPickupRequest { latitude, longitude }
Response: ApiResponse<ArrivedAtPickupResponse>
```

##### 9.4 Start Trip (with OTP)
```kotlin
POST /api/v1/driver/orders/{orderId}/start-trip/confirm
Body: StartTripRequest { otp, latitude, longitude }
Response: ApiResponse<StartTripResponse>
```

##### 9.5 Arrived at Drop
```kotlin
POST /api/v1/driver/orders/{orderId}/arrived-drop
Body: ArrivedAtDropRequest { latitude, longitude }
Response: ApiResponse<ArrivedAtDropResponse>
```

##### 9.6 End Trip
```kotlin
POST /api/v1/driver/orders/{orderId}/end-trip
Body: EndTripRequest { latitude, longitude }
Response: ApiResponse<EndTripResponse>
```

---

### 3. Trip History API Endpoints
**Reference**: Dashboard and trips endpoints

#### Implementation:
- **File**: `ApiService.kt`
- **Status**: ✅ **IMPLEMENTED**

#### Endpoints Added:

##### 10.1 Get Trip List
```kotlin
GET /api/v1/driver/trips?filter={today|thisWeek|thisMonth}&page=0&size=20
Response: ApiResponse<TripListResponse>
```

##### 10.2 Get Trip Detail
```kotlin
GET /api/v1/driver/trips/{orderId}
Response: ApiResponse<TripDetailResponse>
```

---

### 4. Dashboard Summary
**Reference**: Driver home summary endpoint

#### Implementation:
- **File**: `ApiService.kt`
- **Status**: ✅ **ALREADY EXISTED**

```kotlin
GET /api/v1/driver/home/summary
Response: ApiResponse<DriverHomeSummaryResponse>
```

---

## 📋 Complete Trip Flow Sequence

Based on the HTML reference, here's the complete trip flow:

### 1. Driver Goes Online
```
Driver slides "Go Online"
→ DriverLocationService.startService()
→ WebSocket connects to /ws/driver/location
→ Server sends CONNECTED ACK
→ Service broadcasts DRIVER_ONLINE
→ UI updates to "You are Online"
→ Location updates start (every 30s)
```

### 2. Admin Creates Order
```
Admin creates order (automatic or manual)
→ Server finds nearest online driver OR specific driver
→ Server sends NEW_ORDER via WebSocket
```

### 3. Driver Receives Order
```
NEW_ORDER WebSocket message received
→ Service broadcasts NEW_ORDER intent
→ TripRequestBottomSheet shows order details
→ Driver has 30 seconds to respond
```

### 4. Driver Accepts/Rejects Order

**Accept Flow:**
```
Driver clicks "Accept"
→ POST /api/v1/driver/orders/{orderId}/accept
→ Status: OFFERED → ACCEPTED
→ Navigate to trip active screen
```

**Reject Flow:**
```
Driver clicks "Reject"
→ POST /api/v1/driver/orders/{orderId}/reject
→ Returns to home screen
→ Order offered to next driver
```

### 5. Driver Drives to Pickup
```
Driver navigates to pickup location
→ Geofence check triggers
→ "Mark Arrived" button enabled
```

### 6. Driver Arrives at Pickup
```
Driver clicks "Mark Arrived at Pickup"
→ POST /api/v1/driver/orders/{orderId}/arrived-pickup
   Body: { latitude, longitude }
→ Status: ACCEPTED → ARRIVED_PICKUP
→ Server generates start-trip OTP
→ Response includes OTP
→ "Start Trip" button enabled
```

### 7. Driver Starts Trip
```
Driver enters OTP and clicks "Start Trip"
→ POST /api/v1/driver/orders/{orderId}/start-trip/confirm
   Body: { otp, latitude, longitude }
→ Status: ARRIVED_PICKUP → IN_PROGRESS
→ Trip timer starts
```

### 8. Driver Drives to Drop
```
Driver navigates to drop location
→ Geofence check triggers
→ "Mark Arrived at Drop" button enabled
```

### 9. Driver Arrives at Drop
```
Driver clicks "Mark Arrived at Drop"
→ POST /api/v1/driver/orders/{orderId}/arrived-drop
   Body: { latitude, longitude }
→ Status: IN_PROGRESS → ARRIVED_AT_DROP
→ "Complete Trip" button enabled
```

### 10. Driver Completes Trip
```
Driver clicks "Complete Trip"
→ POST /api/v1/driver/orders/{orderId}/end-trip
   Body: { latitude, longitude }
→ Status: ARRIVED_AT_DROP → COMPLETED
→ completedAt timestamp set
→ Navigate to trip summary/earnings screen
```

---

## 🔧 Usage Examples

### Using Trip APIs in ViewModel

```kotlin
class TripViewModel : ViewModel() {
    
    suspend fun acceptOrder(orderId: Long) {
        try {
            val token = "Bearer ${sharedPreference.getSessionToken()}"
            val response = apiService.acceptOrder(token, orderId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                Log.d(TAG, "Order accepted: ${data?.orderId}, status: ${data?.status}")
                // Navigate to trip active screen
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting order", e)
        }
    }
    
    suspend fun arrivedAtPickup(orderId: Long, lat: Double, lng: Double) {
        try {
            val token = "Bearer ${sharedPreference.getSessionToken()}"
            val request = ArrivedAtPickupRequest(lat, lng)
            val response = apiService.arrivedAtPickup(token, orderId, request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                Log.d(TAG, "Arrived at pickup. OTP: ${data?.otp}")
                // Show OTP to driver
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking arrived at pickup", e)
        }
    }
    
    suspend fun startTrip(orderId: Long, otp: String, lat: Double, lng: Double) {
        try {
            val token = "Bearer ${sharedPreference.getSessionToken()}"
            val request = StartTripRequest(otp, lat, lng)
            val response = apiService.startTrip(token, orderId, request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "Trip started successfully")
                // Start trip timer, update UI
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting trip", e)
        }
    }
}
```

### Getting Trip History

```kotlin
class TripHistoryViewModel : ViewModel() {
    
    suspend fun getTodaysTrips() {
        try {
            val token = "Bearer ${sharedPreference.getSessionToken()}"
            val response = apiService.getTripList(
                token = token,
                filter = TripFilter.TODAY.value,
                page = 0,
                size = 20
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val trips = response.body()?.data?.content
                Log.d(TAG, "Found ${trips?.size} trips today")
                // Update UI
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trips", e)
        }
    }
    
    suspend fun getTripDetail(orderId: Long) {
        try {
            val token = "Bearer ${sharedPreference.getSessionToken()}"
            val response = apiService.getTripDetail(token, orderId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val trip = response.body()?.data
                Log.d(TAG, "Trip detail: $trip")
                // Show trip details
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trip detail", e)
        }
    }
}
```

---

## 📝 Key Implementation Notes

### 1. WebSocket Reconnection
- Auto-reconnects on failure with 5-second delay
- Maintains `shouldReconnect` flag
- Only reconnects if explicitly online
- Stops location updates on disconnect

### 2. Location Updates
- Sent every 30 seconds when connected to `/ws/driver/location`
- Validates coordinates before sending
- Includes UTC timestamp in ISO format
- Timer-based using `java.util.Timer`

### 3. Token Authentication
- Uses Authorization header: `Bearer {token}`
- Token retrieved from SharedPreferences
- Same token used for WebSocket and HTTP APIs

### 4. Error Handling
- WebSocket errors trigger `onConnectionError` callback
- API errors return appropriate HTTP status codes
- Geofence validation on server side for trip actions

### 5. Broadcast Events
- `DRIVER_ONLINE` - When WebSocket connects and service starts
- `DRIVER_OFFLINE` - When WebSocket disconnects and service stops
- `NEW_ORDER` - When order is offered via WebSocket

---

## 🎯 Next Steps for UI Integration

1. **Create Trip Active Fragment**
   - Show current trip details
   - Display map with route
   - Show appropriate action buttons based on trip status

2. **Implement Geofencing**
   - Check distance to pickup/drop locations
   - Enable/disable buttons based on proximity
   - Show distance to destination

3. **Add OTP Input Dialog**
   - Show after arriving at pickup
   - Validate OTP before starting trip
   - Show error for invalid OTP

4. **Create Trip Summary Screen**
   - Show after trip completion
   - Display earnings, distance, duration
   - Option to view trip details

5. **Add Trip History Screen**
   - List trips with filters (today/week/month)
   - Show trip cards with summary
   - Navigate to detail screen on tap

---

## 📚 Reference Files

- **HTML Reference**: `design/websocket-and-trip-flow.html`
- **WebSocket Manager**: `app/src/main/java/com/example/logistics_driver_app/data/websocket/DriverWebSocketManager.kt`
- **WebSocket Models**: `app/src/main/java/com/example/logistics_driver_app/data/websocket/WebSocketMessage.kt`
- **API Service**: `app/src/main/java/com/example/logistics_driver_app/data/NetworkCall/ApiService.kt`
- **Trip API Models**: `app/src/main/java/com/example/logistics_driver_app/data/model/TripApiModels.kt`
- **Location Service**: `app/src/main/java/com/example/logistics_driver_app/data/service/DriverLocationService.kt`

---

## ✅ Checklist

- [x] WebSocket connection to `/ws/driver/location`
- [x] LOCATION message sending (every 30s)
- [x] CONNECTED message handling
- [x] LOCATION_RECEIVED ACK handling
- [x] NEW_ORDER message handling
- [x] ERROR message handling
- [x] Accept order endpoint
- [x] Reject order endpoint
- [x] Arrived at pickup endpoint
- [x] Start trip endpoint (with OTP)
- [x] Arrived at drop endpoint
- [x] End trip endpoint
- [x] Get trip list endpoint
- [x] Get trip detail endpoint
- [x] Driver home summary endpoint (already existed)
- [x] Request/response models for all endpoints
- [x] Auto-reconnection logic
- [x] Location update timer
- [x] Broadcast events for UI sync

**Implementation Status**: ✅ **COMPLETE**

---

*Last Updated: 2026-02-19*
*Based on: websocket-and-trip-flow.html reference*
