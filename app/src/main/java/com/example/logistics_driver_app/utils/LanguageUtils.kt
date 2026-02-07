package com.example.logistics_driver_app.utils

/**
 * Language utility class to manage supported languages.
 * Provides language data and helper methods.
 */
object LanguageUtils {
    
    /**
     * Get list of supported languages for onboarding.
     * @return List of language maps with code, name, and native name
     */
    fun getSupportedLanguages(): List<Map<String, String>> {
        return listOf(
            mapOf(
                "code" to "en",
                "name" to "English",
                "nativeName" to "English",
                "flag" to "🇬🇧"
            ),
            mapOf(
                "code" to "hi",
                "name" to "Hindi",
                "nativeName" to "हिंदी",
                "flag" to "🇮🇳"
            ),
            mapOf(
                "code" to "ta",
                "name" to "Tamil",
                "nativeName" to "தமிழ்",
                "flag" to "🇮🇳"
            ),
            mapOf(
                "code" to "te",
                "name" to "Telugu",
                "nativeName" to "తెలుగు",
                "flag" to "🇮🇳"
            ),
            mapOf(
                "code" to "kn",
                "name" to "Kannada",
                "nativeName" to "ಕನ್ನಡ",
                "flag" to "🇮🇳"
            ),
            mapOf(
                "code" to "mr",
                "name" to "Marathi",
                "nativeName" to "मराठी",
                "flag" to "🇮🇳"
            )
        )
    }
    
    /**
     * Get language name by code.
     * @param code Language code
     * @return Language name or "English" as default
     */
    fun getLanguageName(code: String): String {
        return getSupportedLanguages().find { it["code"] == code }?.get("name") ?: "English"
    }
    
    /**
     * Get language native name by code.
     * @param code Language code
     * @return Language native name
     */
    fun getLanguageNativeName(code: String): String {
        return getSupportedLanguages().find { it["code"] == code }?.get("nativeName") ?: "English"
    }
}
