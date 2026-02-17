package com.example.logistics_driver_app.data.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.logistics_driver_app.data.websocket.DriverWebSocketManager
import com.example.logistics_driver_app.data.websocket.NewOrderPayload
import com.example.logistics_driver_app.data.websocket.WebSocketListener
import com.google.android.gms.location.*

/**
 * Location tracking service that integrates with WebSocket
 * Manages location updates and sends them to the server via WebSocket
 */
class LocationTrackingService private constructor(private val context: Context) {

    companion object {
        private const val TAG = "LocationTrackingService"
        private const val LOCATION_UPDATE_INTERVAL = 30000L // 30 seconds
        private const val FASTEST_LOCATION_INTERVAL = 15000L // 15 seconds
        
        @Volatile
        private var INSTANCE: LocationTrackingService? = null
        
        fun getInstance(context: Context): LocationTrackingService {
            return INSTANCE ?: synchronized(this) {
                val instance = LocationTrackingService(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val webSocketManager = DriverWebSocketManager.getInstance(context)
    
    private var currentLocation: Location? = null
    private var isTracking = false
    private var serviceListener: LocationTrackingListener? = null

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        LOCATION_UPDATE_INTERVAL
    ).apply {
        setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
        setWaitForAccurateLocation(false)
    }.build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                currentLocation = location
                Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude}")
                
                // Send location to WebSocket if connected
                if (webSocketManager.isConnected()) {
                    webSocketManager.sendLocation(location.latitude, location.longitude)
                }
                
                // Notify listener
                serviceListener?.onLocationUpdated(location.latitude, location.longitude)
            }
        }
    }

    /**
     * Start location tracking and connect to WebSocket
     */
    @SuppressLint("MissingPermission")
    fun startTracking(listener: LocationTrackingListener) {
        if (isTracking) {
            Log.d(TAG, "Already tracking location")
            return
        }

        // Check permissions
        if (!hasLocationPermissions()) {
            Log.e(TAG, "Location permissions not granted")
            listener.onError("Location permissions not granted")
            return
        }

        this.serviceListener = listener
        isTracking = true

        Log.d(TAG, "Starting location tracking")

        // Start location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnSuccessListener {
            Log.d(TAG, "Location updates started successfully")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to start location updates: ${e.message}", e)
            listener.onError("Failed to start location updates: ${e.message}")
            isTracking = false
        }

        // Get last known location immediately
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLocation = it
                Log.d(TAG, "Last known location: ${it.latitude}, ${it.longitude}")
            }
        }

        // Connect to WebSocket for location updates
        connectToWebSocket()
    }

    /**
     * Connect to WebSocket server
     */
    private fun connectToWebSocket() {
        webSocketManager.connectDriverLocation(object : WebSocketListener {
            override fun onConnected() {
                Log.d(TAG, "WebSocket connected - Driver is now ONLINE")
                serviceListener?.onDriverOnline()
                
                // Start periodic location updates via WebSocket
                webSocketManager.startLocationUpdates(30) {
                    currentLocation?.let { loc ->
                        Pair(loc.latitude, loc.longitude)
                    }
                }
            }

            override fun onDisconnected(code: Int, reason: String) {
                Log.d(TAG, "WebSocket disconnected - Driver is now OFFLINE")
                serviceListener?.onDriverOffline()
            }

            override fun onAckReceived(message: String) {
                Log.d(TAG, "ACK received: $message")
                when (message) {
                    "CONNECTED" -> serviceListener?.onDriverOnline()
                    "LOCATION_RECEIVED" -> {
                        // Location acknowledged by server
                    }
                }
            }

            override fun onNewOrderReceived(order: NewOrderPayload) {
                Log.d(TAG, "New order received: ${order.orderId}")
                serviceListener?.onNewOrderReceived(order)
            }

            override fun onError(errorMessage: String) {
                Log.e(TAG, "WebSocket error: $errorMessage")
                serviceListener?.onError(errorMessage)
            }

            override fun onConnectionError(throwable: Throwable) {
                Log.e(TAG, "WebSocket connection error: ${throwable.message}", throwable)
                serviceListener?.onError("Connection error: ${throwable.message}")
            }
        })
    }

    /**
     * Stop location tracking and disconnect from WebSocket
     */
    fun stopTracking() {
        if (!isTracking) {
            Log.d(TAG, "Not currently tracking")
            return
        }

        Log.d(TAG, "Stopping location tracking")
        isTracking = false

        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // Disconnect WebSocket
        webSocketManager.disconnect()

        serviceListener?.onDriverOffline()
        Log.d(TAG, "Location tracking stopped - Driver is now OFFLINE")
    }

    /**
     * Check if location permissions are granted
     */
    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get current location
     */
    fun getCurrentLocation(): Pair<Double, Double>? {
        return currentLocation?.let { Pair(it.latitude, it.longitude) }
    }

    /**
     * Check if currently tracking
     */
    fun isTracking(): Boolean = isTracking

    /**
     * Check if driver is online (connected to WebSocket)
     */
    fun isOnline(): Boolean = webSocketManager.isConnected()

    /**
     * Remove listener
     */
    fun removeListener() {
        serviceListener = null
        webSocketManager.removeListener()
    }
}

/**
 * Listener for location tracking events
 */
interface LocationTrackingListener {
    /**
     * Called when location is updated
     */
    fun onLocationUpdated(latitude: Double, longitude: Double)
    
    /**
     * Called when driver goes online (WebSocket connected)
     */
    fun onDriverOnline()
    
    /**
     * Called when driver goes offline (WebSocket disconnected)
     */
    fun onDriverOffline()
    
    /**
     * Called when a new order is received
     */
    fun onNewOrderReceived(order: NewOrderPayload)
    
    /**
     * Called when an error occurs
     */
    fun onError(message: String)
}
