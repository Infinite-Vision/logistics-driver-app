package com.example.logistics_driver_app.data.websocket

import android.content.Context
import android.util.Log
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * WebSocket Manager for driver real-time connection
 * Manages WebSocket connections for going online and sending location updates
 */
class DriverWebSocketManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "DriverWebSocketManager"
        private const val WS_BASE_URL = "wss://f3m8w0mx-8080.inc1.devtunnels.ms"
        private const val WS_DRIVER_PATH = "/ws/driver"
        private const val WS_DRIVER_LOCATION_PATH = "/ws/driver/location"
        private const val NORMAL_CLOSURE_STATUS = 1000
        private const val RECONNECT_DELAY_MS = 5000L
        
        @Volatile
        private var INSTANCE: DriverWebSocketManager? = null
        
        fun getInstance(context: Context): DriverWebSocketManager {
            return INSTANCE ?: synchronized(this) {
                val instance = DriverWebSocketManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val gson = Gson()
    private val sharedPreference = SharedPreference.getInstance(context)
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS) // Keep connection alive
        .build()

    private var webSocket: WebSocket? = null
    private var listener: WebSocketListener? = null
    private var isConnected = false
    private var shouldReconnect = false
    private var useLocationEndpoint = false
    
    // Location update timer
    private var locationUpdateTimer: Timer? = null

    /**
     * Connect to driver WebSocket (go online without location updates)
     * @param listener WebSocket event listener
     */
    fun connectDriver(listener: WebSocketListener) {
        this.listener = listener
        this.useLocationEndpoint = false
        connectWebSocket(WS_DRIVER_PATH)
    }

    /**
     * Connect to driver location WebSocket (go online with location updates)
     * @param listener WebSocket event listener
     */
    fun connectDriverLocation(listener: WebSocketListener) {
        Log.d(TAG, "connectDriverLocation called - Setting listener: ${listener.javaClass.simpleName}")
        this.listener = listener
        this.useLocationEndpoint = true
        connectWebSocket(WS_DRIVER_LOCATION_PATH)
    }

    /**
     * Internal method to establish WebSocket connection
     */
    private fun connectWebSocket(path: String) {
        if (isConnected) {
            Log.d(TAG, "WebSocket already connected - notifying listener")
            // Still notify the listener that connection is ready
            listener?.onConnected()
            return
        }

        val token = sharedPreference.getSessionToken()
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "No session token available")
            listener?.onConnectionError(Exception("No authentication token"))
            return
        }

        val url = "$WS_BASE_URL$path"
        Log.d(TAG, "Connecting to WebSocket: $url")

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()

        webSocket = okHttpClient.newWebSocket(request, createWebSocketListener())
        shouldReconnect = true
    }

    /**
     * Create OkHttp WebSocketListener
     */
    private fun createWebSocketListener(): okhttp3.WebSocketListener {
        return object : okhttp3.WebSocketListener() {
            
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                isConnected = true
                listener?.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
                handleMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code - $reason")
                webSocket.close(NORMAL_CLOSURE_STATUS, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code - $reason")
                isConnected = false
                stopLocationUpdates()
                listener?.onDisconnected(code, reason)
                
                // Auto-reconnect if needed
                if (shouldReconnect && code != NORMAL_CLOSURE_STATUS) {
                    scheduleReconnect()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket error: ${t.message}", t)
                isConnected = false
                stopLocationUpdates()
                listener?.onConnectionError(t)
                
                // Auto-reconnect on failure
                if (shouldReconnect) {
                    scheduleReconnect()
                }
            }
        }
    }

    /**
     * Handle incoming WebSocket message
     */
    private fun handleMessage(text: String) {
        try {
            val jsonObject = gson.fromJson(text, JsonObject::class.java)
            val type = jsonObject.get("type")?.asString
            val payloadElement = jsonObject.get("payload")

            when (type) {
                MessageType.CONNECTED -> {
                    // Server sends CONNECTED message when connection is established
                    val message = if (payloadElement != null && payloadElement.isJsonObject) {
                        val payload = gson.fromJson(payloadElement, AckPayload::class.java)
                        payload.message
                    } else {
                        "CONNECTED"
                    }
                    listener?.onAckReceived(message)
                    Log.d(TAG, "CONNECTED: $message")
                }
                
                MessageType.ACK -> {
                    val ackPayload = gson.fromJson(payloadElement, AckPayload::class.java)
                    listener?.onAckReceived(ackPayload.message)
                    Log.d(TAG, "ACK: ${ackPayload.message}")
                }
                
                MessageType.NEW_ORDER -> {
                    val orderPayload = gson.fromJson(payloadElement, NewOrderPayload::class.java)
                    Log.d(TAG, "NEW_ORDER received: Order ID ${orderPayload.orderId}, Listener is ${if (listener != null) "SET" else "NULL"}")
                    if (listener != null) {
                        Log.d(TAG, "Calling listener.onNewOrderReceived() on thread: ${Thread.currentThread().name}")
                        try {
                            listener?.onNewOrderReceived(orderPayload)
                            Log.d(TAG, "Listener callback completed successfully")
                        } catch (e: Exception) {
                            Log.e(TAG, "ERROR in listener callback: ${e.message}", e)
                        }
                    } else {
                        Log.e(TAG, "ERROR: Listener is NULL - cannot notify about new order!")
                    }
                }
                
                MessageType.ERROR -> {
                    val errorPayload = gson.fromJson(payloadElement, ErrorPayload::class.java)
                    listener?.onError(errorPayload.message)
                    Log.e(TAG, "ERROR: ${errorPayload.message}")
                }
                
                else -> {
                    Log.w(TAG, "Unknown message type: $type")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message: ${e.message}", e)
            listener?.onError("Failed to parse message: ${e.message}")
        }
    }

    /**
     * Send location update to server
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     */
    fun sendLocation(latitude: Double, longitude: Double) {
        if (!isConnected) {
            Log.w(TAG, "Cannot send location: WebSocket not connected")
            return
        }

        if (!useLocationEndpoint) {
            Log.w(TAG, "Location updates not enabled on this connection")
            return
        }

        // Validate coordinates
        if (latitude < -90 || latitude > 90) {
            Log.e(TAG, "Invalid latitude: $latitude")
            return
        }
        if (longitude < -180 || longitude > 180) {
            Log.e(TAG, "Invalid longitude: $longitude")
            return
        }

        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val locationPayload = LocationPayload(
                latitude = latitude,
                longitude = longitude,
                timestamp = timestamp
            )

            val message = WebSocketMessage(
                type = MessageType.LOCATION,
                payload = locationPayload
            )

            val jsonMessage = gson.toJson(message)
            webSocket?.send(jsonMessage)
            Log.d(TAG, "Location sent: $latitude, $longitude")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending location: ${e.message}", e)
            listener?.onError("Failed to send location: ${e.message}")
        }
    }

    /**
     * Start sending location updates periodically
     * @param intervalSeconds Interval between updates (default: 30 seconds)
     * @param locationProvider Callback to get current location
     */
    fun startLocationUpdates(
        intervalSeconds: Long = 30,
        locationProvider: () -> Pair<Double, Double>?
    ) {
        if (!useLocationEndpoint) {
            Log.w(TAG, "Location updates not enabled on this connection")
            return
        }

        stopLocationUpdates()

        locationUpdateTimer = Timer("LocationUpdateTimer", true).apply {
            schedule(object : TimerTask() {
                override fun run() {
                    if (isConnected) {
                        locationProvider()?.let { (lat, lng) ->
                            sendLocation(lat, lng)
                        }
                    }
                }
            }, 0, intervalSeconds * 1000)
        }

        Log.d(TAG, "Location updates started with ${intervalSeconds}s interval")
    }

    /**
     * Stop periodic location updates
     */
    fun stopLocationUpdates() {
        locationUpdateTimer?.cancel()
        locationUpdateTimer = null
        Log.d(TAG, "Location updates stopped")
    }

    /**
     * Disconnect from WebSocket (go offline)
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        shouldReconnect = false
        stopLocationUpdates()
        webSocket?.close(NORMAL_CLOSURE_STATUS, "Client disconnect")
        webSocket = null
        isConnected = false
    }

    /**
     * Check if WebSocket is currently connected
     */
    fun isConnected(): Boolean = isConnected

    /**
     * Schedule reconnection attempt
     */
    private fun scheduleReconnect() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (shouldReconnect && !isConnected) {
                    Log.d(TAG, "Attempting to reconnect...")
                    val path = if (useLocationEndpoint) WS_DRIVER_LOCATION_PATH else WS_DRIVER_PATH
                    connectWebSocket(path)
                }
            }
        }, RECONNECT_DELAY_MS)
    }

    /**
     * Remove listener
     */
    fun removeListener() {
        listener = null
    }
}
