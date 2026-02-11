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
 * OnboardingViewModel - Manages onboarding flow for Owner, Vehicle, and Driver details.
 * Based on Backend Specification v1 - Onboarding Modules.
 */
class OnboardingViewModel(application: Application) : BaseViewModel(application) {
    
    companion object {
        private const val TAG = "OnboardingViewModel"
    }
    
    private val apiService = RetrofitClient.getApiService()
    private val sharedPreference = SharedPreference.getInstance(application)
    
    // ============= Owner State =============
    
    private val _ownerSaveSuccess = MutableLiveData<Boolean>()
    val ownerSaveSuccess: LiveData<Boolean> = _ownerSaveSuccess
    
    private val _ownerData = MutableLiveData<OwnerResponse>()
    val ownerData: LiveData<OwnerResponse> = _ownerData
    
    // ============= Vehicle State =============
    
    private val _vehicleSaveSuccess = MutableLiveData<Boolean>()
    val vehicleSaveSuccess: LiveData<Boolean> = _vehicleSaveSuccess
    
    private val _vehicleData = MutableLiveData<VehicleResponse>()
    val vehicleData: LiveData<VehicleResponse> = _vehicleData
    
    private val _vehicleFormOptions = MutableLiveData<VehicleFormOptionsResponse>()
    val vehicleFormOptions: LiveData<VehicleFormOptionsResponse> = _vehicleFormOptions
    
    // ============= Driver State =============
    
    private val _driverSaveSuccess = MutableLiveData<Boolean>()
    val driverSaveSuccess: LiveData<Boolean> = _driverSaveSuccess
    
    private val _driverData = MutableLiveData<DriverResponse>()
    val driverData: LiveData<DriverResponse> = _driverData
    
    // ============= Common State =============
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    // ============= Owner Methods =============
    
    /**
     * 4.1 Save Owner Details
     * Endpoint: POST /api/v1/onboarding/owner
     */
    fun saveOwner(
        name: String,
        ownerSelfieUrl: String,
        ownerAdharUrl: String,
        ownerPanUrl: String
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "[API] saveOwner() called")
                Log.d(TAG, "[API] Parameters - name: $name")
                Log.d(TAG, "[API] Parameters - selfieUrl: $ownerSelfieUrl")
                Log.d(TAG, "[API] Parameters - aadhaarUrl: $ownerAdharUrl")
                Log.d(TAG, "[API] Parameters - panUrl: $ownerPanUrl")
                
                _loading.value = true
                _error.value = null
                _errorMessage.value = null
                
                val token = "Bearer ${sharedPreference.getSessionToken()}"
                Log.d(TAG, "[API] Token: ${if (token.length > 10) "Bearer ${token.substring(7, 17)}..." else "INVALID"}")
                
                val request = SaveOwnerRequest(name, ownerSelfieUrl, ownerAdharUrl, ownerPanUrl)
                Log.d(TAG, "[API] Calling API endpoint: POST /api/v1/onboarding/owner")
                
                val response = apiService.saveOwner(token, request)
                
