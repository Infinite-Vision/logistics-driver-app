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
