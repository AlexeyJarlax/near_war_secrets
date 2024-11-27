package com.pavlov.nearWarSecrets.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlov.nearWarSecrets.util.APK
import com.pavlov.nearWarSecrets.util.APKM
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

    private val _isDarkModeEnabled = MutableStateFlow(getDarkModePreference())
    val isDarkModeEnabled: StateFlow<Boolean> = _isDarkModeEnabled

    private val _isUserSwitchEnabled = MutableStateFlow(getUserSwitchPreference())
    val isUserSwitchEnabled: StateFlow<Boolean> = _isUserSwitchEnabled

    private val _previewSize = MutableStateFlow(getPreviewSizePreference())
    val previewSize: StateFlow<Int> = _previewSize

    private val _personalDataText = MutableStateFlow("Personal Data")
    val personalDataText: StateFlow<String> = _personalDataText

    private var clickCount = 0

    // Dark Mode
    fun toggleDarkMode(enabled: Boolean) {
        _isDarkModeEnabled.value = enabled
        saveDarkModePreference(enabled)
    }

    // User Switch
    fun toggleUserSwitch(enabled: Boolean) {
        _isUserSwitchEnabled.value = enabled
        saveUserSwitchPreference(enabled)
    }

    // Preview Size
    fun updatePreviewSize(size: Int) {
        _previewSize.value = size
        savePreviewSizePreference(size)
    }

    // Personal Data Toggle
    fun togglePersonalData() {
        clickCount++
        _personalDataText.value = if (clickCount % 2 == 0) {
            "Sensitive Info"
        } else {
            "Personal Data"
        }
    }

    // Clear Storage
    fun clearStorage() {
        viewModelScope.launch {
            apkm.clearStorage(context)
        }
    }

    // Reset Settings
    fun resetSettings() {
        viewModelScope.launch {
            apkm.clearPreferences(context)
            apkm.clearStorage(context)
            resetState()
        }
    }

    // Reset State to Defaults
    private fun resetState() {
        _isDarkModeEnabled.value = false
        _isUserSwitchEnabled.value = false
        _previewSize.value = APK.DEFAULT_PREVIEW_SIZE
        _personalDataText.value = "Personal Data"
    }

    // SharedPreferences Helpers
    private fun getDarkModePreference(): Boolean {
        return apkm.getBoolean(APK.KEY_DARK_MODE, false)
    }

    private fun saveDarkModePreference(enabled: Boolean) {
        apkm.putBoolean(APK.KEY_DARK_MODE, enabled)
    }

    private fun getUserSwitchPreference(): Boolean {
        return apkm.getBoolean(APK.KEY_USER_SWITCH, false)
    }

    private fun saveUserSwitchPreference(enabled: Boolean) {
        apkm.putBoolean(APK.KEY_USER_SWITCH, enabled)
    }

    private fun getPreviewSizePreference(): Int {
        return apkm.getInt(APK.KEY_PREVIEW_SIZE_SEEK_BAR)
    }

    private fun savePreviewSizePreference(size: Int) {
        apkm.putInt(APK.KEY_PREVIEW_SIZE_SEEK_BAR, size)
    }
}