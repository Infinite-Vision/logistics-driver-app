package com.example.logistics_driver_app.modules.tripModule.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.data.NetworkCall.RetrofitClient
import com.example.logistics_driver_app.data.model.*
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

    private val _statusVerifying = MutableLiveData<Boolean>()
    val statusVerifying: LiveData<Boolean> = _statusVerifying

    private val _verifiedDriverStatus = MutableLiveData<String?>()
    val verifiedDriverStatus: LiveData<String?> = _verifiedDriverStatus

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

    /**
     * Verify driver status after going online/offline
     * Polls the API until backend status matches expected status
     * @param expectedStatus "ONLINE" or "OFFLINE"
     * @param maxAttempts Maximum number of polling attempts (default: 10)
     * @param delayMs Delay between polling attempts in milliseconds (default: 1000ms)
     */
    fun verifyDriverStatus(expectedStatus: String, maxAttempts: Int = 10, delayMs: Long = 1000L) {
        launchCoroutine {
            try {
                _statusVerifying.postValue(true)
                _verifiedDriverStatus.postValue(null)
                
                val token = sharedPreference.getSessionToken()
                if (token.isEmpty()) {
                    _statusVerifying.postValue(false)
                    return@launchCoroutine
                }

                var attempts = 0
                var statusMatched = false

                while (attempts < maxAttempts && !statusMatched) {
                    attempts++
                    android.util.Log.d("DriverHomeViewModel", "[STATUS_VERIFY] Attempt $attempts/$maxAttempts - checking for $expectedStatus")

                    try {
                        val response = apiService.getDriverHomeSummary("Bearer $token")

                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.success == true) {
                                val data = apiResponse.data
                                val currentStatus = data?.driverStatus
                                
                                android.util.Log.d("DriverHomeViewModel", "[STATUS_VERIFY] Backend status: $currentStatus, Expected: $expectedStatus")

                                if (currentStatus == expectedStatus) {
                                    // Status matched!
                                    statusMatched = true
                                    _verifiedDriverStatus.postValue(currentStatus)
                                    _homeSummary.postValue(data)
                                    _canGoOnline.postValue(data?.canGoOnline ?: false)
                                    android.util.Log.i("DriverHomeViewModel", "[STATUS_VERIFY] ✓ Status verified: $currentStatus")
                                } else {
                                    // Status doesn't match yet, wait and retry
                                    if (attempts < maxAttempts) {
                                        android.util.Log.d("DriverHomeViewModel", "[STATUS_VERIFY] Status not matched yet, retrying in ${delayMs}ms...")
                                        kotlinx.coroutines.delay(delayMs)
                                    }
                                }
                            } else {
                                android.util.Log.w("DriverHomeViewModel", "[STATUS_VERIFY] API error: ${apiResponse?.message}")
                                break
                            }
                        } else {
                            android.util.Log.e("DriverHomeViewModel", "[STATUS_VERIFY] HTTP error: ${response.code()}")
                            break
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DriverHomeViewModel", "[STATUS_VERIFY] Error: ${e.message}", e)
                        break
                    }
                }

                if (!statusMatched) {
                    android.util.Log.w("DriverHomeViewModel", "[STATUS_VERIFY] ✗ Status verification failed after $attempts attempts")
                    // Still update with latest data even if not matched
                    fetchHomeSummary()
                }

            } finally {
                _statusVerifying.postValue(false)
            }
        }
    }
    
    /**
     * Accept an order
     * @param orderId Order ID to accept
     * @param onSuccess Callback when order is successfully accepted
     * @param onError Callback when error occurs
     */
    fun acceptOrder(orderId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        launchCoroutine {
            try {
                android.util.Log.i("DriverHomeViewModel", "[API_CALL] ========================================")
                android.util.Log.i("DriverHomeViewModel", "[API_CALL] Accepting order #$orderId")
                android.util.Log.i("DriverHomeViewModel", "[API_CALL] ========================================")
                
                _loading.postValue(true)
                
                val token = sharedPreference.getSessionToken()
                if (token.isEmpty()) {
                    val errorMsg = "Authentication token not found"
                    android.util.Log.e("DriverHomeViewModel", "[API_ERROR] $errorMsg")
                    onError(errorMsg)
                    return@launchCoroutine
                }
                
                android.util.Log.d("DriverHomeViewModel", "[API_REQUEST] POST /api/v1/driver/orders/$orderId/accept")
                android.util.Log.d("DriverHomeViewModel", "[API_REQUEST] Authorization: Bearer ${token.take(20)}...")
                
                val response = apiService.acceptOrder("Bearer $token", orderId)
                
                android.util.Log.d("DriverHomeViewModel", "[API_RESPONSE] HTTP Status: ${response.code()}")
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    android.util.Log.d("DriverHomeViewModel", "[API_RESPONSE] Body: $apiResponse")
                    
                    if (apiResponse?.success == true) {
                        val data = apiResponse.data
                        android.util.Log.i("DriverHomeViewModel", "[API_SUCCESS] ✓ Order #$orderId accepted")
                        android.util.Log.d("DriverHomeViewModel", "[API_SUCCESS] Message: ${data?.message}")
                        android.util.Log.d("DriverHomeViewModel", "[API_SUCCESS] Status: ${data?.status}")
                        android.util.Log.d("DriverHomeViewModel", "[API_SUCCESS] Order ID: ${data?.orderId}")
                        onSuccess()
                    } else {
                        val errorMsg = apiResponse?.message ?: "Failed to accept order"
                        android.util.Log.e("DriverHomeViewModel", "[API_ERROR] $errorMsg")
                        onError(errorMsg)
                    }
                } else {
                    val errorMsg = "Server error: ${response.code()} - ${response.message()}"
                    android.util.Log.e("DriverHomeViewModel", "[API_ERROR] $errorMsg")
                    try {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("DriverHomeViewModel", "[API_ERROR] Error body: $errorBody")
                    } catch (e: Exception) {
                        android.util.Log.e("DriverHomeViewModel", "[API_ERROR] Could not read error body")
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Network error occurred"
                android.util.Log.e("DriverHomeViewModel", "[API_EXCEPTION] $errorMsg", e)
                onError(errorMsg)
            } finally {
                _loading.postValue(false)
            }
        }
    }
    
    /**
     * Reject an order
     * @param orderId Order ID to reject
     * @param onSuccess Callback when order is successfully rejected
     * @param onError Callback when error occurs
     */
    fun rejectOrder(orderId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        launchCoroutine {
            try {
                android.util.Log.i("DriverHomeViewModel", "[API_CALL] ========================================")
                android.util.Log.i("DriverHomeViewModel", "[API_CALL] Rejecting order #$orderId")
                android.util.Log.i("DriverHomeViewModel", "[API_CALL] ========================================")
                
                _loading.postValue(true)
                
                val token = sharedPreference.getSessionToken()
                if (token.isEmpty()) {
                    val errorMsg = "Authentication token not found"
                    android.util.Log.e("DriverHomeViewModel", "[API_ERROR] $errorMsg")
                    onError(errorMsg)
                    return@launchCoroutine
                }
                
                android.util.Log.d("DriverHomeViewModel", "[API_REQUEST] POST /api/v1/driver/orders/$orderId/reject")
                android.util.Log.d("DriverHomeViewModel", "[API_REQUEST] Authorization: Bearer ${token.take(20)}...")
                
                val response = apiService.rejectOrder("Bearer $token", orderId)
                
                android.util.Log.d("DriverHomeViewModel", "[API_RESPONSE] HTTP Status: ${response.code()}")
                
                if (response.isSuccessful) {
                    android.util.Log.i("DriverHomeViewModel", "[API_SUCCESS] ✓ Order #$orderId rejected (204 No Content)")
                    onSuccess()
                } else {
                    val errorMsg = "Server error: ${response.code()} - ${response.message()}"
                    android.util.Log.e("DriverHomeViewModel", "[API_ERROR] $errorMsg")
                    try {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("DriverHomeViewModel", "[API_ERROR] Error body: $errorBody")
                    } catch (e: Exception) {
                        android.util.Log.e("DriverHomeViewModel", "[API_ERROR] Could not read error body")
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Network error occurred"
                android.util.Log.e("DriverHomeViewModel", "[API_EXCEPTION] $errorMsg", e)
                onError(errorMsg)
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

    private val apiService = RetrofitClient.getApiService()
    private val sharedPreference = SharedPreference.getInstance(application)

    private val _menuAction = MutableLiveData<MenuAction>()
    val menuAction: LiveData<MenuAction> = _menuAction

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    private val _logoutError = MutableLiveData<String>()
    val logoutError: LiveData<String> = _logoutError

    fun onMenuItemClicked(action: MenuAction) {
        _menuAction.value = action
    }

    /**
     * Logout user by calling logout API
     */
    fun logout() {
        launchCoroutine {
            try {
                val token = sharedPreference.getSessionToken()
                Log.d("TripMenuViewModel", "[LOGOUT_API] Calling logout API...")
                Log.d("TripMenuViewModel", "[LOGOUT_API] Token: Bearer ${token.take(20)}...")

                val response = apiService.logout("Bearer $token")

                Log.d("TripMenuViewModel", "[LOGOUT_API] Response code: ${response.code()}")
                Log.d("TripMenuViewModel", "[LOGOUT_API] Response message: ${response.message()}")

                if (response.isSuccessful) {
                    val logoutResponse = response.body()
                    Log.d("TripMenuViewModel", "[LOGOUT_SUCCESS] Logout successful")
                    Log.d("TripMenuViewModel", "[LOGOUT_SUCCESS] Response: $logoutResponse")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.w("TripMenuViewModel", "[LOGOUT_WARNING] API error: ${response.code()} - ${response.message()}")
                    Log.w("TripMenuViewModel", "[LOGOUT_WARNING] Error body: $errorBody")
                    Log.w("TripMenuViewModel", "[LOGOUT_WARNING] Proceeding with local logout anyway")
                }
            } catch (e: Exception) {
                Log.w("TripMenuViewModel", "[LOGOUT_WARNING] Exception during logout API call", e)
                Log.w("TripMenuViewModel", "[LOGOUT_WARNING] Exception: ${e.javaClass.simpleName}: ${e.message}")
                Log.w("TripMenuViewModel", "[LOGOUT_WARNING] Proceeding with local logout anyway")
            } finally {
                // Always clear auth data locally, even if API call fails
                Log.d("TripMenuViewModel", "[LOGOUT] Clearing local session data...")
                Log.d("TripMenuViewModel", "[LOGOUT] Before clear - isLoggedIn: ${sharedPreference.isLoggedIn()}")
                Log.d("TripMenuViewModel", "[LOGOUT] Before clear - hasToken: ${!sharedPreference.getSessionToken().isNullOrEmpty()}")
                Log.d("TripMenuViewModel", "[LOGOUT] Before clear - hasPhone: ${!sharedPreference.getPhoneNumber().isNullOrEmpty()}")
                
                sharedPreference.clearAuthData()
                
                Log.d("TripMenuViewModel", "[LOGOUT] After clear - isLoggedIn: ${sharedPreference.isLoggedIn()}")
                Log.d("TripMenuViewModel", "[LOGOUT] After clear - hasToken: ${!sharedPreference.getSessionToken().isNullOrEmpty()}")
                Log.d("TripMenuViewModel", "[LOGOUT] After clear - hasPhone: ${!sharedPreference.getPhoneNumber().isNullOrEmpty()}")
                Log.d("TripMenuViewModel", "[LOGOUT_SUCCESS] Local session cleared - user logged out")
                
                _logoutSuccess.value = true
            }
        }
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

// =======================================================================
// TripFlowViewModel — single ViewModel for the entire active trip flow.
// Holds the real API calls for arrived-pickup, start-trip, arrived-drop,
// end-trip; scoped to the Activity so all trip fragments share state.
// =======================================================================
class TripFlowViewModel(application: Application) : BaseTripViewModel(application) {

    private val TAG = "TripFlowViewModel"
    private val apiService = RetrofitClient.getApiService()
    private val sharedPreference = SharedPreference.getInstance(application)

    // Shared trip state --------------------------------------------------
    private val _orderId = MutableLiveData<Long>()
    val orderId: LiveData<Long> = _orderId

    /** Entire API response from the latest trip step – used to show details */
    private val _tripDetail = MutableLiveData<TripDetailResponse?>()
    val tripDetail: LiveData<TripDetailResponse?> = _tripDetail

    // Step results -------------------------------------------------------
    sealed class StepResult {
        object Loading : StepResult()
        data class Success(val message: String) : StepResult()
        data class Error(val message: String) : StepResult()
    }

    private val _arrivedPickupResult = MutableLiveData<StepResult>()
    val arrivedPickupResult: LiveData<StepResult> = _arrivedPickupResult

    private val _startTripResult = MutableLiveData<StepResult>()
    val startTripResult: LiveData<StepResult> = _startTripResult

    private val _arrivedDropResult = MutableLiveData<StepResult>()
    val arrivedDropResult: LiveData<StepResult> = _arrivedDropResult

    private val _endTripResult = MutableLiveData<StepResult>()
    val endTripResult: LiveData<StepResult> = _endTripResult

    // ---- helpers -------------------------------------------------------

    fun setOrderId(id: Long) {
        _orderId.value = id
        sharedPreference.saveOrderId(id)
    }

    /**
     * Reset all step LiveData to null before starting a new trip.
     * Must be called when a new order is accepted so old Success/Error values
     * are not re-delivered to fresh fragment observers (which would skip the flow).
     */
    fun resetForNewTrip() {
        _arrivedPickupResult.value = null
        _startTripResult.value = null
        _arrivedDropResult.value = null
        _endTripResult.value = null
        _tripDetail.value = null
        Log.d(TAG, "[RESET] All step results cleared for new trip")
    }

    fun getStoredOrderId(): Long = sharedPreference.getOrderId()

    fun restoreOrderId() {
        val id = sharedPreference.getOrderId()
        if (id != -1L) _orderId.value = id
    }

    private fun token(): String = sharedPreference.getSessionToken() ?: ""

    private fun currentOrderId(): Long = _orderId.value ?: sharedPreference.getOrderId()

    // ---- arrived at pickup --------------------------------------------

    fun arrivedAtPickup(lat: Double, lng: Double) {
        val orderId = currentOrderId()
        if (orderId == -1L) { _arrivedPickupResult.value = StepResult.Error("No active order"); return }
        _arrivedPickupResult.value = StepResult.Loading
        launchCoroutine {
            try {
                Log.d(TAG, "[arrivedAtPickup] POST /driver/orders/$orderId/arrived-pickup lat=$lat lng=$lng")
                val response = apiService.arrivedAtPickup(
                    "Bearer ${token()}", orderId,
                    ArrivedAtPickupRequest(lat, lng)
                )
                Log.d(TAG, "[arrivedAtPickup] HTTP ${response.code()}")
                if (response.isSuccessful && response.body()?.success == true) {
                    val msg = response.body()?.data?.message ?: "Arrived at pickup"
                    Log.i(TAG, "[arrivedAtPickup] ✓ $msg")
                    _arrivedPickupResult.postValue(StepResult.Success(msg))
                } else {
                    val err = response.body()?.message ?: "Failed (${response.code()})"
                    Log.e(TAG, "[arrivedAtPickup] ✗ $err")
                    _arrivedPickupResult.postValue(StepResult.Error(err))
                }
            } catch (e: Exception) {
                Log.e(TAG, "[arrivedAtPickup] exception: ${e.message}", e)
                _arrivedPickupResult.postValue(StepResult.Error(e.message ?: "Network error"))
            }
        }
    }

    // ---- start trip (OTP confirm) -------------------------------------

    fun startTrip(otp: String, lat: Double, lng: Double) {
        val orderId = currentOrderId()
        if (orderId == -1L) { _startTripResult.value = StepResult.Error("No active order"); return }
        _startTripResult.value = StepResult.Loading
        launchCoroutine {
            try {
                Log.d(TAG, "[startTrip] POST /driver/orders/$orderId/start-trip/confirm otp=$otp")
                val response = apiService.startTrip(
                    "Bearer ${token()}", orderId,
                    StartTripRequest(otp, lat, lng)
                )
                Log.d(TAG, "[startTrip] HTTP ${response.code()}")
                if (response.isSuccessful && response.body()?.success == true) {
                    val msg = response.body()?.data?.message ?: "Trip started"
                    Log.i(TAG, "[startTrip] ✓ $msg")
                    _startTripResult.postValue(StepResult.Success(msg))
                } else {
                    val err = response.body()?.message ?: "Failed (${response.code()})"
                    Log.e(TAG, "[startTrip] ✗ $err")
                    _startTripResult.postValue(StepResult.Error(err))
                }
            } catch (e: Exception) {
                Log.e(TAG, "[startTrip] exception: ${e.message}", e)
                _startTripResult.postValue(StepResult.Error(e.message ?: "Network error"))
            }
        }
    }

    // ---- arrived at drop ----------------------------------------------

    fun arrivedAtDrop(lat: Double, lng: Double) {
        val orderId = currentOrderId()
        if (orderId == -1L) { _arrivedDropResult.value = StepResult.Error("No active order"); return }
        _arrivedDropResult.value = StepResult.Loading
        launchCoroutine {
            try {
                Log.d(TAG, "[arrivedAtDrop] POST /driver/orders/$orderId/arrived-drop lat=$lat lng=$lng")
                val response = apiService.arrivedAtDrop(
                    "Bearer ${token()}", orderId,
                    ArrivedAtDropRequest(lat, lng)
                )
                Log.d(TAG, "[arrivedAtDrop] HTTP ${response.code()}")
                if (response.isSuccessful && response.body()?.success == true) {
                    val msg = response.body()?.data?.message ?: "Arrived at drop"
                    Log.i(TAG, "[arrivedAtDrop] ✓ $msg")
                    _arrivedDropResult.postValue(StepResult.Success(msg))
                } else {
                    val err = response.body()?.message ?: "Failed (${response.code()})"
                    Log.e(TAG, "[arrivedAtDrop] ✗ $err")
                    _arrivedDropResult.postValue(StepResult.Error(err))
                }
            } catch (e: Exception) {
                Log.e(TAG, "[arrivedAtDrop] exception: ${e.message}", e)
                _arrivedDropResult.postValue(StepResult.Error(e.message ?: "Network error"))
            }
        }
    }

    // ---- end trip -----------------------------------------------------

    fun endTrip(lat: Double, lng: Double) {
        val orderId = currentOrderId()
        if (orderId == -1L) { _endTripResult.value = StepResult.Error("No active order"); return }
        _endTripResult.value = StepResult.Loading
        launchCoroutine {
            try {
                Log.d(TAG, "[endTrip] POST /driver/orders/$orderId/end-trip lat=$lat lng=$lng")
                val response = apiService.endTrip(
                    "Bearer ${token()}", orderId,
                    EndTripRequest(lat, lng)
                )
                Log.d(TAG, "[endTrip] HTTP ${response.code()}")
                if (response.isSuccessful && response.body()?.success == true) {
                    val msg = response.body()?.data?.message ?: "Trip ended"
                    Log.i(TAG, "[endTrip] ✓ $msg")
                    sharedPreference.clearOrderId()
                    _endTripResult.postValue(StepResult.Success(msg))
                } else {
                    val err = response.body()?.message ?: "Failed (${response.code()})"
                    Log.e(TAG, "[endTrip] ✗ $err")
                    _endTripResult.postValue(StepResult.Error(err))
                }
            } catch (e: Exception) {
                Log.e(TAG, "[endTrip] exception: ${e.message}", e)
                _endTripResult.postValue(StepResult.Error(e.message ?: "Network error"))
            }
        }
    }
}
