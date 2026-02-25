package com.example.logistics_driver_app.Common.util

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreference - Utility class for local storage using SharedPreferences.
 * Provides type-safe access to persistent data storage.
 */
class SharedPreference private constructor(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREF_NAME = "logistics_driver_prefs"
        
        // Keys
        private const val KEY_LANGUAGE = "language_code"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_DRIVER_NAME = "driver_name"
        private const val KEY_DRIVER_ID = "driver_id"
        
        // Owner details keys
        private const val KEY_OWNER_NAME = "owner_name"
        private const val KEY_OWNER_AADHAAR_URI = "owner_aadhaar_uri"
        private const val KEY_OWNER_PAN_URI = "owner_pan_uri"
        private const val KEY_OWNER_SELFIE_URI = "owner_selfie_uri"
        
        // Vehicle details keys
        private const val KEY_VEHICLE_NUMBER = "vehicle_number"
        private const val KEY_VEHICLE_RC_URI = "vehicle_rc_uri"
        private const val KEY_VEHICLE_CITY = "vehicle_city"
        private const val KEY_VEHICLE_TYPE = "vehicle_type"
        private const val KEY_BODY_TYPE = "body_type"
        private const val KEY_BODY_CAPACITY = "body_capacity"
        
        // Driver details keys
        private const val KEY_WILL_DRIVE = "will_drive"
        private const val KEY_DRIVER_PHONE = "driver_phone"
        private const val KEY_LICENSE_URI = "license_uri"
        private const val KEY_CURRENT_ORDER_ID = "current_order_id"
        private const val KEY_CURRENT_ORDER_FARE = "current_order_fare"
        private const val KEY_CURRENT_CUSTOMER_NAME = "current_customer_name"
        private const val KEY_CURRENT_PICKUP_ADDRESS = "current_pickup_address"
        private const val KEY_CURRENT_DROP_ADDRESS = "current_drop_address"
        private const val KEY_CURRENT_CONTACT_PHONE = "current_contact_phone"
        private const val KEY_CURRENT_DROP_LANDMARK = "current_drop_landmark"
        
        @Volatile
        private var INSTANCE: SharedPreference? = null
        
        /**
         * Get singleton instance of SharedPreference.
         * @param context Application context
         * @return SharedPreference instance
         */
        fun getInstance(context: Context): SharedPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = SharedPreference(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * Save string value.
     * @param key Storage key
     * @param value String value to save
     */
    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    /**
     * Get string value.
     * @param key Storage key
     * @param defaultValue Default value if key not found
     * @return Stored string value or default
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    
    /**
     * Save integer value.
     * @param key Storage key
     * @param value Integer value to save
     */
    fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    /**
     * Get integer value.
     * @param key Storage key
     * @param defaultValue Default value if key not found
     * @return Stored integer value or default
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    
    /**
     * Save boolean value.
     * @param key Storage key
     * @param value Boolean value to save
     */
    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    /**
     * Get boolean value.
     * @param key Storage key
     * @param defaultValue Default value if key not found
     * @return Stored boolean value or default
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    /**
     * Save long value.
     * @param key Storage key
     * @param value Long value to save
     */
    fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
    
    /**
     * Get long value.
     * @param key Storage key
     * @param defaultValue Default value if key not found
     * @return Stored long value or default
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }
    
    // Specific app preferences
    
    fun setLanguage(languageCode: String) = putString(KEY_LANGUAGE, languageCode)
    fun getLanguage() = getString(KEY_LANGUAGE, "en")
    fun isLanguageSet(): Boolean = sharedPreferences.contains(KEY_LANGUAGE)
    
    fun setPhoneNumber(phoneNumber: String) = putString(KEY_PHONE_NUMBER, phoneNumber)
    fun getPhoneNumber() = getString(KEY_PHONE_NUMBER)
    
    fun setLoggedIn(isLoggedIn: Boolean) = putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
    fun isLoggedIn() = getBoolean(KEY_IS_LOGGED_IN)
    
    fun setSessionToken(token: String) = putString(KEY_SESSION_TOKEN, token)
    fun getSessionToken() = getString(KEY_SESSION_TOKEN)
    
    fun setOnboardingCompleted(completed: Boolean) = putBoolean(KEY_ONBOARDING_COMPLETED, completed)
    fun isOnboardingCompleted() = getBoolean(KEY_ONBOARDING_COMPLETED)
    
    fun setDriverName(name: String) = putString(KEY_DRIVER_NAME, name)
    fun getDriverName() = getString(KEY_DRIVER_NAME)
    
    fun setDriverId(id: Int) = putInt(KEY_DRIVER_ID, id)
    fun getDriverId() = getInt(KEY_DRIVER_ID)
    
    // Owner details
    fun saveOwnerName(name: String) = putString(KEY_OWNER_NAME, name)
    fun getOwnerName() = getString(KEY_OWNER_NAME)
    
    fun saveOwnerAadhaarUri(uri: String) = putString(KEY_OWNER_AADHAAR_URI, uri)
    fun getOwnerAadhaarUri() = getString(KEY_OWNER_AADHAAR_URI)
    
    fun saveOwnerPANUri(uri: String) = putString(KEY_OWNER_PAN_URI, uri)
    fun getOwnerPANUri() = getString(KEY_OWNER_PAN_URI)
    
    fun saveOwnerSelfieUri(uri: String) = putString(KEY_OWNER_SELFIE_URI, uri)
    fun getOwnerSelfieUri() = getString(KEY_OWNER_SELFIE_URI)
    
    // Vehicle details
    fun saveVehicleNumber(number: String) = putString(KEY_VEHICLE_NUMBER, number)
    fun getVehicleNumber() = getString(KEY_VEHICLE_NUMBER)
    
    fun saveVehicleRCUri(uri: String) = putString(KEY_VEHICLE_RC_URI, uri)
    fun getVehicleRCUri() = getString(KEY_VEHICLE_RC_URI)
    
    fun saveVehicleCity(city: String) = putString(KEY_VEHICLE_CITY, city)
    fun getVehicleCity() = getString(KEY_VEHICLE_CITY)
    
    fun saveVehicleType(type: String) = putString(KEY_VEHICLE_TYPE, type)
    fun getVehicleType() = getString(KEY_VEHICLE_TYPE)
    
    fun saveBodyType(type: String) = putString(KEY_BODY_TYPE, type)
    fun getBodyType() = getString(KEY_BODY_TYPE)
    
    fun saveBodyCapacity(capacity: String) = putString(KEY_BODY_CAPACITY, capacity)
    fun getBodyCapacity() = getString(KEY_BODY_CAPACITY)
    
    // Driver details
    fun saveWillDrive(willDrive: Boolean) = putBoolean(KEY_WILL_DRIVE, willDrive)
    fun getWillDrive() = getBoolean(KEY_WILL_DRIVE)
    
    fun saveDriverPhone(phone: String) = putString(KEY_DRIVER_PHONE, phone)
    fun getDriverPhone() = getString(KEY_DRIVER_PHONE)
    
    fun saveLicenseUri(uri: String) = putString(KEY_LICENSE_URI, uri)
    fun getLicenseUri() = getString(KEY_LICENSE_URI)

    // ---- Active Trip ----
    fun saveOrderId(orderId: Long) = sharedPreferences.edit().putLong(KEY_CURRENT_ORDER_ID, orderId).apply()
    fun getOrderId(): Long = sharedPreferences.getLong(KEY_CURRENT_ORDER_ID, -1L)
    fun clearOrderId() = sharedPreferences.edit().remove(KEY_CURRENT_ORDER_ID).apply()

    fun saveOrderFare(fare: Double) = sharedPreferences.edit().putFloat(KEY_CURRENT_ORDER_FARE, fare.toFloat()).apply()
    fun getOrderFare(): Double = sharedPreferences.getFloat(KEY_CURRENT_ORDER_FARE, 0f).toDouble()

    fun saveCurrentCustomerName(name: String) = putString(KEY_CURRENT_CUSTOMER_NAME, name)
    fun getCurrentCustomerName(): String = getString(KEY_CURRENT_CUSTOMER_NAME) ?: ""

    fun saveCurrentPickupAddress(addr: String) = putString(KEY_CURRENT_PICKUP_ADDRESS, addr)
    fun getCurrentPickupAddress(): String = getString(KEY_CURRENT_PICKUP_ADDRESS) ?: ""

    fun saveCurrentDropAddress(addr: String) = putString(KEY_CURRENT_DROP_ADDRESS, addr)
    fun getCurrentDropAddress(): String = getString(KEY_CURRENT_DROP_ADDRESS) ?: ""

    fun saveCurrentContactPhone(phone: String) = putString(KEY_CURRENT_CONTACT_PHONE, phone)
    fun getCurrentContactPhone(): String = getString(KEY_CURRENT_CONTACT_PHONE) ?: ""

    fun saveCurrentDropLandmark(landmark: String) = putString(KEY_CURRENT_DROP_LANDMARK, landmark)
    fun getCurrentDropLandmark(): String = getString(KEY_CURRENT_DROP_LANDMARK) ?: ""
    
    /**
     * Clear all preferences.
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Clear authentication data only.
     */
    fun clearAuthData() {
        sharedPreferences.edit()
            .remove(KEY_PHONE_NUMBER)
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_SESSION_TOKEN)
            .remove(KEY_DRIVER_NAME)
            .remove(KEY_DRIVER_ID)
            .apply()
    }
    
    /**
     * Remove specific key.
     * @param key Key to remove
     */
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
    
    /**
     * Check if key exists.
     * @param key Key to check
     * @return True if key exists
     */
    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}
