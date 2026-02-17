package com.example.logistics_driver_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Trip status enum
 */
enum class TripStatus {
    TRIP_ASSIGNED,
    HEADING_TO_PICKUP,
    ARRIVED_AT_PICKUP,
    STARTED_TRIP,
    HEADING_TO_DROP,
    ARRIVED_AT_DROP,
    TRIP_COMPLETED,
    TRIP_CANCELLED
}

/**
 * Payment status enum
 */
enum class PaymentStatus {
    PENDING,
    CASH_COLLECTED,
    ONLINE_PAID,
    FAILED
}

/**
 * Trip data model
 */
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey
    val tripId: String,
    val orderId: String,
    val pickupAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val pickupContactName: String,
    val pickupContactPhone: String,
    val dropAddress: String,
    val dropLat: Double,
    val dropLng: Double,
    val dropContactName: String,
    val dropContactPhone: String,
    val distance: Double, // in km
    val estimatedTime: Int, // in minutes
    val amount: Double,
    val paymentMode: String, // "CASH" or "ONLINE"
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val status: TripStatus = TripStatus.TRIP_ASSIGNED,
    val notes: String? = null,
    val itemDescription: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val cancelledAt: Long? = null,
    val cancellationReason: String? = null
)

/**
 * Location point for tracking
 */
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Trip statistics for display
 */
data class TripStats(
    val totalTrips: Int = 0,
    val completedTrips: Int = 0,
    val totalEarnings: Double = 0.0,
    val todayTrips: Int = 0,
    val todayEarnings: Double = 0.0
)

/**
 * Cancellation reason
 */
data class CancellationReason(
    val id: String,
    val reason: String
)

/**
 * Common cancellation reasons
 */
object CancellationReasons {
    val reasons = listOf(
        CancellationReason("1", "Customer not available"),
        CancellationReason("2", "Wrong address"),
        CancellationReason("3", "Vehicle breakdown"),
        CancellationReason("4", "Customer cancelled"),
        CancellationReason("5", "Traffic issue"),
        CancellationReason("6", "Other")
    )
}
