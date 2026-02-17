package com.example.logistics_driver_app.data.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
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
         * Stop the service and go offline
         */
        fun stopService(context: Context) {
            val intent = Intent(context, DriverLocationService::class.java).apply {
                action = ACTION_GO_OFFLINE
            }
            context.startService(intent)
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
        Log.d(TAG, "[LIFECYCLE] onStartCommand - Action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_GO_ONLINE -> {
                goOnline()
            }
            ACTION_GO_OFFLINE -> {
                goOffline()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // Service restarted by system - check if should be online
                if (sharedPreference.getBoolean(KEY_IS_ONLINE, false)) {
                    Log.i(TAG, "[RESTART] Service restarted - restoring online state")
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
        Log.i(TAG, "[WEBSOCKET] Going ONLINE")
        
        // Start foreground service with notification
        startForeground(NOTIFICATION_ID, createNotification("Connecting...", true))
        
        // Save online state
        sharedPreference.putBoolean(KEY_IS_ONLINE, true)
        
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
        
        // Connect to WebSocket
        connectToWebSocket()
    }

    /**
     * Go offline - Stop location tracking, disconnect WebSocket, stop service
     */
    private fun goOffline() {
        Log.i(TAG, "[WEBSOCKET] Going OFFLINE")
        
        isConnected = false
        
        // Save offline state
        sharedPreference.putBoolean(KEY_IS_ONLINE, false)
        
        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)
        
        // Disconnect WebSocket
        webSocketManager.disconnect()
        
        Log.i(TAG, "[STATUS] Driver is now OFFLINE")
    }

    /**
     * Connect to WebSocket server
     */
    private fun connectToWebSocket() {
        Log.d(TAG, "[WEBSOCKET] Attempting to connect...")
        
        webSocketManager.connectDriverLocation(object : WebSocketListener {
            override fun onConnected() {
                isConnected = true
                Log.i(TAG, "[WEBSOCKET] Connected - Driver is now ONLINE")
                updateNotification("Online - Tracking location", true)
                
                // Start periodic location updates via WebSocket
                webSocketManager.startLocationUpdates(30) {
                    currentLocation?.let { loc ->
                        Pair(loc.latitude, loc.longitude)
                    }
                }
                
                // Broadcast online status
                sendBroadcast(Intent("com.example.logistics_driver_app.DRIVER_ONLINE"))
            }

            override fun onDisconnected(code: Int, reason: String) {
                isConnected = false
                Log.w(TAG, "[WEBSOCKET] Disconnected: $reason")
                updateNotification("Disconnected - Reconnecting...", true)
                
                // Broadcast offline status
                sendBroadcast(Intent("com.example.logistics_driver_app.DRIVER_OFFLINE"))
            }

            override fun onAckReceived(message: String) {
                Log.d(TAG, "[WEBSOCKET] ACK: $message")
            }

            override fun onNewOrderReceived(order: NewOrderPayload) {
                Log.i(TAG, "[ORDER] New order received: ${order.orderId}")
                
                // Send broadcast for new order
                sendBroadcast(Intent("com.example.logistics_driver_app.NEW_ORDER").apply {
                    putExtra("order_id", order.orderId)
                    putExtra("pickup_address", order.pickup)
                    putExtra("dropoff_address", order.drop)
                })
                
                // Update notification
                updateNotification("New Order! ${order.pickup}", false)
            }

            override fun onError(errorMessage: String) {
                Log.e(TAG, "[WEBSOCKET] Error: $errorMessage")
                updateNotification("Error: $errorMessage", true)
            }

            override fun onConnectionError(throwable: Throwable) {
                Log.e(TAG, "[WEBSOCKET] Connection error: ${throwable.message}", throwable)
                updateNotification("Connection failed - Retrying...", true)
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
        
        // Action to go offline
        val offlineIntent = Intent(this, DriverLocationService::class.java).apply {
            action = ACTION_GO_OFFLINE
        }
        val offlinePendingIntent = PendingIntent.getService(
            this,
            1,
            offlineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Driver Mode: Online")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_location) // Make sure you have this icon
            .setContentIntent(pendingIntent)
            .setOngoing(ongoing)
            .addAction(R.drawable.ic_close, "Go Offline", offlinePendingIntent)
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
        Log.w(TAG, "[LIFECYCLE] Service onDestroy")
        
        // Clean up
        fusedLocationClient.removeLocationUpdates(locationCallback)
        
        // If driver should be online, service will restart due to START_STICKY
        if (sharedPreference.getBoolean(KEY_IS_ONLINE, false)) {
            Log.i(TAG, "[RESTART] Service destroyed but driver is online - will restart")
        }
        
        super.onDestroy()
    }
}
