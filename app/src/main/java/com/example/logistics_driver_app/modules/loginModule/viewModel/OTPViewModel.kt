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
    
    private val apiService = RetrofitClient.getRetrofitInstance().create(
        com.example.logistics_driver_app.data.NetworkCall.ApiService::class.java
    )
    
    private val _verificationSuccess = MutableLiveData<Boolean>()
    val verificationSuccess: LiveData<Boolean> = _verificationSuccess
    
    private val _resendSuccess = MutableLiveData<Boolean>()
    val resendSuccess: LiveData<Boolean> = _resendSuccess
    
    private val _onboardingStatus = MutableLiveData<String>()
    val onboardingStatus: LiveData<String> = _onboardingStatus
    
    private val _onboardingStep = MutableLiveData<String>()
    val onboardingStep: LiveData<String> = _onboardingStep
    
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
                
                // Make actual API call
                val request = OTPVerifyRequest(countryCode, phoneNumber, otp, languageCode)
                val response = apiService.verifyOTP(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    val token = data?.token ?: ""
                    val status = data?.onboardingStatus ?: ""
                    val step = data?.onboardingStep ?: ""
                    
                    // Save session
                    saveAuthSession(phoneNumber, token)
                    
                    // Update onboarding state
                    _onboardingStatus.value = status
                    _onboardingStep.value = step
                    
                    _verificationSuccess.value = true
                } else {
                    _errorMessage.value = response.body()?.data?.message ?: "Invalid OTP"
                    _verificationSuccess.value = false
                }
                
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> _errorMessage.value = "Invalid OTP. Please try again."
                    429 -> _errorMessage.value = "Too many attempts. Please wait a moment."
                    500 -> _errorMessage.value = "Service temporarily unavailable. Please try again later."
                    else -> _errorMessage.value = "Error: ${e.message()}"
                }
                _verificationSuccess.value = false
            } catch (e: IOException) {
                _errorMessage.value = "Network error. Please check your connection."
                _verificationSuccess.value = false
            } catch (e: Exception) {
                handleException(e)
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _verificationSuccess.value = false
            } finally {
                _loading.value = false
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
                    _errorMessage.value = response.body()?.data?.message ?: "Failed to resend OTP"
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
     */
    private suspend fun saveAuthSession(phoneNumber: String, token: String) {
        val expiryTime = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L) // 30 days
        
        // Save to database
        val authSession = AuthSession(
            phoneNumber = phoneNumber,
            sessionToken = token,
            isVerified = true,
            expiryTimestamp = expiryTime
        )
        authDao.insertAuthSession(authSession)
        
        // Save to preferences
        sharedPreference.setPhoneNumber(phoneNumber)
        sharedPreference.setSessionToken(token)
        sharedPreference.setLoggedIn(true)
    }
}
