package com.example.logistics_driver_app.modules.loginModule.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.logistics_driver_app.Common.util.SharedPreference
import com.example.logistics_driver_app.data.model.Language
import com.example.logistics_driver_app.modules.loginModule.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 * LanguageViewModel - ViewModel for language selection.
 * Manages language selection logic and state.
 */
class LanguageViewModel(application: Application) : BaseViewModel(application) {
    
    private val sharedPreference = SharedPreference.getInstance(application)
    
    private val _languages = MutableLiveData<List<Language>>()
    val languages: LiveData<List<Language>> = _languages
    
    private val _selectedLanguage = MutableLiveData<Language?>()
    val selectedLanguage: LiveData<Language?> = _selectedLanguage
    
    /**
     * Load available languages.
     */
    fun loadLanguages() {
        viewModelScope.launch {
            val languageList = listOf(
                Language(
                    code = "en",
                    name = "English",
                    nativeName = "English",
                    flagEmoji = "🇬🇧"
                ),
                Language(
                    code = "hi",
                    name = "Hindi",
                    nativeName = "हिंदी",
                    flagEmoji = "🇮🇳"
                ),
                Language(
                    code = "ta",
                    name = "Tamil",
                    nativeName = "தமிழ்",
                    flagEmoji = "🇮🇳"
                ),
                Language(
                    code = "te",
                    name = "Telugu",
                    nativeName = "తెలుగు",
                    flagEmoji = "🇮🇳"
                ),
                Language(
                    code = "kn",
                    name = "Kannada",
                    nativeName = "ಕನ್ನಡ",
                    flagEmoji = "🇮🇳"
                ),
                Language(
                    code = "mr",
                    name = "Marathi",
                    nativeName = "मराठी",
                    flagEmoji = "🇮🇳"
                )
            )
            _languages.value = languageList
            
            // Set currently selected language
            val currentLanguageCode = sharedPreference.getLanguage()
            val currentLanguage = languageList.find { it.code == currentLanguageCode }
            if (currentLanguage != null) {
                _selectedLanguage.value = currentLanguage
            }
        }
    }
    
    /**
     * Select a language.
     * @param language Language to select
     */
    fun selectLanguage(language: Language) {
        viewModelScope.launch {
            // Update selected language
            _selectedLanguage.value = language
            
            // Update languages list with selection state
            val updatedList = _languages.value?.map {
                it.copy(isSelected = it.code == language.code)
            }
            _languages.value = updatedList ?: emptyList()
            
            // Save to preferences
            sharedPreference.setLanguage(language.code)
        }
    }
    
    /**
     * Check if a language is selected.
     * @return True if language selected
     */
    fun isLanguageSelected(): Boolean {
        return _selectedLanguage.value != null
    }
}
