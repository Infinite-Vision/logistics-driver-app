package com.example.logistics_driver_app.data.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.MainActivity
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.data.websocket.DriverWebSocketManager
import com.example.logistics_driver_app.data.websocket.NewOrderPayload
import com.example.logistics_driver_app.data.websocket.WebSocketListener
import com.google.android.gms.location.*

/**
 * Foreground service for maintaining WebSocket connection and location tracking
 * Survives navigation and app backgrounding
 */
class DriverLocationService : Service() {

    companion object {
        private const val TAG = "DriverLocationService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "driver_location_service"
        private const val CHANNEL_NAME = "Driver Location Service"
        private const val LOCATION_UPDATE_INTERVAL = 30000L // 30 seconds
        private const val FASTEST_LOCATION_INTERVAL = 15000L // 15 seconds
        
        // Actions
        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_GO_ONLINE = "ACTION_GO_ONLINE"
        const val ACTION_GO_OFFLINE = "ACTION_GO_OFFLINE"
        
        // SharedPreference keys
        private const val KEY_IS_ONLINE = "driver_is_online"
        
        /**
         * Reset online state — call on app start to guarantee OFFLINE on fresh launch.
         * When the app is force-killed onDestroy() may not run, leaving the flag stale.
         */
        fun resetOnlineState(context: Context) {
            SharedPreference.getInstance(context).putBoolean(KEY_IS_ONLINE, false)
        }

        /**
         * Check if driver is currently online
         */
        fun isDriverOnline(context: Context): Boolean {
            return SharedPreference.getInstance(context).getBoolean(KEY_IS_ONLINE, false)
        }
        
        /**
         * Start the service and go online
         */
        fun startService(context: Context) {
            val intent = Intent(context, DriverLocationService::class.java).apply {
                action = ACTION_GO_ONLINE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        /**
         * Last GPS location reported by this service — updated on every location fix.
         * Fragments use this to get the real device position for API calls.
         */
        @Volatile
        var lastKnownLocation: android.location.Location? = null
            private set

        /**
         * Stop the service and go offline
         */
        fun stopService(context: Context) {
            Log.d(TAG, "[STOP_SERVICE] stopService() called - stopping service directly")
            val intent = Intent(context, DriverLocationService::class.java)
            context.stopService(intent)
            Log.d(TAG, "[STOP_SERVICE] stopService() called - service will trigger onDestroy()")
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var webSocketManager: DriverWebSocketManager
    private lateinit var sharedPreference: SharedPreference
    private var currentLocation: Location? = null
    private var isConnected = false

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
                lastKnownLocation = location  // expose real GPS for fragments
                Log.d(TAG, "[LOCATION] Updated: ${location.latitude}, ${location.longitude}")
                
                // Send location to WebSocket if connected
                if (isConnected && webSocketManager.isConnected()) {
                    webSocketManager.sendLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "[LIFECYCLE] Service onCreate")
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        webSocketManager = DriverWebSocketManager.getInstance(this)
        sharedPreference = SharedPreference.getInstance(this)
        
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: "NULL"
        Log.d(TAG, "[LIFECYCLE] =====================================")
        Log.d(TAG, "[LIFECYCLE] onStartCommand called")
        Log.d(TAG, "[LIFECYCLE] Action: $action")
        Log.d(TAG, "[LIFECYCLE] Current state: ${if (sharedPreference.getBoolean(KEY_IS_ONLINE, false)) "ONLINE" else "OFFLINE"}")
        Log.d(TAG, "[LIFECYCLE] =====================================")
        
        when (intent?.action) {
            ACTION_GO_ONLINE -> {
                Log.i(TAG, "[ACTION] Processing ACTION_GO_ONLINE")
                
                // CRITICAL: Call startForeground() IMMEDIATELY to avoid crash
                // This must be done within 5-10 seconds of startForegroundService()
                startForeground(NOTIFICATION_ID, createNotification("Going online...", true))
                Log.d(TAG, "[FOREGROUND] Service promoted to foreground")
                
                // Clear state and force fresh connection
                isConnected = false
                
                // Now proceed with going online
                goOnline()
            }
            else -> {
                Log.w(TAG, "[ACTION] No action specified or service restarted")
                // Service restarted by system - check if should be online
                if (sharedPreference.getBoolean(KEY_IS_ONLINE, false)) {
                    Log.i(TAG, "[RESTART] Service restarted - restoring online state")
                    
                    // Must call startForeground for restarted service too
                    startForeground(NOTIFICATION_ID, createNotification("Reconnecting...", true))
                    Log.d(TAG, "[FOREGROUND] Service promoted to foreground (restart)")
                    
                    goOnline()
                } else {
                    Log.d(TAG, "[RESTART] Service started but driver was offline - stopping")
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }
        
        // START_STICKY ensures service restarts if killed by system
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Go online - Start foreground service, connect WebSocket, track location
     */
    @SuppressLint("MissingPermission")
    private fun goOnline() {
        Log.i(TAG, "[WEBSOCKET] =====================================")
        Log.i(TAG, "[WEBSOCKET] Going ONLINE - Starting connection")
        Log.i(TAG, "[WEBSOCKET] =====================================")
        
        // Check if already online AND connected
        val alreadyOnline = sharedPreference.getBoolean(KEY_IS_ONLINE, false)
        if (alreadyOnline && isConnected) {
            Log.i(TAG, "[STATE] Already ONLINE and connected - re-sending broadcast for UI sync")
            
            // Update notification
            updateNotification("Online - Tracking location", true)
            
            // Re-send broadcast to sync UI
            val onlineBroadcast = Intent("com.example.logistics_driver_app.DRIVER_ONLINE")
            sendBroadcast(onlineBroadcast)
            Log.d(TAG, "[BROADCAST] ✓ Sent DRIVER_ONLINE broadcast (already online)")
            return
        }
        
        if (alreadyOnline && !isConnected) {
            Log.w(TAG, "[STATE] Marked ONLINE but WebSocket disconnected - reconnecting")
        }
        
        // Save online state FIRST
        sharedPreference.putBoolean(KEY_IS_ONLINE, true)
        Log.d(TAG, "[STATE] Saved online state to SharedPreferences")
        
        // Broadcast that we're going online (UI updates immediately)
        val onlineBroadcast = Intent("com.example.logistics_driver_app.DRIVER_ONLINE")
        sendBroadcast(onlineBroadcast)
        Log.d(TAG, "[BROADCAST] ✓ Sent DRIVER_ONLINE broadcast")
        
        // Check permissions
        if (!hasLocationPermissions()) {
            Log.e(TAG, "[ERROR] Location permissions not granted")
            updateNotification("Error: No location permission", true)
            return
        }
        
        // Start location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnSuccessListener {
            Log.d(TAG, "[LOCATION] Location updates started successfully")
        }.addOnFailureListener { e ->
            Log.e(TAG, "[ERROR] Failed to start location updates: ${e.message}", e)
            updateNotification("Error: Cannot get location", true)
        }
        
        // Get last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLocation = it
                Log.d(TAG, "[LOCATION] Last known: ${it.latitude}, ${it.longitude}")
            }
        }
        
        // Connect to WebSocket (async, may fail)
        connectToWebSocket()
        
        Log.i(TAG, "[STATUS] Driver is now ONLINE - Connection initiated")
    }

    /**
     * Go offline - Stop location tracking, disconnect WebSocket, stop service
     */
    private fun goOffline() {
        Log.i(TAG, "[WEBSOCKET] =====================================  ")
        Log.i(TAG, "[WEBSOCKET] Going OFFLINE - Starting shutdown")
        Log.i(TAG, "[WEBSOCKET] =====================================")
        
        isConnected = false
        
        // Save offline state FIRST
        sharedPreference.putBoolean(KEY_IS_ONLINE, false)
        Log.d(TAG, "[STATE] Saved offline state to SharedPreferences")
        
        // Broadcast that we're going offline (UI updates immediately)
        val offlineBroadcast = Intent("com.example.logistics_driver_app.DRIVER_OFFLINE")
        sendBroadcast(offlineBroadcast)
        Log.d(TAG, "[BROADCAST] ✓ Sent DRIVER_OFFLINE broadcast")
        
        // Stop location updates
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.d(TAG, "[LOCATION] Stopped location updates")
        } catch (e: Exception) {
            Log.e(TAG, "[ERROR] Failed to stop location updates: ${e.message}")
        }
        
        // Disconnect WebSocket
        try {
            webSocketManager.disconnect()
            Log.d(TAG, "[WEBSOCKET] Disconnected WebSocket")
        } catch (e: Exception) {
            Log.e(TAG, "[ERROR] Failed to disconnect WebSocket: ${e.message}")
        }
        
        Log.i(TAG, "[STATUS] Driver is now OFFLINE - Shutdown complete")
    }

    /**
     * Connect to WebSocket server
     */
    private fun connectToWebSocket() {
        Log.d(TAG, "[WEBSOCKET] Attempting to connect...")
        updateNotification("Connecting to server...", true)
        
        webSocketManager.connectDriverLocation(object : WebSocketListener {
            override fun onConnected() {
                isConnected = true
                Log.i(TAG, "[WEBSOCKET] Connected successfully")
                updateNotification("Online - Tracking location", true)
                
                // Start periodic location updates via WebSocket
                webSocketManager.startLocationUpdates(30) {
                    currentLocation?.let { loc ->
                        Pair(loc.latitude, loc.longitude)
                    }
                }
            }

            override fun onDisconnected(code: Int, reason: String) {
                isConnected = false
                Log.w(TAG, "[WEBSOCKET] Disconnected: $reason")
                updateNotification("Disconnected - Reconnecting...", true)
            }

            override fun onAckReceived(message: String) {
                Log.d(TAG, "[WEBSOCKET] ACK: $message")
            }

            override fun onNewOrderReceived(order: NewOrderPayload) {
                Log.i(TAG, "[ORDER] ========================================")
                Log.i(TAG, "[ORDER] onNewOrderReceived CALLED - Thread: ${Thread.currentThread().name}")
                Log.i(TAG, "[ORDER] ========================================")
                Log.i(TAG, "[ORDER] New order received: ${order.orderId}")
                Log.d(TAG, "[ORDER] Pickup: ${order.pickup}, Drop: ${order.drop}, Fare: ${order.estimatedFare}")
                Log.d(TAG, "[ORDER] Customer: ${order.customerName ?: "Unknown"}, Distance: ${order.distanceKm} km")
                
                // Post to main thread to send broadcast (broadcasts should be sent on main thread)
                Handler(Looper.getMainLooper()).post {
                    try {
                        Log.d(TAG, "[BROADCAST] Posting broadcast on main thread")
                        // Send broadcast for new order with all data
                        val intent = Intent("com.example.logistics_driver_app.NEW_ORDER").apply {
                            setPackage(packageName) // Ensure broadcast is delivered to this app
                            putExtra("order_id", order.orderId)
                            putExtra("pickup_address", order.pickup)
                            putExtra("drop_address", order.drop)
                            putExtra("distance_km", order.distanceKm)
                            putExtra("estimated_fare", order.estimatedFare.toInt())
                            putExtra("helper_required", order.helperRequired)
                            putExtra("customer_name", order.customerName ?: "Customer")
                        }
                        sendBroadcast(intent)
                        Log.d(TAG, "[BROADCAST] ✓ Sent NEW_ORDER broadcast for order #${order.orderId} to package: $packageName")
                    } catch (e: Exception) {
                        Log.e(TAG, "[ERROR] Failed to send broadcast: ${e.message}", e)
                    }
                    
                    try {
                        // Update notification
                        updateNotification("New Order from ${order.customerName ?: "Customer"}!", false)
                    } catch (e: Exception) {
                        Log.e(TAG, "[ERROR] Failed to update notification: ${e.message}", e)
                    }
                }
            }

            override fun onError(errorMessage: String) {
                Log.e(TAG, "[WEBSOCKET] Error: $errorMessage")
                updateNotification("Online (Server connecting...)", true)
            }

            override fun onConnectionError(throwable: Throwable) {
                Log.e(TAG, "[WEBSOCKET] Connection error: ${throwable.message}")
                updateNotification("Online (Server unavailable)", true)
                // Don't keep retrying if getting 404 - server endpoint doesn't exist
                if (throwable.message?.contains("404") == true) {
                    Log.w(TAG, "[WEBSOCKET] Server endpoint not found (404) - stopping reconnection attempts")
                }
            }
        })
    }

    /**
     * Create notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps driver online and tracks location"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            
            Log.d(TAG, "[NOTIFICATION] Channel created")
        }
    }

    /**
     * Create notification for foreground service
     */
    private fun createNotification(message: String, ongoing: Boolean): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Driver Mode: Online")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_location) // Make sure you have this icon
            .setContentIntent(pendingIntent)
            .setOngoing(ongoing)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    /**
     * Update foreground notification
     */
    private fun updateNotification(message: String, ongoing: Boolean) {
        val notification = createNotification(message, ongoing)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Check if location permissions are granted
     */
    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        Log.w(TAG, "[LIFECYCLE] Service onDestroy - beginning cleanup")
        
        // Always perform full cleanup to ensure clean state
        Log.i(TAG, "[SHUTDOWN] Service destroyed - performing cleanup")
        goOffline()
        
        // Mark as not connected
        isConnected = false
        
        // Always clean up location updates
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            // Ignore errors during final cleanup
        }
        
        super.onDestroy()
    }
}
