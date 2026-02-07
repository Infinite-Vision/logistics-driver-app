package com.example.logistics_driver_app.data.model

/**
 * Language data class representing supported languages in the app.
 * Used for onboarding language selection.
 */
data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flagEmoji: String
)
