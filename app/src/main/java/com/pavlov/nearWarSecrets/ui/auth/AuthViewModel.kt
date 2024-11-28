package com.pavlov.nearWarSecrets.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlov.nearWarSecrets.ui.settings.SettingsViewModel
import com.pavlov.nearWarSecrets.util.APK
import com.pavlov.nearWarSecrets.util.APKM
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bouncycastle.crypto.params.Blake3Parameters.context
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apkm: APKM,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError

    private val _counter = MutableStateFlow(10)
    val counter: StateFlow<Int> = _counter

    private val _delPassword = MutableStateFlow(false)
    val delPassword: StateFlow<Boolean> = _delPassword

    private val _isPasswordExist = MutableStateFlow(true)
    val isPasswordExist: StateFlow<Boolean> = _isPasswordExist

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    // Перенесите объявление навигационных переменных перед блоком init
    private val _navigateToItemLoader = MutableStateFlow(false)
    val navigateToItemLoader: StateFlow<Boolean> = _navigateToItemLoader

    private val _navigateToTwoStepsForSave = MutableStateFlow(false)
    val navigateToTwoStepsForSave: StateFlow<Boolean> = _navigateToTwoStepsForSave

    init {
        firstStart()
    }

    private fun firstStart() {
        if (apkm.getBoolean(APK.KEY_FIRST_RUN, true)) {
            apkm.putInt(APK.KEY_PREVIEW_SIZE_SEEK_BAR, 30)
            apkm.putBoolean(APK.KEY_FIRST_RUN, false)
            // Переход на экран TwoStepsForSaveScreen
            _navigateToTwoStepsForSave.value = true
        } else {
            val savedPassword = apkm.getMastersSecret(APK.KEY_SMALL_SECRET)
            if (savedPassword.isBlank()) {
                _isPasswordExist.value = false
                // Переход на главный экран
                _navigateToItemLoader.value = true
            }
        }
    }

    fun onPasswordEntered(password: String) {
        _loading.value = true
        viewModelScope.launch {
            delay(2000)
            _loading.value = false
        }
        val savedPassword = apkm.getMastersSecret(APK.KEY_SMALL_SECRET)
        if (savedPassword == password.trim()) {
            _counter.value = 10
            apkm.saveCounter(APK.KEY_COUNT_TRY, _counter.value)
            if (_delPassword.value) {
                doDelPassword()
                // Navigate to TwoStepsForSaveScreen
                _navigateToTwoStepsForSave.value = true
            } else {
                _message.value = "Успешный вход"
                _navigateToItemLoader.value = true
            }
        } else {
            _counter.value -= 1
            _hasError.value = true
            apkm.saveCounter(APK.KEY_COUNT_TRY, _counter.value)
            _message.value = "Неверный пароль"

            if (_counter.value < 27) {
            }
            if (_counter.value < 1) {
                resetSettings()
            }
        }
    }

    private fun resetSettings() {
        viewModelScope.launch {
            apkm.clearPreferences(context)
            apkm.clearStorage(context)
            resetState()
        }
    }

    // Reset State to Defaults
    private fun resetState() {
//        _isUserSwitchEnabled.value = false
//        _previewSize.value = APK.DEFAULT_PREVIEW_SIZE
//        _personalDataText.value = "Personal Data"
    }

    private fun doDelPassword() {
        apkm.delMastersSecret(APK.KEY_SMALL_SECRET)
        apkm.putBoolean(APK.KEY_EXIST_OF_PASSWORD, false)
    }
}