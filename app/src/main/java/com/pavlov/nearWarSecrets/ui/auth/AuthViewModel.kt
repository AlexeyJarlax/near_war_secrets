package com.pavlov.nearWarSecrets.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlov.nearWarSecrets.util.APK
import com.pavlov.nearWarSecrets.util.APKM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apkm: APKM
) : ViewModel() {

    private val _delPassword = MutableStateFlow(false)
    val delPassword: StateFlow<Boolean> = _delPassword

    private val _counter = MutableStateFlow(30)
    val counter: StateFlow<Int> = _counter

    private val _isPasswordExist = MutableStateFlow(true)
    val isPasswordExist: StateFlow<Boolean> = _isPasswordExist

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    // Перенесите объявление навигационных переменных перед блоком init
    private val _navigateToMain = MutableStateFlow(false)
    val navigateToMain: StateFlow<Boolean> = _navigateToMain

    private val _navigateToTwoStepsForSave = MutableStateFlow(false)
    val navigateToTwoStepsForSave: StateFlow<Boolean> = _navigateToTwoStepsForSave

    init {
        firstStart()
    }

    private fun firstStart() {
        if (apkm.getBooleanFromSPK(APK.KEY_FIRST_RUN, true)) {
            apkm.saveIntToSP(APK.KEY_PREVIEW_SIZE_SEEK_BAR, 30)
            apkm.saveBooleanToSPK(APK.KEY_FIRST_RUN, false)
            // Переход на экран TwoStepsForSaveScreen
            _navigateToTwoStepsForSave.value = true
        } else {
            val savedPassword = apkm.getMastersSecret(APK.KEY_SMALL_SECRET)
            if (savedPassword.isBlank()) {
                _isPasswordExist.value = false
                // Переход на главный экран
                _navigateToMain.value = true
            }
        }
    }

    fun onPasswordEntered(password: String) {
        val savedPassword = apkm.getMastersSecret(APK.KEY_SMALL_SECRET)
        if (savedPassword == password.trim()) {
            _counter.value = 30
            apkm.saveCounter(APK.KEY_COUNT_TRY, _counter.value)
            if (_delPassword.value) {
                doDelPassword()
                // Navigate to TwoStepsForSaveScreen
                _navigateToTwoStepsForSave.value = true
            } else {
                // Navigate to MainScreen
                _navigateToMain.value = true
            }
        } else {
            _counter.value -= 1
            apkm.saveCounter(APK.KEY_COUNT_TRY, _counter.value)
            _message.value = "Неверный пароль"

            if (_counter.value < 27) {
                _loading.value = true
                viewModelScope.launch {
                    delay(2000)
                    _loading.value = false
                }
            }
            if (_counter.value < 1) {
                // Clear storage and reset settings
                // Close the app
            }
        }
    }

    private fun doDelPassword() {
        apkm.delMastersSecret(APK.KEY_SMALL_SECRET)
        apkm.saveBooleanToSPK(APK.KEY_EXIST_OF_PASSWORD, false)
    }
}