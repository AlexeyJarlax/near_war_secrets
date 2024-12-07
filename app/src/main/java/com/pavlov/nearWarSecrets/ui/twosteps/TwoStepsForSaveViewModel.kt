package com.pavlov.nearWarSecrets.ui.twosteps

import androidx.lifecycle.ViewModel
import com.pavlov.nearWarSecrets.util.APK
import com.pavlov.nearWarSecrets.util.APKM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TwoStepsForSaveViewModel @Inject constructor(
    private val apkm: APKM
) : ViewModel() {

    private val _step = MutableStateFlow(0)
    val step: StateFlow<Int> = _step

    private val _navigateToSetPassword = MutableStateFlow(false)
    val navigateToSetPassword: StateFlow<Boolean> = _navigateToSetPassword

    private val _navigateToMain = MutableStateFlow(false)
    val navigateToMain: StateFlow<Boolean> = _navigateToMain

    private val _navigateToKeyInput = MutableStateFlow(false)
    val navigateToKeyInput: StateFlow<Boolean> = _navigateToKeyInput

    init {
//        val isPasswordExists = apkm.getMastersSecret(APK.KEY_SMALL_SECRET).isNotBlank()
//        _step.value = if (isPasswordExists) 3 else 0
    }

    fun onNextButtonClicked() {
        _step.value = 1
    }

    fun onYesClicked() {
        when (_step.value) {
            1 -> {
                _navigateToSetPassword.value = true
            }
//            3 -> {
//                _navigateToKeyInput.value = true
//            }
        }
    }

    fun onNoClicked() {
        when (_step.value) {
            1 -> {
//                _step.value = 3
                _navigateToMain.value = true
            }
//            3 -> {
//                apkm.delFromSP(APK.DEFAULT_KEY)
//                apkm.putBoolean(APK.KEY_USE_THE_ENCRYPTION_K, false)
//                _navigateToMain.value = true
//            }
        }
    }
}