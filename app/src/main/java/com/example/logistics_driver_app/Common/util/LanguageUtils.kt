package com.example.logistics_driver_app.Common.util

import android.content.Context
import java.util.Locale

/**
 * LanguageUtils - Utility for managing app localization.
 * Provides functions to change app language dynamically.
 */
object LanguageUtils {
    
    /**
     * Set app language.
     * @param context Context
     * @param languageCode Language code (e.g., "en", "hi")
     */
    fun setLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        
        // Save language preference
        val sharedPreference = SharedPreference.getInstance(context)
        sharedPreference.setLanguage(languageCode)
    }
    
    /**
     * Get current app language.
     * @param context Context
     * @return Current language code
     */
    fun getCurrentLanguage(context: Context): String {
        val sharedPreference = SharedPreference.getInstance(context)
        return sharedPreference.getLanguage()
    }
    
    /**
     * Get language display name.
     * @param languageCode Language code
     * @return Display name of language
     */
    fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "hi" -> "हिंदी"
            else -> "English"
        }
    }
}
