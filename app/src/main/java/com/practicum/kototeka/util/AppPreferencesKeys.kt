package com.practicum.kototeka.util

internal object AppPreferencesKeys { // Internal - доступно только в модуле
    // ключи и файлы в хранилище
    const val PREFS_NAME = "MyPrefs"
    const val KEY_INPUT_SHOWN_KEY = "key_input_shown"
    const val KEY_NIGHT_MODE = "nightMode"
    const val USER_SWITCH = "userMode"
    const val SETTINGS_REQUEST_CODE = 1 // первый запуск = запрос ключа шифрования сразу
    const val PREFS_HISTORY_NAME = "SearchHistory"
    const val KEY_HISTORY_LIST = "key_for_history_list"
    // числовые константы
    const val ALBUM_ROUNDED_CORNERS = 8
    const val SERVER_PROCESSING_MILLISECONDS: Long = 1500
    const val HISTORY_TRACK_LIST_SIZE = 10
}
