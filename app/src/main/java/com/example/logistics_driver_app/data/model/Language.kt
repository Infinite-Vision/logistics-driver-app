package com.example.logistics_driver_app.data.model

/**
 * Language model - Common entity for language selection.
 * Represents supported languages in the application.
 */
data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flagEmoji: String,
    val isSelected: Boolean = false
)
