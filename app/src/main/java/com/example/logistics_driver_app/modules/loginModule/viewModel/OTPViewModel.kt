package com.example.logistics_driver_app.modules.loginModule.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.RoomDB.AppDatabase
import com.example.logistics_driver_app.data.model.AuthSession
import com.example.logistics_driver_app.data.NetworkCall.RetrofitClient
import com.example.logistics_driver_app.data.model.OTPRequest
import com.example.logistics_driver_app.data.model.OTPVerifyRequest
import com.example.logistics_driver_app.data.model.AppStateResponse
import com.example.logistics_driver_app.modules.loginModule.base.BaseViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * OTPViewModel - ViewModel for OTP verification.
 * Manages OTP verification and resend logic.
 */
class OTPViewModel(application: Application) : BaseViewModel(application) {
    
    private val authDao = AppDatabase.getDatabase(application).authDao()
    private val sharedPreference = SharedPreference.getInstance(application)
    
    init {
        // Initialize RetrofitClient with context
        RetrofitClient.initialize(application)
    }
    
    private val apiService = RetrofitClient.getApiService()
    
    private val _verificationSuccess = MutableLiveData<Boolean>()
    val verificationSuccess: LiveData<Boolean> = _verificationSuccess
    
    private val _resendSuccess = MutableLiveData<Boolean>()
    val resendSuccess: LiveData<Boolean> = _resendSuccess
    
    private val _onboardingStatus = MutableLiveData<String>()
    val onboardingStatus: LiveData<String> = _onboardingStatus
    
    private val _onboardingStep = MutableLiveData<String>()
    val onboardingStep: LiveData<String> = _onboardingStep
    
    private val _nextScreen = MutableLiveData<String>()
    val nextScreen: LiveData<String> = _nextScreen
    
