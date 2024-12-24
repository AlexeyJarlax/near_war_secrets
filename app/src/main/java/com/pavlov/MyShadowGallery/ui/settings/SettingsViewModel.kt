package com.pavlov.MyShadowGallery.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.util.APKM
import com.pavlov.MyShadowGallery.util.ToastExt
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

    private val _personalDataText = MutableStateFlow(context.getString(R.string.personal_data))
    val personalDataText: StateFlow<String> = _personalDataText

    private val _language = MutableStateFlow("en") // Значение по умолчанию
    val language: StateFlow<String> = _language

    private var clickCount = 0

    fun clearStorage() {
        viewModelScope.launch {
            try {
                apkm.clearStorage(context)
                ToastExt.show(context.getString(R.string.storage_cleared))
            } catch (e: Exception) {
                ToastExt.show(context.getString(R.string.storage_clear_error))
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
                ToastExt.show(context.getString(R.string.settings_reset_success))
            } catch (e: Exception) {
                e.printStackTrace()
                ToastExt.show(context.getString(R.string.settings_reset_error))
            }
        }
    }

    private fun resetState() {
        _personalDataText.value = context.getString(R.string.personal_data)
        _language.value = "en" // Сброс языка на английский
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            ToastExt.show(context.getString(R.string.language_change_notice))
            _language.value = languageCode
        }
    }
}