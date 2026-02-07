package com.example.logistics_driver_app.ui.auth.otp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logistics_driver_app.data.local.database.AppDatabase
import com.example.logistics_driver_app.data.local.preferences.PreferencesManager
import com.example.logistics_driver_app.data.model.AuthSession
import com.example.logistics_driver_app.data.repository.AuthRepository
import com.example.logistics_driver_app.utils.ValidationUtils
import kotlinx.coroutines.launch

/**
 * ViewModel for OTP Verification screen.
 * Handles OTP validation and authentication session management.
 */
class OTPViewModel : ViewModel() {

    private val _enteredOTP = MutableLiveData<String>()
    val enteredOTP: LiveData<String> = _enteredOTP

    private val _expectedOTP = MutableLiveData<String>()

    private val _otpVerified = MutableLiveData<Boolean>()
    val otpVerified: LiveData<Boolean> = _otpVerified

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _canResendOTP = MutableLiveData<Boolean>()
    val canResendOTP: LiveData<Boolean> = _canResendOTP

    /**
     * Set the expected OTP for verification.
     * @param otp Expected OTP string
     */
    fun setExpectedOTP(otp: String) {
        _expectedOTP.value = otp
    }

    /**
     * Update the entered OTP as user types.
     * @param otp Entered OTP string
     */
    fun updateEnteredOTP(otp: String) {
        _enteredOTP.value = otp
    }

    /**
     * Verify the entered OTP against expected OTP.
     * @param enteredOTP OTP entered by user
     */
    fun verifyOTP(enteredOTP: String) {
        if (!ValidationUtils.isValidOTP(enteredOTP)) {
            _errorMessage.value = "Please enter a valid 4-digit OTP"
            return
        }

        val expectedOTP = _expectedOTP.value ?: ""
        
        if (enteredOTP == expectedOTP) {
            _otpVerified.value = true
            _errorMessage.value = null
        } else {
            _otpVerified.value = false
            _errorMessage.value = "Invalid OTP. Please try again"
        }
    }

    /**
     * Set whether OTP can be resent.
     * @param canResend True if resend is available
     */
    fun setCanResendOTP(canResend: Boolean) {
        _canResendOTP.value = canResend
    }

    /**
     * Save authentication session to database and preferences.
     * @param context Context for accessing database
     * @param phoneNumber User's phone number
     */
    fun saveAuthSession(context: Context, phoneNumber: String) {
        viewModelScope.launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val authRepository = AuthRepository(database.authDao())
                val prefsManager = PreferencesManager.getInstance(context)

                // Create auth session
                val authSession = AuthSession(
                    phoneNumber = phoneNumber,
                    otp = _expectedOTP.value,
                    isVerified = true,
                    sessionToken = "token_${System.currentTimeMillis()}",
                    languageCode = prefsManager.getLanguage()
                )

                // Save to database
                authRepository.saveAuthSession(authSession)

                // Save to preferences
                prefsManager.setPhoneNumber(phoneNumber)
                prefsManager.setLoggedIn(true)
                prefsManager.setSessionToken(authSession.sessionToken ?: "")
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save session: ${e.message}"
            }
        }
    }
}