    private val _appStateData = MutableLiveData<AppStateResponse>()
    val appStateData: LiveData<AppStateResponse> = _appStateData
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    /**
     * Verify OTP entered by user.
     * @param countryCode Country code (e.g., "+91")
     * @param phoneNumber Phone number
     * @param otp OTP code to verify
     * @param languageCode Preferred language code (e.g., "en", "hi", "ta")
     */
    fun verifyOTP(countryCode: String, phoneNumber: String, otp: String, languageCode: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _errorMessage.value = null
                
                android.util.Log.d("OTPViewModel", "[API] verifyOTP() called")
                android.util.Log.d("OTPViewModel", "[API] Parameters - countryCode: $countryCode")
                android.util.Log.d("OTPViewModel", "[API] Parameters - phoneNumber: $phoneNumber")
                android.util.Log.d("OTPViewModel", "[API] Parameters - otp: ****") // Hide OTP in logs
                android.util.Log.d("OTPViewModel", "[API] Parameters - languageCode: $languageCode")
                
                // Make actual API call
                val request = OTPVerifyRequest(countryCode, phoneNumber, otp, languageCode)
                android.util.Log.d("OTPViewModel", "[API] Request body created")
                android.util.Log.d("OTPViewModel", "[API] Calling API endpoint: POST /api/v1/auth/otp/verify")
                
                val response = apiService.verifyOTP(request)
                
                android.util.Log.d("OTPViewModel", "[API] Response received")
                android.util.Log.d("OTPViewModel", "[API] Response code: ${response.code()}")
                android.util.Log.d("OTPViewModel", "[API] Response successful: ${response.isSuccessful}")
                android.util.Log.d("OTPViewModel", "[API] Response body success: ${response.body()?.success}")
                android.util.Log.d("OTPViewModel", "[API] Response message: ${response.body()?.message}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    val token = data?.token ?: ""
                    val status = data?.onboardingStatus ?: ""
                    val step = data?.onboardingStep ?: ""
                    
                    android.util.Log.d("OTPViewModel", "[API] Token received: ${if (token.isNotEmpty()) "${token.take(10)}..." else "EMPTY"}")
                    android.util.Log.d("OTPViewModel", "[API] Onboarding status: $status")
                    android.util.Log.d("OTPViewModel", "[API] Onboarding step: $step")
                    
                    // Save session with language code
                    saveAuthSession(phoneNumber, token, languageCode)
                    
                    // Update onboarding state
                    _onboardingStatus.value = status
                    _onboardingStep.value = step
                    
                    // Call app state API to get proper routing information
                    android.util.Log.d("OTPViewModel", "[API] ===== OTP VERIFIED SUCCESSFULLY =====")
                    android.util.Log.d("OTPViewModel", "[API] Token saved to SharedPreference")
                    android.util.Log.d("OTPViewModel", "[API] Now calling fetchAppState() to get nextScreen...")
                    
                    // IMPORTANT: Call fetchAppState() and WAIT for it to complete before setting verificationSuccess
                    fetchAppState()
                    android.util.Log.d("OTPViewModel", "[API] fetchAppState() completed")
                    
                    _verificationSuccess.value = true
                    android.util.Log.d("OTPViewModel", "[API] verificationSuccess LiveData set to TRUE")
                } else {
                    val errorMsg = response.body()?.message 
                        ?: response.body()?.data?.message 
                        ?: "Invalid OTP"
                    android.util.Log.e("OTPViewModel", "[API] Verification failed: $errorMsg")
                    _errorMessage.value = errorMsg
                    _verificationSuccess.value = false
                }
                
            } catch (e: HttpException) {
                android.util.Log.e("OTPViewModel", "[API] HttpException in verifyOTP", e)
                android.util.Log.e("OTPViewModel", "[API] HTTP Code: ${e.code()}")
                android.util.Log.e("OTPViewModel", "[API] HTTP Message: ${e.message()}")
                when (e.code()) {
                    400 -> _errorMessage.value = "Invalid OTP. Please try again."
                    429 -> _errorMessage.value = "Too many attempts. Please wait a moment."
                    500 -> _errorMessage.value = "Service temporarily unavailable. Please try again later."
                    else -> _errorMessage.value = "Error ${e.code()}: ${e.message()}"
                }
                _verificationSuccess.value = false
            } catch (e: IOException) {
                android.util.Log.e("OTPViewModel", "[API] IOException in verifyOTP", e)
                _errorMessage.value = "Network error: ${e.message ?: "Cannot connect to server. Please check your internet connection."}"
                _verificationSuccess.value = false
            } catch (e: Exception) {
                android.util.Log.e("OTPViewModel", "[API] Exception in verifyOTP", e)
                handleException(e)
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _verificationSuccess.value = false
            } finally {
                _loading.value = false
                android.util.Log.d("OTPViewModel", "[API] verifyOTP() completed")
            }
        }
    }
    
    /**
     * Resend OTP to phone number.
     * @param countryCode Country code (e.g., "+91")
     * @param phoneNumber Phone number to resend OTP
     */
    fun resendOTP(countryCode: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _errorMessage.value = null
                
                // Make actual API call
                val request = OTPRequest(countryCode, phoneNumber)
                val response = apiService.requestOTP(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _resendSuccess.value = true
                } else {
                    _errorMessage.value = response.body()?.message 
                        ?: response.body()?.data?.message 
                        ?: "Failed to resend OTP"
                    _resendSuccess.value = false
                }
                
            } catch (e: HttpException) {
                when (e.code()) {
                    429 -> _errorMessage.value = "You are sending requests too quickly. Please wait a moment."
                    500 -> _errorMessage.value = "Service temporarily unavailable. Please try again later."
                    else -> _errorMessage.value = "Error: ${e.message()}"
                }
                _resendSuccess.value = false
            } catch (e: IOException) {
                _errorMessage.value = "Network error. Please check your connection."
                _resendSuccess.value = false
            } catch (e: Exception) {
                handleException(e)
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _resendSuccess.value = false
            } finally {
                _loading.value = false
            }
        }
    }
    
    /**
     * Save authentication session to database and preferences.
     * @param phoneNumber Phone number
     * @param token Session token
     * @param languageCode User's preferred language code
     */
    private suspend fun saveAuthSession(phoneNumber: String, token: String, languageCode: String) {
        android.util.Log.d("OTPViewModel", "[PERSIST] ===== SAVING SESSION DATA =====")
        android.util.Log.d("OTPViewModel", "[PERSIST] Phone: $phoneNumber")
        android.util.Log.d("OTPViewModel", "[PERSIST] Token: ${token.take(15)}...")
        android.util.Log.d("OTPViewModel", "[PERSIST] Language: $languageCode")
        
        val expiryTime = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L) // 30 days
        
        // Save to database
        val authSession = AuthSession(
            phoneNumber = phoneNumber,
            sessionToken = token,
            isVerified = true,
            expiryTimestamp = expiryTime
        )
        authDao.insertAuthSession(authSession)
        android.util.Log.d("OTPViewModel", "[PERSIST] ✓ Saved to Room Database")
        
        // Save to SharedPreferences
        sharedPreference.setPhoneNumber(phoneNumber)
        sharedPreference.setSessionToken(token)
        sharedPreference.setLoggedIn(true)
        sharedPreference.setLanguage(languageCode.lowercase()) // Save selected language
        android.util.Log.d("OTPViewModel", "[PERSIST] ✓ Saved to SharedPreferences")
        
        // Verify data was saved correctly
        android.util.Log.d("OTPViewModel", "[PERSIST] ===== VERIFYING DATA PERSISTENCE =====")
        android.util.Log.d("OTPViewModel", "[PERSIST] Retrieved Phone: ${sharedPreference.getPhoneNumber()}")
        android.util.Log.d("OTPViewModel", "[PERSIST] Retrieved Token: ${sharedPreference.getSessionToken()?.take(15)}...")
        android.util.Log.d("OTPViewModel", "[PERSIST] Retrieved Language: ${sharedPreference.getLanguage()}")
        android.util.Log.d("OTPViewModel", "[PERSIST] Is Logged In: ${sharedPreference.isLoggedIn()}")
        android.util.Log.d("OTPViewModel", "[PERSIST] ===== DATA SUCCESSFULLY PERSISTED =====")
    }
    
    /**
     * Fetch app state to determine next screen for navigation.
     * Called automatically after successful OTP verification.
     * This is a suspend function that blocks until the API call completes.
     */
    private suspend fun fetchAppState() {
        try {
            android.util.Log.d("OTPViewModel", "[APP_STATE] ========== fetchAppState() CALLED ==========")
            android.util.Log.d("OTPViewModel", "[APP_STATE] Fetching app state...")
            
            val token = "Bearer ${sharedPreference.getSessionToken()}"
            android.util.Log.d("OTPViewModel", "[APP_STATE] Token length: ${token.length}, isEmpty: ${token.isEmpty()}") 
            
            if (token == "Bearer " || token.isEmpty()) {
                android.util.Log.e("OTPViewModel", "[APP_STATE] ERROR: Token is empty or invalid!")
                android.util.Log.e("OTPViewModel", "[APP_STATE] Setting default nextScreen to OWNER_DETAILS")
                _nextScreen.value = "OWNER_DETAILS"
                return
            }
            
            android.util.Log.d("OTPViewModel", "[APP_STATE] Calling API endpoint: GET /api/v1/app/state")
            val response = apiService.getAppState(token)
            
            android.util.Log.d("OTPViewModel", "[APP_STATE] Response received")
            android.util.Log.d("OTPViewModel", "[APP_STATE] Response code: ${response.code()}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        android.util.Log.d("OTPViewModel", "[APP_STATE] Success - nextScreen: ${data.nextScreen}")
                        android.util.Log.d("OTPViewModel", "[APP_STATE] onboardingStatus: ${data.onboardingStatus}")
                        android.util.Log.d("OTPViewModel", "[APP_STATE] driverStatus: ${data.driverStatus}")
                        
                        _appStateData.value = data
                        
                        // Update next screen for navigation
                        val nextScreenValue = data.nextScreen
                        android.util.Log.d("OTPViewModel", "[APP_STATE] ===== SETTING NEXT SCREEN =====")
                        if (!nextScreenValue.isNullOrEmpty()) {
                            android.util.Log.d("OTPViewModel", "[APP_STATE] NextScreen value from API: '$nextScreenValue'")
                            _nextScreen.value = nextScreenValue
                            android.util.Log.d("OTPViewModel", "[APP_STATE] _nextScreen LiveData updated to: '$nextScreenValue'")
                            android.util.Log.d("OTPViewModel", "[APP_STATE] Fragment observers should now trigger")
                        } else {
                            // API did not return nextScreen — derive from onboardingStatus
                            val derivedScreen = when (data.onboardingStatus?.uppercase()) {
                                "APPROVED" -> "HOME"
                                "PENDING" -> "VERIFICATION_IN_PROGRESS"
                                "IN_PROGRESS" -> when (data.driverStatus?.uppercase()) {
                                    "ACTIVE", "ONLINE", "OFFLINE", "ON_TRIP" -> "HOME"
                                    else -> "OWNER_DETAILS"
                                }
                                else -> "OWNER_DETAILS"
                            }
                            android.util.Log.w("OTPViewModel", "[APP_STATE] NextScreen null/empty — derived from onboardingStatus (${data.onboardingStatus}): $derivedScreen")
                            _nextScreen.value = derivedScreen
                        }
                        
                        // Update preferred language if provided by API (overrides user selection)
                        val language = data.preferredLanguage
                        if (!language.isNullOrEmpty()) {
                            val currentLanguage = sharedPreference.getLanguage()
                            if (currentLanguage != language.lowercase()) {
                                sharedPreference.setLanguage(language.lowercase())
                                android.util.Log.d("OTPViewModel", "[APP_STATE] Language updated from '$currentLanguage' to '${language.lowercase()}'")
                            } else {
                                android.util.Log.d("OTPViewModel", "[APP_STATE] Language unchanged: $language")
                            }
                        } else {
                            android.util.Log.d("OTPViewModel", "[APP_STATE] No language preference from API, keeping current: ${sharedPreference.getLanguage()}")
                        }
                    } else {
                        android.util.Log.w("OTPViewModel", "[APP_STATE] Response data is NULL")
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to fetch app state"
                    android.util.Log.e("OTPViewModel", "[APP_STATE] Failed: $errorMsg")
                }
                
            } catch (e: HttpException) {
                android.util.Log.e("OTPViewModel", "[APP_STATE] HTTP Error: ${e.code()}", e)
            } catch (e: IOException) {
                android.util.Log.e("OTPViewModel", "[APP_STATE] Network Error", e)
            } catch (e: Exception) {
                android.util.Log.e("OTPViewModel", "[APP_STATE] Unexpected Error", e)
            }
    }
}
