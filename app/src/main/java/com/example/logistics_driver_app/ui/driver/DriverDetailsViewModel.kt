package com.example.logistics_driver_app.ui.driver

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.logistics_driver_app.data.local.database.AppDatabase
import com.example.logistics_driver_app.data.local.preferences.PreferencesManager
import com.example.logistics_driver_app.data.model.Driver
import com.example.logistics_driver_app.data.repository.DriverRepository
import com.example.logistics_driver_app.utils.ValidationUtils
import kotlinx.coroutines.launch

/**
 * ViewModel for Driver Details screen.
 * Handles driver data validation, saving, and retrieval.
 */
class DriverDetailsViewModel(private val context: Context) : ViewModel() {

    private val driverRepository: DriverRepository
    private val preferencesManager: PreferencesManager

    init {
        val database = AppDatabase.getDatabase(context)
        driverRepository = DriverRepository(database.driverDao())
        preferencesManager = PreferencesManager.getInstance(context)
        
        // Load existing driver data if any
        loadExistingDriver()
    }

    private val _existingDriver = MutableLiveData<Driver?>()
    val existingDriver: LiveData<Driver?> = _existingDriver

    private val _driverSaved = MutableLiveData<Boolean>()
    val driverSaved: LiveData<Boolean> = _driverSaved

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _validationErrors = MutableLiveData<Map<String, String>>()
    val validationErrors: LiveData<Map<String, String>> = _validationErrors

    /**
     * Load existing driver data by phone number from preferences.
     */
    private fun loadExistingDriver() {
        val phoneNumber = preferencesManager.getPhoneNumber()
        if (phoneNumber.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    val driver = driverRepository.getDriverByPhoneSync(phoneNumber)
                    _existingDriver.value = driver
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to load driver data: ${e.message}"
                }
            }
        }
    }

    /**
     * Validate and save driver details.
     * @param name Driver's full name
     * @param email Email address
     * @param address Physical address
     * @param city City name
     * @param state State name
     * @param pincode PIN code
     * @param vehicleNumber Vehicle registration number
     * @param vehicleType Type of vehicle
     * @param licenseNumber Driving license number
     */
    fun saveDriverDetails(
        name: String,
        email: String,
        address: String,
        city: String,
        state: String,
        pincode: String,
        vehicleNumber: String,
        vehicleType: String,
        licenseNumber: String
    ) {
        // Validate all fields
        val errors = mutableMapOf<String, String>()

        if (name.isEmpty() || !ValidationUtils.isValidName(name)) {
            errors["name"] = "Please enter a valid name"
        }

        if (email.isNotEmpty() && !ValidationUtils.isValidEmail(email)) {
            errors["email"] = "Please enter a valid email address"
        }

        if (pincode.isNotEmpty() && !ValidationUtils.isValidPincode(pincode)) {
            errors["pincode"] = "Please enter a valid 6-digit pincode"
        }

        if (vehicleNumber.isNotEmpty() && !ValidationUtils.isValidVehicleNumber(vehicleNumber)) {
            errors["vehicleNumber"] = "Please enter a valid vehicle number"
        }

        if (licenseNumber.isNotEmpty() && !ValidationUtils.isValidLicenseNumber(licenseNumber)) {
            errors["licenseNumber"] = "Please enter a valid license number"
        }

        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            return
        }

        // Save driver details
        viewModelScope.launch {
            try {
                val phoneNumber = preferencesManager.getPhoneNumber()
                
                // Check if driver already exists
                val existingDriver = driverRepository.getDriverByPhoneSync(phoneNumber)
                
                val driver = if (existingDriver != null) {
                    // Update existing driver
                    existingDriver.copy(
                        name = name,
                        email = email.ifEmpty { null },
                        address = address.ifEmpty { null },
                        city = city.ifEmpty { null },
                        state = state.ifEmpty { null },
                        pincode = pincode.ifEmpty { null },
                        vehicleNumber = vehicleNumber.ifEmpty { null },
                        vehicleType = vehicleType.ifEmpty { null },
                        licenseNumber = licenseNumber.ifEmpty { null },
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    // Create new driver
                    Driver(
                        phoneNumber = phoneNumber,
                        name = name,
                        email = email.ifEmpty { null },
                        address = address.ifEmpty { null },
                        city = city.ifEmpty { null },
                        state = state.ifEmpty { null },
                        pincode = pincode.ifEmpty { null },
                        vehicleNumber = vehicleNumber.ifEmpty { null },
                        vehicleType = vehicleType.ifEmpty { null },
                        licenseNumber = licenseNumber.ifEmpty { null }
                    )
                }

                if (existingDriver != null) {
                    driverRepository.updateDriver(driver)
                } else {
                    driverRepository.insertDriver(driver)
                }

                // Save driver name to preferences
                preferencesManager.setDriverName(name)
                preferencesManager.setOnboardingCompleted(true)

                _driverSaved.value = true
                _errorMessage.value = null
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save driver details: ${e.message}"
                _driverSaved.value = false
            }
        }
    }
}

/**
 * ViewModelFactory for DriverDetailsViewModel.
 * Required to pass Context to ViewModel.
 */
class DriverDetailsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DriverDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DriverDetailsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
