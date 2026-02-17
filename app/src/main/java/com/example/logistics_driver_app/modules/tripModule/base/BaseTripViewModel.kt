package com.example.logistics_driver_app.modules.tripModule.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.logistics_driver_app.data.model.Trip
import com.example.logistics_driver_app.data.model.TripStatus
import com.example.logistics_driver_app.modules.loginModule.base.BaseViewModel

/**
 * BaseTripViewModel - Base class for all trip-related ViewModels.
 * Provides common trip data and operations.
 */
abstract class BaseTripViewModel(application: Application) : BaseViewModel(application) {

    // Current active trip
    protected val _currentTrip = MutableLiveData<Trip?>()
    val currentTrip: LiveData<Trip?> = _currentTrip

    // Trip status updates
    protected val _tripStatus = MutableLiveData<TripStatus>()
    val tripStatus: LiveData<TripStatus> = _tripStatus

    /**
     * Load current active trip (mock data for now)
     */
    fun loadCurrentTrip() {
        launchCoroutine {
            // TODO: Replace with actual API call
            val mockTrip = Trip(
                tripId = "TRIP${System.currentTimeMillis()}",
                orderId = "ORD${(1000..9999).random()}",
                pickupAddress = "123 Main Street, Sector 15, Noida, UP 201301",
                pickupLat = 28.5355,
                pickupLng = 77.3910,
                pickupContactName = "Rahul Kumar",
                pickupContactPhone = "+91 98765 43210",
                dropAddress = "456 Market Road, Connaught Place, New Delhi, DL 110001",
                dropLat = 28.6139,
                dropLng = 77.2090,
                dropContactName = "Priya Sharma",
                dropContactPhone = "+91 98765 12345",
                distance = 15.5,
                estimatedTime = 45,
                amount = 450.0,
                paymentMode = "CASH",
                itemDescription = "Electronic items - 2 boxes"
            )
            _currentTrip.postValue(mockTrip)
            _tripStatus.postValue(mockTrip.status)
        }
    }

    /**
     * Update trip status
     */
    fun updateTripStatus(newStatus: TripStatus) {
        launchCoroutine {
            _currentTrip.value?.let { trip ->
                val updatedTrip = trip.copy(
                    status = newStatus,
                    startedAt = if (newStatus == TripStatus.STARTED_TRIP && trip.startedAt == null) 
                        System.currentTimeMillis() else trip.startedAt,
                    completedAt = if (newStatus == TripStatus.TRIP_COMPLETED) 
                        System.currentTimeMillis() else null
                )
                _currentTrip.postValue(updatedTrip)
                _tripStatus.postValue(newStatus)
            }
        }
    }

    /**
     * Cancel current trip
     */
    fun cancelTrip(reason: String) {
        launchCoroutine {
            _currentTrip.value?.let { trip ->
                val updatedTrip = trip.copy(
                    status = TripStatus.TRIP_CANCELLED,
                    cancelledAt = System.currentTimeMillis(),
                    cancellationReason = reason
                )
                _currentTrip.postValue(updatedTrip)
                _tripStatus.postValue(TripStatus.TRIP_CANCELLED)
            }
        }
    }
}
