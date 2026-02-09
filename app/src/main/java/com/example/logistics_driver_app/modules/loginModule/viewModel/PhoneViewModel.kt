package com.example.logistics_driver_app.modules.loginModule.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.logistics_driver_app.data.NetworkCall.RetrofitClient
import com.example.logistics_driver_app.data.model.OTPRequest
import com.example.logistics_driver_app.modules.loginModule.base.BaseViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * PhoneViewModel - ViewModel for phone number entry.
 * Manages OTP sending logic and state.
 */
class PhoneViewModel(application: Application) : BaseViewModel(application) {
    
    private val _otpSent = MutableLiveData<Boolean>()
    val otpSent: LiveData<Boolean> = _otpSent
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val apiService = RetrofitClient.getRetrofitInstance().create(
        com.example.logistics_driver_app.data.NetworkCall.ApiService::class.java
    )
    
    /**
     * Send OTP to phone number.
     * @param countryCode Country code (e.g., "+91")
     * @param phoneNumber Phone number to send OTP to
     */
    fun sendOTP(countryCode: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _errorMessage.value = null
                
                // Make actual API call
                val request = OTPRequest(countryCode, phoneNumber)
                val response = apiService.requestOTP(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _otpSent.value = true
                } else {
                    _errorMessage.value = response.body()?.data?.message ?: "Failed to send OTP"
                    _otpSent.value = false
                }
                
            } catch (e: HttpException) {
                when (e.code()) {
                    429 -> _errorMessage.value = "You are sending requests too quickly. Please wait a moment."
                    500 -> _errorMessage.value = "Service temporarily unavailable. Please try again later."
                    else -> _errorMessage.value = "Error: ${e.message()}"
                }
                _otpSent.value = false
            } catch (e: IOException) {
                _errorMessage.value = "Network error. Please check your connection."
                _otpSent.value = false
            } catch (e: Exception) {
                handleException(e)
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _otpSent.value = false
            } finally {
                _loading.value = false
            }
        }
    }
}
