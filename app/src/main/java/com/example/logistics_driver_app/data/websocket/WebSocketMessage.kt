package com.example.logistics_driver_app.data.websocket

import com.google.gson.annotations.SerializedName

/**
 * WebSocket message types
 */
object MessageType {
    const val LOCATION = "LOCATION"
    const val ACK = "ACK"
    const val CONNECTED = "CONNECTED"
    const val NEW_ORDER = "NEW_ORDER"
    const val ERROR = "ERROR"
}

/**
 * Base WebSocket message structure
 */
data class WebSocketMessage(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("payload")
    val payload: Any
)

/**
 * Location payload for sending location updates
 */
data class LocationPayload(
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("timestamp")
    val timestamp: String? = null
)

/**
 * ACK payload from server
 */
data class AckPayload(
    @SerializedName("message")
    val message: String
)

/**
 * New order payload from server
 */
data class NewOrderPayload(
    @SerializedName("orderId")
    val orderId: Long,
    
    @SerializedName("pickup")
    val pickup: String,
    
    @SerializedName("drop")
    val drop: String,
    
    @SerializedName("distanceKm")
    val distanceKm: Double,
    
    @SerializedName("estimatedFare")
    val estimatedFare: Double,
    
    @SerializedName("helperRequired")
    val helperRequired: Boolean,
    
    @SerializedName("customerName")
    val customerName: String? = null
)

/**
 * Error payload from server
 */
data class ErrorPayload(
    @SerializedName("message")
    val message: String
)
