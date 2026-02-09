package com.example.logistics_driver_app.modules.loginModule.entity

/**
 * LanguageEntity - Entity for language selection in login module.
 * Represents available languages for app localization.
 */
data class LanguageEntity(
    val code: String,
    val name: String,
    val nativeName: String,
    val flagEmoji: String
)
