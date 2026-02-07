package com.example.logistics_driver_app.data.local.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences manager for temporary data storage.
 * Handles app preferences like language, auth tokens, and user settings.
 */
class PreferencesManager private constructor(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREF_NAME = "logistics_driver_prefs"
        private const val KEY_LANGUAGE = "language_code"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_DRIVER_NAME = "driver_name"
        
        @Volatile
        private var INSTANCE: PreferencesManager? = null
        
        /**
         * Get singleton instance of PreferencesManager.
         * @param context Application context
         * @return PreferencesManager instance
         */
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PreferencesManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * Save selected language code.
     * @param languageCode Language code to save (e.g., "en", "hi", "ta")
     */
    fun setLanguage(languageCode: String) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }
    
    /**
     * Get selected language code.
     * @return Language code, defaults to "en"
     */
    fun getLanguage(): String {
        return sharedPreferences.getString(KEY_LANGUAGE, "en") ?: "en"
    }
    
    /**
     * Save phone number after login.
     * @param phoneNumber Phone number to save
     */
    fun setPhoneNumber(phoneNumber: String) {
        sharedPreferences.edit().putString(KEY_PHONE_NUMBER, phoneNumber).apply()
    }
    
    /**
     * Get saved phone number.
     * @return Saved phone number or empty string
     */
    fun getPhoneNumber(): String {
        return sharedPreferences.getString(KEY_PHONE_NUMBER, "") ?: ""
    }
    
    /**
     * Set login status.
     * @param isLoggedIn True if user is logged in
     */
    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    /**
     * Check if user is logged in.
     * @return True if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Save session token.
     * @param token Session token to save
     */
    fun setSessionToken(token: String) {
        sharedPreferences.edit().putString(KEY_SESSION_TOKEN, token).apply()
    }
    
    /**
     * Get session token.
     * @return Session token or empty string
     */
    fun getSessionToken(): String {
        return sharedPreferences.getString(KEY_SESSION_TOKEN, "") ?: ""
    }
    
    /**
     * Mark onboarding as completed.
     * @param completed True if onboarding is completed
     */
    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }
    
    /**
     * Check if onboarding is completed.
     * @return True if onboarding is completed
     */
    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
    
    /**
     * Save driver name.
     * @param name Driver name to save
     */
    fun setDriverName(name: String) {
        sharedPreferences.edit().putString(KEY_DRIVER_NAME, name).apply()
    }
    
    /**
     * Get driver name.
     * @return Driver name or empty string
     */
    fun getDriverName(): String {
        return sharedPreferences.getString(KEY_DRIVER_NAME, "") ?: ""
    }
    
    /**
     * Clear all preferences (logout operation).
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Clear auth-related preferences only.
     */
    fun clearAuthData() {
        sharedPreferences.edit()
            .remove(KEY_PHONE_NUMBER)
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_SESSION_TOKEN)
            .remove(KEY_DRIVER_NAME)
            .apply()
    }
}
