package com.pavlov.MyShadowGallery.util



import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson

internal object AppPreferencesKeys { // Internal - доступно только в модуле
    // хранилища SharedPreferences
    const val PREFS_NAME = "MyPrefs" // открытое хранилище
    const val PREFS_HISTORY_NAME = "SearchHistory" // история песен
    const val MY_SECRETS_PREFS_NAME = "secret_shared_prefs" // защищенное хранилище

    // ключи и файлы
    const val KEY_FIRST_RUN = "first_app_run" // первый запуск ?

    //    const val ENCRYPTION_KLUCHIK = "encription_kluchik" // ключ для стринги ключа
    const val KEY_HISTORY_LIST = "key_for_history_list"
    const val KEY_SMALL_SECRET = "my_secret"  // короткий секретик
    const val KEY_BIG_SECRET = "my_big_secret"  // длинный секретик
    const val KEY_COUNT_TRY = "my_big_secret"  // счетчик

    // ключи к статусу трех шагов
    const val KEY_EXIST_OF_PASSWORD = "parolchik"
    const val KEY_EXIST_OF_MIMICRY = "mimicry"
    const val KEY_EXIST_OF_ENCRYPTION_K = "exists_of_encryption_kluchik"

    // константы
    const val ALBUM_ROUNDED_CORNERS = 8
    const val SERVER_PROCESSING_MILLISECONDS: Long = 1500
    const val LOAD_PROCESSING_MILLISECONDS: Long = 800
    const val HISTORY_TRACK_LIST_SIZE = 8
    const val DEFAULT_PREVIEW_SIZE = 30
    const val DEFAULT_MIMIC_PASS: String = "000"

    //    const val REGEX = "[a-zA-Zа-яА-ЯñÑáéíóúüÜ0-9.,!?@#\$%^&*()_+-=:;<>{}\\[\\]\"'\\\\/\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}]+"
    const val REGEX = "[a-zA-Z0-9.,!?@#\$%^&*()_+-=:;<>{}\\[\\]\"'\\\\/]+"


    // переключатели состояний SharedPreferences
    const val KEY_NIGHT_MODE = "nightMode"
    const val KEY_USER_SWITCH = "userMode"
    const val KEY_USE_THE_ENCRYPTION_K = "useTheEncryptionKey"
    const val KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION = "deleteEKWhenClosingTheSession"
    const val KEY_PREVIEW_SIZE_SEEK_BAR = "previewSizeSeekBar"
    const val PREF_LANGUAGE_KEY = "selected_language"
    const val FILE_NAME_KEY = "file_naming"

    //  в рамках сессии
    var KEY_MIMICRY_THIS_SESSION_SWITCH = false
    var EXCLAMATION: Boolean = false
}

// ------------------------------------------------------------------------------------ гетеры и сетеры
internal class AppPreferencesKeysMethods(private val context: Context) {
    private val sharedPreferences = getSharedPreferences()
    private fun getSharedPreferences() =
        context.getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)

    // ------------------------------------------------------------------------ Boolean гетеры и сетеры
    fun saveBooleanToSharedPreferences(key: String, isChecked: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, isChecked)
        editor.apply()
    } // AppPreferencesKeysMethods(context = this).saveBooleanToSharedPreferences(AppPreferencesKeys.KEY_NIGHT_MODE, isChecked)

    fun getBooleanFromSharedPreferences(key: String): Boolean {
        return sharedPreferences.getBoolean(
            key,
            false
        )
    } // AppPreferencesKeysMethods(context = this).getBooleanFromSharedPreferences(AppPreferencesKeys.KEY_NIGHT_MODE)

    // ---------------------------------------------------------------------------- Int гетеры и сетеры
    fun saveIntToSharedPreferences(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    } // AppPreferencesKeysMethods(context = savedContext).saveIntToSharedPreferences(AppPreferencesKeys.KEY_PREVIEW_SIZE_SEEK_BAR, size)

    fun getIntFromSharedPreferences(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    } //AppPreferencesKeysMethods(context).getIntFromSharedPreferences(AppPreferencesKeys.FILE_NAME_KEY)

    // ------------------------------------------------------------------------- String гетеры и сетеры
    fun getStringFromSharedPreferences(key: String): String {
        return sharedPreferences.getString(key, "упс...ах") ?: "упс...ах"
    }// AppPreferencesKeysMethods(context).getObjectFromSharedPreferences(AppPreferencesKeys.KEY_HISTORY_LIST)

    fun saveStringToSharedPreferences(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }// AppPreferencesKeysMethods(context).saveStringToSharedPreferences(AppPreferencesKeys.KEY_HISTORY_LIST, jsonString)

    fun delStringFromSharedPreferences(key: String) {
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }// appPreferencesMethods.delStringFromSharedPreferences(AppPreferencesKeys.KEY_HISTORY_LIST)

    // -------------------------------------------------------------- json Object гетеры и сетеры
    inline fun <reified T> getObjectFromSharedPreferences(key: String): T? {
        val json = sharedPreferences.getString(key, null)
        return Gson().fromJson(json, T::class.java)
    }// appPreferencesMethods.getObjectFromSharedPreferences<String>(AppPreferencesKeys.KEY_HISTORY_LIST)

    inline fun <reified T> saveObjectToSharedPreferences(key: String, value: T) {
        val json = Gson().toJson(value)
        val editor = sharedPreferences.edit()
        editor.putString(key, json)
        editor.apply()
    }// appPreferencesMethods.saveObjectToSharedPreferences(AppPreferencesKeys.KEY_HISTORY_LIST, jsonString)

// удаляется объект как простая стринга

    // -------------------------------------------------------------- encrypted String гетеры и сетеры
    fun getMastersSecret(key: String): String {
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val encryptedSharedPreferences: SharedPreferences =
            EncryptedSharedPreferences.create(
                AppPreferencesKeys.MY_SECRETS_PREFS_NAME,
                masterAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        return encryptedSharedPreferences.getString(key, "") ?: ""
    } //    AppPreferencesKeysMethods(context = this).getMastersSecret(AppPreferencesKeys.KEY_BIG_SECRET)

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
    } //    AppPreferencesKeysMethods(context = this).saveMastersSecret(keyValue, AppPreferencesKeys.KEY_BIG_SECRET)

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
    } //    AppPreferencesKeysMethods(context = this).delMastersSecret(AppPreferencesKeys.KEY_BIG_SECRET)

    // -------------------------------------------------------------- encrypted Int гетеры и сетеры
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
    } // AppPreferencesKeysMethods(context = this).getCounter()

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
    } //    AppPreferencesKeysMethods(context = this).saveCounter(counter)

}


