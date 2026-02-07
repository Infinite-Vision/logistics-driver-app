package com.example.logistics_driver_app.ui.auth.phone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.logistics_driver_app.utils.CommonUtils
import com.example.logistics_driver_app.utils.ValidationUtils

/**
 * ViewModel for Phone Entry screen.
 * Handles phone number validation and OTP generation.
 */
class PhoneViewModel : ViewModel() {

    private val _phoneNumber = MutableLiveData<String>()
    val phoneNumber: LiveData<String> = _phoneNumber

    private val _isPhoneValid = MutableLiveData<Boolean>()
    val isPhoneValid: LiveData<Boolean> = _isPhoneValid

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _otpSent = MutableLiveData<Boolean>()
    val otpSent: LiveData<Boolean> = _otpSent

    private val _generatedOTP = MutableLiveData<String>()
    val generatedOTP: LiveData<String> = _generatedOTP

    /**
     * Update phone number and validate in real-time.
     * @param phone Phone number entered by user
     */
    fun updatePhoneNumber(phone: String) {
        _phoneNumber.value = phone
        _isPhoneValid.value = ValidationUtils.isValidPhoneNumber(phone)
    }

    /**
     * Validate phone number and generate OTP.
     * @param phone Phone number to validate
     * @return True if valid and OTP sent
     */
    fun validateAndSendOTP(phone: String): Boolean {
        return if (ValidationUtils.isValidPhoneNumber(phone)) {
            // Generate OTP (since no API, we generate locally)
            val otp = CommonUtils.generateOTP()
            _generatedOTP.value = otp
            
            // Simulate API call delay
            _otpSent.value = true
            _errorMessage.value = null
            
            // Log OTP for testing (remove in production)
            android.util.Log.d("PhoneViewModel", "Generated OTP: $otp for $phone")
            
            true
        } else {
            _errorMessage.value = "Please enter a valid 10-digit phone number"
            false
        }
    }
}
