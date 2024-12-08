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

    fun clearStorage() {
        viewModelScope.launch {
            try {
            apkm.clearStorage(context)
            ToastExt.show("Хранилище отчищено!")
            } catch (e: Exception) {
                ToastExt.show("Ошибка при отчистке хранилища")
            }
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            try {
                apkm.clearPreferences(context)
                apkm.clearEncryptedPreferences(context)
                apkm.clearStorage(context)
                context.cacheDir.deleteRecursively()
                resetState()
                ToastExt.show("Настройки сброшены успешно!")
            } catch (e: Exception) {
                e.printStackTrace()
                ToastExt.show("Ошибка при сбросе настроек.")
            }
        }
    }

    private fun resetState() {
        _personalDataText.value = "Personal Data"
        _language.value = "en" // Сброс языка на английский
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            ToastExt.show("Язык поменяется при следующем запуске")
            _language.value = languageCode
        }
    }
}
