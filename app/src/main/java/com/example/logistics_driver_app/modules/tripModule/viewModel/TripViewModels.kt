package com.example.logistics_driver_app.modules.tripModule.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.data.NetworkCall.RetrofitClient
import com.example.logistics_driver_app.data.model.DriverHomeSummaryResponse
import com.example.logistics_driver_app.modules.tripModule.base.BaseTripViewModel

/**
 * DriverHomeViewModel - ViewModel for driver home screen
 */
class DriverHomeViewModel(application: Application) : BaseTripViewModel(application) {

    private val apiService = RetrofitClient.getApiService()
    private val sharedPreference = SharedPreference.getInstance(application)

    private val _homeSummary = MutableLiveData<DriverHomeSummaryResponse?>()
    val homeSummary: LiveData<DriverHomeSummaryResponse?> = _homeSummary

    private val _canGoOnline = MutableLiveData<Boolean>()
    val canGoOnline: LiveData<Boolean> = _canGoOnline

    /**
     * Fetch driver home summary from API
     */
    fun fetchHomeSummary() {
        launchCoroutine {
            try {
                _loading.postValue(true)
                _error.postValue(null)

                val token = sharedPreference.getSessionToken()
                if (token.isEmpty()) {
                    _error.postValue("Authentication token not found. Please login again.")
                    _loading.postValue(false)
                    return@launchCoroutine
                }

                val response = apiService.getDriverHomeSummary("Bearer $token")

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val data = apiResponse.data
                        _homeSummary.postValue(data)
                        _canGoOnline.postValue(data?.canGoOnline ?: false)
                    } else {
                        _error.postValue(apiResponse?.message ?: "Failed to load home summary")
                    }
                } else {
                    _error.postValue("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Network error occurred")
            } finally {
                _loading.postValue(false)
            }
        }
    }
}

/**
 * TripActiveViewModel - ViewModel for active trip screen
 */
class TripActiveViewModel(application: Application) : BaseTripViewModel(application) {

    private val _estimatedArrival = MutableLiveData<String>()
    val estimatedArrival: LiveData<String> = _estimatedArrival

    private val _navigationStarted = MutableLiveData<Boolean>()
    val navigationStarted: LiveData<Boolean> = _navigationStarted

    init {
        loadCurrentTrip()
    }

    fun startNavigation() {
        _navigationStarted.value = true
    }

    fun updateEstimatedArrival(time: String) {
        _estimatedArrival.value = time
    }
}

/**
 * TripPickupViewModel - ViewModel for pickup screen
 */
class TripPickupViewModel(application: Application) : BaseTripViewModel(application) {

    private val _otpVerified = MutableLiveData<Boolean>()
    val otpVerified: LiveData<Boolean> = _otpVerified

    fun verifyPickupOTP(otp: String): Boolean {
        // TODO: Verify with backend
        val isValid = otp.length == 4
        _otpVerified.value = isValid
        return isValid
    }
}

/**
 * TripDropViewModel - ViewModel for drop screen
 */
class TripDropViewModel(application: Application) : BaseTripViewModel(application) {

    private val _otpVerified = MutableLiveData<Boolean>()
    val otpVerified: LiveData<Boolean> = _otpVerified

    private val _paymentCollected = MutableLiveData<Boolean>()
    val paymentCollected: LiveData<Boolean> = _paymentCollected

    fun verifyDropOTP(otp: String): Boolean {
        // TODO: Verify with backend
        val isValid = otp.length == 4
        _otpVerified.value = isValid
        return isValid
    }

    fun collectPayment(amount: Double) {
        launchCoroutine {
            // TODO: API call for payment
            _paymentCollected.value = true
        }
    }
}

/**
 * TripMenuViewModel - ViewModel for menu and settings
 */
class TripMenuViewModel(application: Application) : BaseTripViewModel(application) {

    private val _menuAction = MutableLiveData<MenuAction>()
    val menuAction: LiveData<MenuAction> = _menuAction

    fun onMenuItemClicked(action: MenuAction) {
        _menuAction.value = action
    }

    enum class MenuAction {
        PROFILE,
        EARNINGS,
        SUPPORT,
        SETTINGS,
        LOGOUT
    }
}

/**
 * CancellationViewModel - ViewModel for trip cancellation
 */
class CancellationViewModel(application: Application) : BaseTripViewModel(application) {

    private val _cancellationSuccess = MutableLiveData<Boolean>()
    val cancellationSuccess: LiveData<Boolean> = _cancellationSuccess

    fun submitCancellation(reason: String, additionalNotes: String?) {
        launchCoroutine {
            // TODO: API call for cancellation
            cancelTrip(reason)
            _cancellationSuccess.value = true
        }
    }
}

/**
 * PaymentViewModel - ViewModel for payment screens
 */
class PaymentViewModel(application: Application) : BaseTripViewModel(application) {

    private val _paymentConfirmed = MutableLiveData<Boolean>()
    val paymentConfirmed: LiveData<Boolean> = _paymentConfirmed

    fun confirmCashPayment(amount: Double) {
        launchCoroutine {
            // TODO: API call for payment confirmation
            _paymentConfirmed.value = true
        }
    }

    fun confirmOnlinePayment(transactionId: String) {
        launchCoroutine {
            // TODO: API call for online payment verification
            _paymentConfirmed.value = true
        }
    }
}
