package com.example.logistics_driver_app.Common.util

/**
 * Constants - Application-wide constants.
 * Contains API endpoints, keys, and configuration values.
 */
object Constants {
    
    // API Configuration
    const val BASE_URL = "http://43.205.235.73:8080/api/v1/"
    const val API_TIMEOUT = 30L // seconds
    
    // API Endpoints
    object Endpoints {
        const val SEND_OTP = "auth/otp/request"
        const val VERIFY_OTP = "auth/otp/verify"
        const val REGISTER_DRIVER = "driver/register"
        const val UPDATE_DRIVER = "driver/update"
        const val GET_DRIVER_PROFILE = "driver/profile"
    }
    
    // Request Codes
    const val REQUEST_CODE_CAMERA = 100
    const val REQUEST_CODE_GALLERY = 101
    const val REQUEST_CODE_LOCATION = 102
    
    // Preferences Keys
    object PrefsKeys {
        const val LANGUAGE = "language_code"
        const val PHONE_NUMBER = "phone_number"
        const val IS_LOGGED_IN = "is_logged_in"
        const val SESSION_TOKEN = "session_token"
        const val DRIVER_NAME = "driver_name"
        const val DRIVER_ID = "driver_id"
    }
    
    // Language Codes
    object Languages {
        const val ENGLISH = "en"
        const val HINDI = "hi"
    }
    
    // Validation
    const val PHONE_NUMBER_LENGTH = 10
    const val OTP_LENGTH = 4
    const val MIN_NAME_LENGTH = 2
    const val MIN_LICENSE_LENGTH = 5
    
    // Timer
    const val OTP_RESEND_TIMEOUT = 60 // seconds
    
    // Database
    const val DATABASE_NAME = "logistics_driver_database"
    const val DATABASE_VERSION = 1
}
