package com.example.logistics_driver_app.modules.loginModule.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.data.NetworkCall.RetrofitClient
import com.example.logistics_driver_app.data.model.*
import com.example.logistics_driver_app.modules.loginModule.base.BaseViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * AppStateViewModel - Manages app state and navigation flow.
 * Based on Backend Specification v1 - App State Module.
 */
class AppStateViewModel(application: Application) : BaseViewModel(application) {
    
    companion object {
        private const val TAG = "AppStateViewModel"
    }
    
    private val apiService = RetrofitClient.getApiService()
    private val sharedPreference = SharedPreference.getInstance(application)
    
    private val _appState = MutableLiveData<AppStateResponse>()
    val appState: LiveData<AppStateResponse> = _appState
    
    private val _nextScreen = MutableLiveData<String>()
    val nextScreen: LiveData<String> = _nextScreen
    
    private val _onboardingStatus = MutableLiveData<String>()
    val onboardingStatus: LiveData<String> = _onboardingStatus
    
    private val _driverStatus = MutableLiveData<String>()
    val driverStatus: LiveData<String> = _driverStatus
    
    private val _preferredLanguage = MutableLiveData<String>()
    val preferredLanguage: LiveData<String> = _preferredLanguage
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    /**
     * 2.1 Get App State
     * Fetches user's current onboarding progress and determines next screen.
     * Endpoint: GET /api/v1/app/state
     */
    fun getAppState() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "[API] getAppState() called")
                
                _loading.value = true
                _error.value = null
                _errorMessage.value = null
                
                val token = "Bearer ${sharedPreference.getSessionToken()}"
                Log.d(TAG, "[API] Token: ${if (token.length > 10) "Bearer ${token.substring(7, 17)}..." else "INVALID"}")
                Log.d(TAG, "[API] Calling API endpoint: GET /api/v1/app/state")
                
                val response = apiService.getAppState(token)
                
                Log.d(TAG, "[API] Response received")
                Log.d(TAG, "[API] Response code: ${response.code()}")
                Log.d(TAG, "[API] Response successful: ${response.isSuccessful}")
                Log.d(TAG, "[API] Response body success: ${response.body()?.success}")
                Log.d(TAG, "[API] Response message: ${response.body()?.message}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        Log.d(TAG, "[API] App state data received")
                        Log.d(TAG, "[API] nextScreen: ${data.nextScreen}")
                        Log.d(TAG, "[API] onboardingStatus: ${data.onboardingStatus}")
                        Log.d(TAG, "[API] driverStatus: ${data.driverStatus}")
                        Log.d(TAG, "[API] preferredLanguage: ${data.preferredLanguage}")
                        
                        _appState.value = data
                        
                        val nextScreenValue = data.nextScreen
                        Log.d(TAG, "[API] Setting nextScreen to: $nextScreenValue")
                        if (!nextScreenValue.isNullOrEmpty()) {
                            _nextScreen.value = nextScreenValue
                            Log.d(TAG, "[API] nextScreen posted to observers")
                        } else {
                            Log.w(TAG, "[API] nextScreen is null or empty, not posting")
                        }
                        
                        _onboardingStatus.value = data.onboardingStatus ?: ""
                        _driverStatus.value = data.driverStatus ?: ""
                        _preferredLanguage.value = data.preferredLanguage ?: ""
                        
                        // Save to preferences
                        val language = data.preferredLanguage
                        if (!language.isNullOrEmpty()) {
                            sharedPreference.setLanguage(language)
                            Log.d(TAG, "[API] Language saved to preferences: $language")
                        }
                    } else {
                        Log.w(TAG, "[API] Response data is NULL")
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to fetch app state"
                    Log.e(TAG, "[API] Failed to fetch app state: $errorMsg")
                    _errorMessage.value = errorMsg
                }
                
            } catch (e: HttpException) {
                Log.e(TAG, "[API] HttpException in getAppState", e)
                Log.e(TAG, "[API] HTTP Code: ${e.code()}")
                Log.e(TAG, "[API] HTTP Message: ${e.message()}")
                
                when (e.code()) {
                    401 -> {
                        _errorMessage.value = "Session expired. Please login again."
                        // Clear session
                        sharedPreference.setLoggedIn(false)
                        sharedPreference.setSessionToken("")
                    }
                    500 -> _errorMessage.value = "Service temporarily unavailable. Please try again later."
                    else -> _errorMessage.value = "Error: ${e.message()}"
                }
            } catch (e: IOException) {
                Log.e(TAG, "[API] IOException in getAppState", e)
                _errorMessage.value = "Network error. Please check your connection."
            } catch (e: Exception) {
                Log.e(TAG, "[API] Exception in getAppState", e)
                handleException(e)
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _loading.value = false
                Log.d(TAG, "[API] getAppState() completed")
            }
        }
    }
    
    /**
     * 3.1 Update Preferred Language
     * Updates user's preferred language.
     * Endpoint: POST /api/v1/users/language
     */
    fun updateLanguage(phoneNumber: String, language: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _errorMessage.value = null
                
                val token = "Bearer ${sharedPreference.getSessionToken()}"
                val request = UpdateLanguageRequest(phoneNumber, language)
                val response = apiService.updateLanguage(token, request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedLanguage = response.body()?.data?.preferredLanguage ?: language
                    _preferredLanguage.value = updatedLanguage
                    
                    // Save to preferences
                    sharedPreference.setLanguage(updatedLanguage)
                } else {
                    _errorMessage.value = response.body()?.message ?: "Failed to update language"
                }
                
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> _errorMessage.value = "Invalid language selection."
                    401 -> _errorMessage.value = "Session expired. Please login again."
                    500 -> _errorMessage.value = "Service temporarily unavailable. Please try again later."
                    else -> _errorMessage.value = "Error: ${e.message()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Network error. Please check your connection."
            } catch (e: Exception) {
                handleException(e)
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _loading.value = false
            }
        }
    }
    
    /**
     * 1.3 Logout
     * Revokes current JWT token and clears session.
     * Endpoint: POST /api/v1/auth/logout
     */
    fun logout() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                
                val token = "Bearer ${sharedPreference.getSessionToken()}"
                val response = apiService.logout(token)
                
                if (response.isSuccessful) {
                    // Clear session
                    clearSession()
                } else {
                    _errorMessage.value = "Failed to logout"
                    // Clear session anyway
                    clearSession()
                }
                
            } catch (e: Exception) {
                // Clear session even if API call fails
                clearSession()
            } finally {
                _loading.value = false
            }
        }
    }
    
    private fun clearSession() {
        sharedPreference.setLoggedIn(false)
        sharedPreference.setSessionToken("")
        sharedPreference.setPhoneNumber("")
        sharedPreference.setOnboardingCompleted(false)
    }
}
