package com.practicum.kototeka.util

internal class MyCompObj { // Internal - доступно только в модуле

    companion object {
        const val PREFS_NAME = "MyPrefs"
        const val KEY_INPUT_SHOWN_KEY = "key_input_shown"
        const val KEY_NIGHT_MODE = "nightMode"
        const val USER_SWITCH = "userMode"
        const val SETTINGS_REQUEST_CODE = 1 // первый запуск = запрос ключа шифрования сразу
    }
}