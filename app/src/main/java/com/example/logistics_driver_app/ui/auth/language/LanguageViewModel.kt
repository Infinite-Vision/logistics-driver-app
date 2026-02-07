package com.example.logistics_driver_app.ui.auth.language

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.logistics_driver_app.data.local.preferences.PreferencesManager
import com.example.logistics_driver_app.utils.LanguageUtils

/**
 * ViewModel for Language Selection screen.
 * Manages language selection state and preferences.
 */
class LanguageViewModel : ViewModel() {

    private val _selectedLanguage = MutableLiveData<String?>()
    val selectedLanguage: LiveData<String?> = _selectedLanguage

    /**
     * Get list of supported languages.
     * @return List of language data
     */
    fun getLanguages(): List<Map<String, String>> {
        return LanguageUtils.getSupportedLanguages()
    }

    /**
     * Select a language.
     * @param languageCode Language code to select
     */
    fun selectLanguage(languageCode: String) {
        _selectedLanguage.value = languageCode
    }

    /**
     * Save language preference to SharedPreferences.
     * @param context Context for accessing SharedPreferences
     */
    fun saveLanguagePreference(context: Context) {
        val languageCode = _selectedLanguage.value ?: "en"
        PreferencesManager.getInstance(context).setLanguage(languageCode)
    }
}
