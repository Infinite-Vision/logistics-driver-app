package com.example.logistics_driver_app.utils

/**
 * Constants used throughout the application.
 * Centralized location for app-wide constant values.
 */
object Constants {
    
    // API Configuration (for future use)
    const val BASE_URL = "https://api.logistics.example.com/"
    const val API_TIMEOUT = 30L // seconds
    
    // Shared Preferences Keys
    const val PREF_NAME = "logistics_driver_prefs"
    
    // Auth Constants
    const val OTP_LENGTH = 4
    const val OTP_RESEND_TIME = 30 // seconds
    const val PHONE_NUMBER_LENGTH = 10
    const val SESSION_VALIDITY_DAYS = 30
    
    // Language Codes
    const val LANG_ENGLISH = "en"
    const val LANG_HINDI = "hi"
    const val LANG_TAMIL = "ta"
    const val LANG_TELUGU = "te"
    const val LANG_KANNADA = "kn"
    const val LANG_MARATHI = "mr"
    
    // Database Constants
    const val DATABASE_NAME = "logistics_driver_db"
    const val DATABASE_VERSION = 1
    
    // Vehicle Types
    val VEHICLE_TYPES = listOf(
        "Two Wheeler",
        "Three Wheeler",
        "Four Wheeler",
        "Truck",
        "Van",
        "Other"
    )
    
    // Intent Extras
    const val EXTRA_PHONE_NUMBER = "extra_phone_number"
    const val EXTRA_OTP = "extra_otp"
    const val EXTRA_LANGUAGE = "extra_language"
    
    // Navigation
    const val DESTINATION_LANGUAGE = "language_selection"
    const val DESTINATION_PHONE = "phone_entry"
    const val DESTINATION_OTP = "otp_verification"
    const val DESTINATION_DRIVER_DETAILS = "driver_details"
    const val DESTINATION_HOME = "home"
}
