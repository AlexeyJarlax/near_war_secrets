package com.pavlov.nearWarSecrets.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlov.nearWarSecrets.util.APKM
import com.pavlov.nearWarSecrets.util.ToastExt
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apkm: APKM
) : ViewModel() {

    private val _personalDataText = MutableStateFlow("Personal Data")
    val personalDataText: StateFlow<String> = _personalDataText

    private val _language = MutableStateFlow("en") // Значение по умолчанию
    val language: StateFlow<String> = _language

    private var clickCount = 0

    // Переключение личных данных
    fun togglePersonalData() {
        clickCount++
        _personalDataText.value = if (clickCount % 2 == 0) {
            "Sensitive Info"
        } else {
            "Personal Data"
        }
    }

    // Очистка хранилища
    fun clearStorage() {
        viewModelScope.launch {
            apkm.clearStorage(context)
        }
    }

    // Сброс настроек
    fun resetSettings() {
        viewModelScope.launch {
            apkm.clearPreferences(context)
            apkm.clearStorage(context)
            resetState()
        }
    }

    private fun resetState() {
        _personalDataText.value = "Personal Data"
        _language.value = "en" // Сброс языка на английский
    }

    // Установка языка
    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            ToastExt.show("Язык поменяется при следующем запуске")
            _language.value = languageCode
        }
    }
}
