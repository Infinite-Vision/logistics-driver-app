package com.example.logistics_driver_app.modules.loginModule.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.RoomDB.AppDatabase
import com.example.logistics_driver_app.data.model.Driver
import com.example.logistics_driver_app.modules.loginModule.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * DriverDetailsViewModel - ViewModel for driver details entry.
 * Manages driver registration and data saving.
 */
class DriverDetailsViewModel(application: Application) : BaseViewModel(application) {
    
    private val driverDao = AppDatabase.getDatabase(application).driverDao()
    private val sharedPreference = SharedPreference.getInstance(application)
    
    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess
    
    /**
     * Save driver details to database and server.
     * @param phoneNumber Phone number
     * @param name Driver name
     * @param vehicleNumber Vehicle number
     * @param licenseNumber License number
     */
    fun saveDriverDetails(
        phoneNumber: String,
        name: String,
        vehicleNumber: String,
        licenseNumber: String
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                
                // Simulate API call delay (replace with actual API call)
                delay(1500)
                
                // TODO: Replace with actual API call
                // val response = apiService.registerDriver(phoneNumber, name, vehicleNumber, licenseNumber)
                // if (response.isSuccessful) {
                //     val driverId = response.body()?.driverId
                //     saveToLocalDatabase(driverId, phoneNumber, name, vehicleNumber, licenseNumber)
                //     _saveSuccess.value = true
                // } else {
                //     _error.value = "Failed to save driver details"
                //     _saveSuccess.value = false
                // }
                
                // For now, simulate success and save locally
                val driverId = (1000..9999).random() // Generate demo ID
                saveToLocalDatabase(driverId, phoneNumber, name, vehicleNumber, licenseNumber)
                _saveSuccess.value = true
                
            } catch (e: Exception) {
                handleException(e)
                _saveSuccess.value = false
            } finally {
                _loading.value = false
            }
        }
    }
    
    /**
     * Save driver details to local Room database.
     * @param driverId Driver ID from server
     * @param phoneNumber Phone number
     * @param name Driver name
     * @param vehicleNumber Vehicle number
     * @param licenseNumber License number
     */
    private suspend fun saveToLocalDatabase(
        driverId: Int,
        phoneNumber: String,
        name: String,
        vehicleNumber: String,
        licenseNumber: String
    ) {
        val driver = Driver(
            id = driverId,
            name = name,
            phoneNumber = phoneNumber,
            vehicleNumber = vehicleNumber,
            licenseNumber = licenseNumber
        )
        
        driverDao.insertDriver(driver)
        
        // Save to preferences
        sharedPreference.setDriverName(name)
        sharedPreference.setDriverId(driverId)
        sharedPreference.setOnboardingCompleted(true)
    }
}
