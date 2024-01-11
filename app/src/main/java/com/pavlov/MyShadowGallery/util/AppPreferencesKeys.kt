package com.pavlov.MyShadowGallery.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

internal object AppPreferencesKeys { // Internal - доступно только в модуле
    // хранилища SharedPreferences
    const val PREFS_NAME = "MyPrefs" // открытое хранилище
    const val PREFS_HISTORY_NAME = "SearchHistory" // история песен
    const val MY_SECRETS_PREFS_NAME = "secret_shared_prefs" // защищенное хранилище

    // ключи и файлы
    const val KEY_FIRST_RUN = "first_app_run" // первый запуск ?
    const val ENCRYPTION_KLUCHIK = "encription_kluchik" // ключ для стринги ключа
    const val KEY_HISTORY_LIST = "key_for_history_list"
    const val KEY_SMALL_SECRET = "my_secret"  // короткий секретик
    const val KEY_BIG_SECRET = "my_big_secret"  // длинный секретик
    const val KEY_COUNT_TRY = "my_big_secret"  // счетчик

    // ключи к статусу трех шагов
    const val KEY_EXIST_OF_PASSWORD = "parolchik"
    const val KEY_EXIST_OF_MIMICRY = "mimicry"
    const val KEY_EXIST_OF_ENCRYPTION_KLUCHIK = "exists_of_encryption_kluchik"

    // числовые константы
    const val ALBUM_ROUNDED_CORNERS = 8
    const val SERVER_PROCESSING_MILLISECONDS: Long = 1500
    const val LOAD_PROCESSING_MILLISECONDS: Long = 800
    const val HISTORY_TRACK_LIST_SIZE = 8
    const val DEFAULT_PREVIEW_SIZE = 30
    const val DEFAULT_MIMIC_PASS: String = "000"

    // переключатели состояний SharedPreferences
    const val KEY_NIGHT_MODE = "nightMode"
    const val KEY_USER_SWITCH = "userMode"
    const val KEY_USE_THE_ENCRYPTION_KLUCHIK = "useTheEncryptionKey"
    const val KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION = "deleteEKWhenClosingTheSession"
    const val KEY_PREVIEW_SIZE_SEEK_BAR = "previewSizeSeekBar"

    const val APP_LANGUAGE = "appLanguage"

    //  константы в рамках сессии
    var KEY_MIMICRY_THIS_SESSION_SWITCH = false
}

internal class AppPreferencesKeysMethods(private val context: Context) {

    private val sharedPreferences = getSharedPreferences()

    //    var internalContext = context
    private fun getSharedPreferences() =
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

    fun savePreviewSizeValue(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun loadPreviewSizeValue(key: String): Int {
        return sharedPreferences.getInt(
            key,
            0
        ) // Значение по умолчанию, если ключ не найден
    }

    fun loadStringFromSharedPreferences(key: String): String {
        return sharedPreferences.getString(key, "упс...ах") ?: "упс...ах"
    }

    fun getMastersSecret(key: String): String? {
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val encryptedSharedPreferences: SharedPreferences =
            EncryptedSharedPreferences.create(
                AppPreferencesKeys.MY_SECRETS_PREFS_NAME,
                masterAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        return encryptedSharedPreferences.getString(key, "")
    }

    fun saveMastersSecret(secret: String, key: String) {
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val encryptedSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            AppPreferencesKeys.MY_SECRETS_PREFS_NAME,
            masterAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        encryptedSharedPreferences.edit {
            putString(key, secret).apply()
        }
    }

    fun delMastersSecret(key: String) {
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val encryptedSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            AppPreferencesKeys.MY_SECRETS_PREFS_NAME,
            masterAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        encryptedSharedPreferences.edit {
            remove(key)
            apply()
        }
    }

    fun getCounter(): Int { // счетчик
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            AppPreferencesKeys.MY_SECRETS_PREFS_NAME,
            masterAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getInt(AppPreferencesKeys.KEY_COUNT_TRY, 30)
    }

    fun saveCounter(counter: Int) {
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            AppPreferencesKeys.MY_SECRETS_PREFS_NAME,
            masterAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit {
            putInt(AppPreferencesKeys.KEY_COUNT_TRY, counter).apply()
//            toastIt("счетчик изменён")
        }
    }

}