                Log.d(TAG, "[API] Response received")
                Log.d(TAG, "[API] Response code: ${response.code()}")
                Log.d(TAG, "[API] Response successful: ${response.isSuccessful}")
                Log.d(TAG, "[API] Response body success: ${response.body()?.success}")
                Log.d(TAG, "[API] Response message: ${response.body()?.message}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _ownerData.value = response.body()?.data
                    Log.d(TAG, "[API] Owner data saved: ${response.body()?.data}")
                    Log.d(TAG, "[API] Setting ownerSaveSuccess to TRUE")
                    _ownerSaveSuccess.value = true
                    Log.d(TAG, "[API] ownerSaveSuccess posted to observers")
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to save owner details"
                    Log.e(TAG, "[API] Save failed: $errorMsg")
                    _errorMessage.value = errorMsg
                    _ownerSaveSuccess.value = false
                }
                
            } catch (e: HttpException) {
                Log.e(TAG, "[API] HttpException in saveOwner", e)
                Log.e(TAG, "[API] HTTP Code: ${e.code()}")
                Log.e(TAG, "[API] HTTP Message: ${e.message()}")
                handleHttpException(e)
                _ownerSaveSuccess.value = false
            } catch (e: IOException) {
                Log.e(TAG, "[API] IOException in saveOwner", e)
                _errorMessage.value = "Network error. Please check your connection."
                _ownerSaveSuccess.value = false
            } catch (e: Exception) {
                Log.e(TAG, "[API] Exception in saveOwner", e)
                handleException(e)
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _ownerSaveSuccess.value = false
            } finally {
                _loading.value = false
                Log.d(TAG, "[API] saveOwner() completed")
            }
        }
    }
    
    /**
     * 4.2 Get Owner Details
     * Endpoint: GET /api/v1/onboarding/owner
     */
    fun getOwner() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                
                val token = "Bearer ${sharedPreference.getSessionToken()}"
                val response = apiService.getOwner(token)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _ownerData.value = response.body()?.data
                } else {
                    _errorMessage.value = response.body()?.message ?: "Failed to fetch owner details"
                }
                
            } catch (e: Exception) {
                handleException(e)
            } finally {
                _loading.value = false
            }
        }
    }
    
    // ============= Vehicle Methods =============
    
    /**
     * 5.1 Save Vehicle Details
     * Endpoint: POST /api/v1/onboarding/vehicle
     */
    fun saveVehicle(
        registrationNumber: String,
        vehicleType: String,
        bodyType: String,
        bodySpec: String,
        rcUrl: String,
        insuranceUrl: String,
        pucUrl: String
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "[API] saveVehicle() called")
                Log.d(TAG, "[API] Parameters - registrationNumber: $registrationNumber")
                Log.d(TAG, "[API] Parameters - vehicleType: $vehicleType")
                Log.d(TAG, "[API] Parameters - bodyType: $bodyType")
                Log.d(TAG, "[API] Parameters - bodySpec: $bodySpec")
                Log.d(TAG, "[API] Parameters - rcUrl: $rcUrl")
                Log.d(TAG, "[API] Parameters - insuranceUrl: $insuranceUrl")
                Log.d(TAG, "[API] Parameters - pucUrl: $pucUrl")
                
                _loading.value = true
                _error.value = null
                _errorMessage.value = null
                
                val token = "Bearer ${sharedPreference.getSessionToken()}"
                Log.d(TAG, "[API] Token: ${if (token.length > 10) "Bearer ${token.substring(7, 17)}..." else "INVALID"}")
                
                val request = SaveVehicleRequest(
                    registrationNumber, vehicleType, bodyType, bodySpec,
                    rcUrl, insuranceUrl, pucUrl
                )
                Log.d(TAG, "[API] Request body created: registrationNumber=$registrationNumber, types=$vehicleType/$bodyType/$bodySpec")
                Log.d(TAG, "[API] Calling API endpoint: POST /api/v1/onboarding/vehicle")
                
                val response = apiService.saveVehicle(token, request)
                
                Log.d(TAG, "[API] Response received")
                Log.d(TAG, "[API] Response code: ${response.code()}")
                Log.d(TAG, "[API] Response successful: ${response.isSuccessful}")
                Log.d(TAG, "[API] Response body success: ${response.body()?.success}")
                Log.d(TAG, "[API] Response message: ${response.body()?.message}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _vehicleData.value = response.body()?.data
                    Log.d(TAG, "[API] Vehicle data saved: ${response.body()?.data}")
                    Log.d(TAG, "[API] Setting vehicleSaveSuccess to TRUE")
                    _vehicleSaveSuccess.value = true
                    Log.d(TAG, "[API] vehicleSaveSuccess posted to observers")
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to save vehicle details"
                    Log.e(TAG, "[API] Save failed: $errorMsg")
                    _errorMessage.value = errorMsg
                    _vehicleSaveSuccess.value = false
                }
                
            } catch (e: HttpException) {
                Log.e(TAG, "[API] HttpException in saveVehicle", e)
                Log.e(TAG, "[API] HTTP Code: ${e.code()}")
                Log.e(TAG, "[API] HTTP Message: ${e.message()}")
                handleHttpException(e)
                _vehicleSaveSuccess.value = false
            } catch (e: IOException) {
                Log.e(TAG, "[API] IOException in saveVehicle", e)
                _errorMessage.value = "Network error. Please check your connection."
                _vehicleSaveSuccess.value = false
            } catch (e: Exception) {
                Log.e(TAG, "[API] Exception in saveVehicle", e)
                handleException(e)
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _vehicleSaveSuccess.value = false
            } finally {
                _loading.value = false
                Log.d(TAG, "[API] saveVehicle() completed")
            }
        }
    }
    
    /**
     * 5.2 Get Vehicle Details
     * Endpoint: GET /api/v1/onboarding/vehicle
     */
    fun getVehicle() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                
                val token = "Bearer ${sharedPreference.getSessionToken()}"
                val response = apiService.getVehicle(token)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _vehicleData.value = response.body()?.data
                } else {
                    _errorMessage.value = response.body()?.message ?: "Failed to fetch vehicle details"
                }
                
            } catch (e: Exception) {
                handleException(e)
            } finally {
                _loading.value = false
            }
        }
    }
    
    /**
     * 7.1 Get Vehicle Form Options
     * Endpoint: GET /api/v1/meta/vehicle-form-options
     */
    fun getVehicleFormOptions() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                
                val token = "Bearer ${sharedPreference.getSessionToken()}"
                val response = apiService.getVehicleFormOptions(token)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _vehicleFormOptions.value = response.body()?.data
                } else {
                    _errorMessage.value = response.body()?.message ?: "Failed to fetch form options"
                }
                
            } catch (e: Exception) {
                handleException(e)
            } finally {
                _loading.value = false
            }
        }
    }
    
    // ============= Driver Methods =============
    
    /**
     * 6.1 Save Driver Details
     * Endpoint: POST /api/v1/onboarding/driver
     */
    fun saveDriver(
        isSelfDriving: Boolean,
        name: String?,
        phoneNumber: String?,
        driverLicenseUrl: String?
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "[API] saveDriver() called")
                Log.d(TAG, "[API] Parameters - isSelfDriving: $isSelfDriving")
                Log.d(TAG, "[API] Parameters - name: ${name ?: "null (self-driving)"}")
                Log.d(TAG, "[API] Parameters - phoneNumber: ${phoneNumber ?: "null (self-driving)"}")
                Log.d(TAG, "[API] Parameters - licenseUrl: $driverLicenseUrl")
                
                _loading.value = true
                _error.value = null
                _errorMessage.value = null
                
                val token = "Bearer ${sharedPreference.getSessionToken()}"
                Log.d(TAG, "[API] Token: ${if (token.length > 10) "Bearer ${token.substring(7, 17)}..." else "INVALID"}")
                
                val request = SaveDriverRequest(isSelfDriving, name, phoneNumber, driverLicenseUrl)
                Log.d(TAG, "[API] Request body created: isSelfDriving=$isSelfDriving")
                Log.d(TAG, "[API] Calling API endpoint: POST /api/v1/onboarding/driver")
                
                val response = apiService.saveDriver(token, request)
                
                Log.d(TAG, "[API] Response received")
                Log.d(TAG, "[API] Response code: ${response.code()}")
                Log.d(TAG, "[API] Response successful: ${response.isSuccessful}")
                Log.d(TAG, "[API] Response body success: ${response.body()?.success}")
                Log.d(TAG, "[API] Response message: ${response.body()?.message}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _driverData.value = response.body()?.data
                    Log.d(TAG, "[API] Driver data saved: ${response.body()?.data}")
                    Log.d(TAG, "[API] Setting driverSaveSuccess to TRUE")
                    _driverSaveSuccess.value = true
                    Log.d(TAG, "[API] driverSaveSuccess posted to observers")
                    
                    // Mark onboarding as completed if available
                    val step = response.body()?.data?.onboardingStep
                    Log.d(TAG, "[API] Onboarding step from response: $step")
                    if (step == "SUBMITTED") {
                        sharedPreference.setOnboardingCompleted(true)
                        Log.d(TAG, "[API] Onboarding marked as completed")
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to save driver details"
                    Log.e(TAG, "[API] Save failed: $errorMsg")
                    _errorMessage.value = errorMsg
                    _driverSaveSuccess.value = false
                }
                
            } catch (e: HttpException) {
                Log.e(TAG, "[API] HttpException in saveDriver", e)
                Log.e(TAG, "[API] HTTP Code: ${e.code()}")
                Log.e(TAG, "[API] HTTP Message: ${e.message()}")
                handleHttpException(e)
                _driverSaveSuccess.value = false
            } catch (e: IOException) {
                Log.e(TAG, "[API] IOException in saveDriver", e)
                _errorMessage.value = "Network error. Please check your connection."
                _driverSaveSuccess.value = false
            } catch (e: Exception) {
                Log.e(TAG, "[API] Exception in saveDriver", e)
                handleException(e)
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _driverSaveSuccess.value = false
            } finally {
                _loading.value = false
                Log.d(TAG, "[API] saveDriver() completed")
            }
        }
    }
    
    /**
     * 6.2 Get Driver Details
     * Endpoint: GET /api/v1/onboarding/driver
     */
    fun getDriver() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                
                val token = "Bearer ${sharedPreference.getSessionToken()}"
                val response = apiService.getDriver(token)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _driverData.value = response.body()?.data
                } else {
                    _errorMessage.value = response.body()?.message ?: "Failed to fetch driver details"
                }
                
            } catch (e: Exception) {
                handleException(e)
            } finally {
                _loading.value = false
            }
        }
    }
    
    // ============= Helper Methods =============
    
    private fun handleHttpException(e: HttpException) {
        when (e.code()) {
            400 -> _errorMessage.value = "Invalid data. Please check your inputs."
            401 -> _errorMessage.value = "Session expired. Please login again."
            403 -> _errorMessage.value = "Access denied."
            429 -> _errorMessage.value = "Too many requests. Please wait a moment."
            500 -> _errorMessage.value = "Service temporarily unavailable. Please try again later."
            else -> _errorMessage.value = "Error: ${e.message()}"
        }
    }
}
