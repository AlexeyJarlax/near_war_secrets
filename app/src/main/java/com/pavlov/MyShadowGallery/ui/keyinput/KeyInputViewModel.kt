package com.pavlov.MyShadowGallery.ui.keyinput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.util.APKM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class KeyInputViewModel @Inject constructor(
    private val apkm: APKM
) : ViewModel() {

    val keyName = MutableStateFlow(apkm.generateUniqueKeyName())

    private val _keyValue = MutableStateFlow("")
    val keyValue: StateFlow<String> = _keyValue

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    private val _isKeyValid = MutableStateFlow(false)
    val isKeyValid: StateFlow<Boolean> = _isKeyValid

    private val _navigateToMain = MutableStateFlow(false)
    val navigateToMain: StateFlow<Boolean> = _navigateToMain

    val _showNewKeyFields = MutableStateFlow(true)
    val showNewKeyFields: StateFlow<Boolean> = _showNewKeyFields

    val _showOldKeys = MutableStateFlow(false)
    val showOldKeys: StateFlow<Boolean> = _showOldKeys

    val oldKeys = MutableStateFlow<List<Pair<String, Int>>>(emptyList())

    init {
        if (apkm.countBigSecrets() > 0) {
            _showNewKeyFields.value = false
            _showOldKeys.value = true
            loadOldKeys()
        }
    }

    private fun loadOldKeys() {
        val keys = mutableListOf<Pair<String, Int>>()
        apkm.getMastersSecret(APK.KEY_BIG_SECRET_NAME1).takeIf { it.isNotBlank() }?.let {
            keys.add(it to 1)
        }
        apkm.getMastersSecret(APK.KEY_BIG_SECRET_NAME2).takeIf { it.isNotBlank() }?.let {
            keys.add(it to 2)
        }
        apkm.getMastersSecret(APK.KEY_BIG_SECRET_NAME3).takeIf { it.isNotBlank() }?.let {
            keys.add(it to 3)
        }
        oldKeys.value = keys
    }

    fun onKeyValueChange(value: String) {
        _keyValue.value = value
        validateKey(value)
    }

    private fun validateKey(value: String) {
        val byteCount = value.toByteArray(Charsets.UTF_8).size
        val remainingBytes = 16 - byteCount
        _isKeyValid.value = remainingBytes >= 0 && !value.contains(" ")
        _message.value = when {
            remainingBytes > 0 -> "Осталось байт: $remainingBytes"
            remainingBytes == 0 -> ""
            else -> "Превышен размер ключа"
        }
    }

    fun generateRandomKey() {
        val chars = ('a'..'f') + ('0'..'9')
        val randomKey = (1..16)
            .map { chars.random() }
            .joinToString("")
        _keyValue.value = randomKey
        validateKey(randomKey)
    }

    fun saveKey() {
        if (_isKeyValid.value && _keyValue.value.isNotEmpty()) {
            val keyValue = _keyValue.value
            val nameKeyValue = keyName.value
            confirm(keyValue, nameKeyValue)
        } else {
            _message.value = "Введите корректный ключ"
        }
    }

    private fun confirm(keyValue: String, nameKeyValue: String) {
        apkm.saveBooleanToSPK(APK.KEY_USE_THE_ENCRYPTION_K, true)
        if (keyValue.isNotEmpty()) {
            val secret1 = apkm.getMastersSecret(APK.KEY_BIG_SECRET1)
            val secret2 = apkm.getMastersSecret(APK.KEY_BIG_SECRET2)
            val secret3 = apkm.getMastersSecret(APK.KEY_BIG_SECRET3)
            when {
                secret1.isNullOrBlank() -> {
                    apkm.saveMastersSecret(nameKeyValue, APK.KEY_BIG_SECRET_NAME1)
                    apkm.saveMastersSecret(keyValue, APK.KEY_BIG_SECRET1)
                    apkm.saveIntToSP(APK.DEFAULT_KEY, 1)
                }
                secret2.isNullOrBlank() -> {
                    apkm.saveMastersSecret(nameKeyValue, APK.KEY_BIG_SECRET_NAME2)
                    apkm.saveMastersSecret(keyValue, APK.KEY_BIG_SECRET2)
                    apkm.saveIntToSP(APK.DEFAULT_KEY, 2)
                }
                secret3.isNullOrBlank() -> {
                    apkm.saveMastersSecret(nameKeyValue, APK.KEY_BIG_SECRET_NAME3)
                    apkm.saveMastersSecret(keyValue, APK.KEY_BIG_SECRET3)
                    apkm.saveIntToSP(APK.DEFAULT_KEY, 3)
                }
                else -> {
                    _message.value = "Нет доступных слотов для ключей"
                }
            }
            apkm.saveBooleanToSPK(APK.KEY_EXIST_OF_ENCRYPTION_K, true)
            apkm.saveBooleanToSPK(APK.KEY_USE_THE_ENCRYPTION_K, true)
            _navigateToMain.value = true
        } else {
            _message.value = "Ключ не задан"
        }
    }

    fun onDoNotUseKey() {
        apkm.saveBooleanToSPK(APK.KEY_USE_THE_ENCRYPTION_K, false)
        apkm.delFromSP(APK.DEFAULT_KEY)
        _navigateToMain.value = true
    }

    fun deleteKey(keyNumber: Int) {
        when (keyNumber) {
            1 -> {
                apkm.delMastersSecret(APK.KEY_BIG_SECRET_NAME1)
                apkm.delMastersSecret(APK.KEY_BIG_SECRET1)
            }
            2 -> {
                apkm.delMastersSecret(APK.KEY_BIG_SECRET_NAME2)
                apkm.delMastersSecret(APK.KEY_BIG_SECRET2)
            }
            3 -> {
                apkm.delMastersSecret(APK.KEY_BIG_SECRET_NAME3)
                apkm.delMastersSecret(APK.KEY_BIG_SECRET3)
            }
        }
        loadOldKeys()
    }

    fun selectOldKey(keyNumber: Int) {
        apkm.saveIntToSP(APK.DEFAULT_KEY, keyNumber)
        apkm.saveBooleanToSPK(APK.KEY_EXIST_OF_ENCRYPTION_K, true)
        apkm.saveBooleanToSPK(APK.KEY_USE_THE_ENCRYPTION_K, true)
        _navigateToMain.value = true
    }

    // Дополнительные методы из оригинального кода
}