package com.example.logistics_driver_app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Trip API Models for driver trip flow operations
 * Based on Backend Specification and websocket-and-trip-flow.html reference
 */

// ============= Trip Action Request Models =============

/**
 * Request model for marking arrived at pickup
 */
data class ArrivedAtPickupRequest(
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
)

/**
 * Request model for starting trip with OTP
 */
data class StartTripRequest(
    @SerializedName("otp")
    val otp: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
)

/**
 * Request model for marking arrived at drop
 */
data class ArrivedAtDropRequest(
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
)

/**
 * Request model for ending trip
 */
data class EndTripRequest(
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
)

// ============= Trip Response Models =============

/**
 * Response for trip acceptance
 */
data class TripAcceptResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("orderId")
    val orderId: Long,
    
    @SerializedName("status")
    val status: String
)

/**
 * Response for arrived at pickup
 */
data class ArrivedAtPickupResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("orderId")
    val orderId: Long,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("otp")
    val otp: String? = null
)

/**
 * Response for start trip
 */
data class StartTripResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("orderId")
    val orderId: Long,
    
    @SerializedName("status")
    val status: String
)

/**
 * Response for arrived at drop
 */
data class ArrivedAtDropResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("orderId")
    val orderId: Long,
    
    @SerializedName("status")
    val status: String
)

/**
 * Response for end trip
 */
data class EndTripResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("orderId")
    val orderId: Long,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("completedAt")
    val completedAt: String? = null
)

// ============= Trip History Models =============

/**
 * Trip filter enum
 */
enum class TripFilter(val value: String) {
    TODAY("today"),
    THIS_WEEK("thisWeek"),
    THIS_MONTH("thisMonth")
}

/**
 * Trip list response
 */
data class TripListResponse(
    @SerializedName("content")
    val content: List<TripDetailResponse>,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("size")
    val size: Int,
    
    @SerializedName("totalElements")
    val totalElements: Int,
    
    @SerializedName("totalPages")
    val totalPages: Int
)

/**
 * Trip detail response (for both list and detail endpoints)
 */
data class TripDetailResponse(
    @SerializedName("orderId")
    val orderId: Long,
    
    @SerializedName("pickupAddress")
    val pickupAddress: String,
    
    @SerializedName("dropAddress")
    val dropAddress: String,
    
    @SerializedName("pickupLatitude")
    val pickupLatitude: Double? = null,
    
    @SerializedName("pickupLongitude")
    val pickupLongitude: Double? = null,
    
    @SerializedName("dropLatitude")
    val dropLatitude: Double? = null,
    
    @SerializedName("dropLongitude")
    val dropLongitude: Double? = null,
    
    @SerializedName("distanceKm")
    val distanceKm: Double,
    
    @SerializedName("estimatedFare")
    val estimatedFare: Double,
    
    @SerializedName("actualFare")
    val actualFare: Double? = null,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("customerName")
    val customerName: String? = null,
    
    @SerializedName("helperRequired")
    val helperRequired: Boolean = false,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("acceptedAt")
    val acceptedAt: String? = null,
    
    @SerializedName("arrivedAtPickupAt")
    val arrivedAtPickupAt: String? = null,
    
    @SerializedName("startedAt")
    val startedAt: String? = null,
    
    @SerializedName("arrivedAtDropAt")
    val arrivedAtDropAt: String? = null,
    
    @SerializedName("completedAt")
    val completedAt: String? = null,
    
    @SerializedName("rejectedAt")
    val rejectedAt: String? = null
)
