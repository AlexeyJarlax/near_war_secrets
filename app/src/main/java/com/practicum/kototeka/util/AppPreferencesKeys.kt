package com.practicum.kototeka.util
import android.content.Context

internal object AppPreferencesKeys { // Internal - доступно только в модуле
    // хранилища SharedPreferences
    const val PREFS_NAME = "MyPrefs"
    const val PREFS_HISTORY_NAME = "SearchHistory"

    // ключи и файлы
    const val KEY_INPUT_SHOWN_KEY = "key_input_shown"
    const val SETTINGS_REQUEST_CODE = 1 // первый запуск = запрос ключа шифрования сразу
    const val ENCRYPTION_KLUCHIK = "encription_kluchik" // ключ для стринги ключа
    const val KEY_EXIST_OF_ENCRYPTION_KLUCHIK = "exists_of_encryption_kluchik"

    const val KEY_HISTORY_LIST = "key_for_history_list"

    // числовые константы
    const val ALBUM_ROUNDED_CORNERS = 8
    const val SERVER_PROCESSING_MILLISECONDS: Long = 1500
    const val HISTORY_TRACK_LIST_SIZE = 7

    // переключатели в настройках
    const val KEY_NIGHT_MODE = "nightMode"
    const val KEY_USER_SWITCH = "userMode"
    const val KEY_USE_THE_ENCRYPTION_KLUCHIK = "useTheEncryptionKey"
    const val KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION = "deleteEKWhenClosingTheSession"
    const val KEY_PREVIEW_SIZE_SEEK_BAR = "previewSizeSeekBar"
    const val KEY_MIMICRY_SWITCH = "mimicry"
}

internal class AppPreferencesKeysMethods(private val context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)

    // Обработка методов сохранения и загрузки значений
    fun saveSwitchValue(key: String, isChecked: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, isChecked)
        editor.apply()
    }

    fun loadSwitchValue(key: String): Boolean {
        return sharedPreferences.getBoolean(
            key,
            false
        ) // Значение по умолчанию, если ключ не найден
    }
}


