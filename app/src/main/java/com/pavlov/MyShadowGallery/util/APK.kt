package com.pavlov.MyShadowGallery.util


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson

internal object APK { // AppPreferencesKey Internal - доступно только в модуле
    // хранилища SharedPreferences
    const val PREFS_NAME = "MyPrefs" // открытое хранилище
    const val PREFS_HISTORY_NAME = "SearchHistory" // история песен
    const val MY_SECRETS_PREFS_NAME = "secret_shared_prefs" // защищенное хранилище

    // SharedPreferences
    const val KEY_FIRST_RUN = "first_app_run" // первый запуск ?
    const val KEY_HISTORY_LIST = "key_for_history_list"
    const val DEFAULT_KEY = "default_key"
    const val KEY_EXIST_OF_PASSWORD = "parolchik"
    const val KEY_EXIST_OF_MIMICRY = "mimicry"
    const val KEY_EXIST_OF_ENCRYPTION_K = "exists_of_encryption_kluchik" // исключаем из кода
    const val KEY_USE_THE_ENCRYPTION_K = "useTheEncryptionKey"

    // ENCRYPTED SharedPreferences
    const val KEY_SMALL_SECRET = "my_secret"  // короткий секретик
    const val KEY_BIG_SECRET = "my_big_secret"  // длинный секретик
    const val KEY_BIG_SECRET_NAME1 = "my_big_secret_name_1"  // 1
    const val KEY_BIG_SECRET1 = "my_big_secret1"  // 1
    const val KEY_BIG_SECRET_NAME2 = "my_big_secret_name_2"  // 2
    const val KEY_BIG_SECRET2 = "my_big_secret2"  // 2
    const val KEY_BIG_SECRET_NAME3 = "my_big_secret_name_3"  // 3
    const val KEY_BIG_SECRET3 = "my_big_secret3"  // 3
    const val KEY_COUNT_TRY = "how_many_try_to_pass=30df"  // счетчик

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

    const val KEY_DELETE_AFTER_SESSION = "deleteEKWhenClosingTheSession"
    const val KEY_PREVIEW_SIZE_SEEK_BAR = "previewSizeSeekBar"
    const val PREF_LANGUAGE_KEY = "selected_language"
    const val FILE_NAME_KEY = "file_naming"

    //  в рамках сессии
    var KEY_MIMICRY_THIS_SESSION_SWITCH = false
    var EXCLAMATION: Boolean = false
}

// ------------------------------------------------------------------------------------ гетеры и сетеры
internal class APKM(private val context: Context) {
    private val sharedPreferences = getSharedPreferences()
    private fun getSharedPreferences() =
        context.getSharedPreferences(APK.PREFS_NAME, Context.MODE_PRIVATE)

    // ------------------------------------------------------------------------ Boolean гетеры и сетеры
    fun saveBooleanToSPK(key: String, isChecked: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, isChecked)
        editor.apply()
    } // APKM(context = this).saveBooleanToSPK(APK.KEY_NIGHT_MODE, false)

    fun getBooleanFromSPK(key: String): Boolean {
        return sharedPreferences.getBoolean(
            key,
            false
        )
    } // APKM(context = this).getBooleanFromSPK(APK.KEY_NIGHT_MODE)

    // ---------------------------------------------------------------------------- Int гетеры и сетеры
    fun saveIntToSP(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    } // APKM(context = savedContext).saveIntToSP(APK.DEFAULT_KEY, size)

    fun getIntFromSP(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    } //APKM(context).getIntFromSP(APK.DEFAULT_KEY)

    fun delFromSP(key: String) {
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }//APKM(context).delFromSP(APK.DEFAULT_KEY)

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
                APK.MY_SECRETS_PREFS_NAME,
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
            APK.MY_SECRETS_PREFS_NAME,
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
            APK.MY_SECRETS_PREFS_NAME,
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
    fun getCounter(key: String, default: Int): Int { // счетчик
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            APK.MY_SECRETS_PREFS_NAME,
            masterAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getInt(key, default)
    } // AppPreferencesKeysMethods(context = this).getCounter()

    fun saveCounter(key: String, counter: Int) {
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            APK.MY_SECRETS_PREFS_NAME,
            masterAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit {
            putInt(key, counter).apply()
//            toastIt("счетчик изменён")
        }
    } //    AppPreferencesKeysMethods(context = this).saveCounter(counter)

    fun countBigSecrets(): Int {
        var count = 3
        val secret1 = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET1)
        val secret2 = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET2)
        val secret3 = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET3)
        if (secret1.isNullOrBlank()) {
            count -= 1
        }
        if (secret2.isNullOrBlank()) {
            count -= 1
        }
        if (secret3.isNullOrBlank()) {
            count -= 1
        }
        return count
    }

    fun generateUniqueKeyName(): String {
        val name1 = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET_NAME1)
        val name2 = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET_NAME2)
        val name3 = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET_NAME3)
        val existingNames = setOf(name1, name2, name3)

        var index = 1
        var newKeyName = "key $index"

        while (existingNames.contains(newKeyName)) {
            index++
            newKeyName = "key $index"
        }

        return newKeyName
    }

    fun getDefauldKey(): String {
        val defaultKey: Int = APKM(context).getIntFromSP(APK.DEFAULT_KEY)
        if (defaultKey == 1) {
            return APKM(context).getMastersSecret(APK.KEY_BIG_SECRET1)
        } else if (defaultKey == 2) {
            return APKM(context).getMastersSecret(APK.KEY_BIG_SECRET2)
        } else if (defaultKey == 3) {
            return APKM(context).getMastersSecret(APK.KEY_BIG_SECRET3)
        } else {
            return ""
        }
    }

    fun getDefauldKeyName(): String {
        val defaultKey: Int = APKM(context).getIntFromSP(APK.DEFAULT_KEY)
        if (defaultKey == 1) {
            return APKM(context).getMastersSecret(APK.KEY_BIG_SECRET_NAME1)
        } else if (defaultKey == 2) {
            return APKM(context).getMastersSecret(APK.KEY_BIG_SECRET_NAME2)
        } else if (defaultKey == 3) {
            return APKM(context).getMastersSecret(APK.KEY_BIG_SECRET_NAME3)
        } else {
            return ""
        }
    }

    fun getKeyByNumber(int: Int): String {
        if (int == 1) {
            return APKM(context).getMastersSecret(APK.KEY_BIG_SECRET1)
        } else if (int == 2) {
            return APKM(context).getMastersSecret(APK.KEY_BIG_SECRET2)
        } else if (int == 3) {
            return APKM(context).getMastersSecret(APK.KEY_BIG_SECRET3)
        } else {
            return ""
        }
    }
}


