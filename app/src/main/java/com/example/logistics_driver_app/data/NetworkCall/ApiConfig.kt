package com.example.logistics_driver_app.data.NetworkCall

/**
 * ApiConfig - Central API configuration and endpoint documentation.
 * 
 * Base URL: https://hyperactively-florescent-addilyn.ngrok-free.dev/api/v1/
 * 
 * Available Endpoints:
 * 
 * 1. Request OTP
 *    POST /auth/otp/request
 *    Request Body: {
 *      "countryCode": "+91",
 *      "phoneNumber": "9886614571"
 *    }
 *    Response: {
 *      "success": true,
 *      "data": {
 *        "message": "OTP sent successfully."
 *      }
 *    }
 * 
 * 2. Verify OTP
 *    POST /auth/otp/verify
 *    Request Body: {
 *      "countryCode": "+91",
 *      "phoneNumber": "9886614571",
 *      "otp": "1234",
 *      "preferredLanguageCode": "EN"
 *    }
 *    Response: {
 *      "success": true,
 *      "data": {
 *        "token": "522dc28f-3468-4d85-b866-e6da5febd099",
 *        "onboardingStatus": "IN_PROGRESS",
 *        "onboardingStep": "OWNER"
 *      }
 *    }
 * 
 * Usage:
 * - RetrofitClient.getApiService() returns configured ApiService instance
 * - All ViewModels should use RetrofitClient.getApiService() to make API calls
 * - Language codes should be sent in UPPERCASE format (EN, HI, TA, TE, KN, MR)
 * - Country code defaults to +91 for India
 */
object ApiConfig {
    const val BASE_URL = "https://hyperactively-florescent-addilyn.ngrok-free.dev/api/v1/"
    const val TIMEOUT_SECONDS = 30L
    
    object Paths {
        const val AUTH_OTP_REQUEST = "auth/otp/request"
        const val AUTH_OTP_VERIFY = "auth/otp/verify"
    }
    
    object Headers {
        const val CONTENT_TYPE = "Content-Type"
        const val APPLICATION_JSON = "application/json"
        const val AUTHORIZATION = "Authorization"
    }
    
    object CountryCodes {
        const val INDIA = "+91"
    }
    
    object LanguageCodes {
        const val ENGLISH = "EN"
        const val HINDI = "HI"
        const val TAMIL = "TA"
        const val TELUGU = "TE"
        const val KANNADA = "KN"
        const val MARATHI = "MR"
    }
}
