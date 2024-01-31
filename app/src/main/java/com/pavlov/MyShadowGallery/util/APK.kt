package com.pavlov.MyShadowGallery.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson

internal object APK { // AppPreferencesKey Internal - доступно только в модуле
    // хранилища SharedPreferences
    const val PREFS_NAME = "my_prefs_new" // открытое хранилище
    const val MY_SECRETS_PREFS_NAME = "secret_shared_prefs_new" // защищенное хранилище

    // SharedPreferences
    const val KEY_FIRST_RUN = "first_app_run_new" // первый запуск ?
    const val KEY_HISTORY_LIST = "key_for_history_list_new"
    const val DEFAULT_KEY = "default_key_new"
    const val KEY_EXIST_OF_PASSWORD = "parolchik_new"
    const val KEY_EXIST_OF_MIMICRY = "mimicry_new"
    const val KEY_EXIST_OF_ENCRYPTION_K = "exists_of_encryption_kluchik_new" // исключаем из кода
    const val KEY_USE_THE_ENCRYPTION_K = "use_the_encryption_key_new"

    // ENCRYPTED SharedPreferences
    const val KEY_SMALL_SECRET = "my_secret_new"  // короткий секретик
    const val KEY_BIG_SECRET_NAME1 = "my_big_secret_name_1_new"  // 1
    const val KEY_BIG_SECRET1 = "my_big_secret1_new"  // 1
    const val KEY_BIG_SECRET_NAME2 = "my_big_secret_name_2_new"  // 2
    const val KEY_BIG_SECRET2 = "my_big_secret2_new"  // 2
    const val KEY_BIG_SECRET_NAME3 = "my_big_secret_name_3_new"  // 3
    const val KEY_BIG_SECRET3 = "my_big_secret3_new"  // 3
    const val KEY_COUNT_TRY = "how_many_try_to_pass=30df_new"  // счетчик

    // константы
    const val ALBUM_ROUNDED_CORNERS = 8
    const val SERVER_PROCESSING_MILLISECONDS: Long = 1500
    const val LOAD_PROCESSING_MILLISECONDS: Long = 800
    const val HISTORY_TRACK_LIST_SIZE = 25
    const val DEFAULT_PREVIEW_SIZE = 30
    const val DEFAULT_MIMIC_PASS: String = "000"
    
    const val REGEX = "[a-zA-Z0-9.,!?@#\$%^&*()_+-=:;<>{}\\[\\]\"'\\\\/]+"


    // переключатели состояний SharedPreferences
    const val KEY_NIGHT_MODE = "night_mode_new"
    const val KEY_USER_SWITCH = "user_mode_new"

    const val KEY_DELETE_AFTER_SESSION = "delete_ek_when_closing_new"
    const val KEY_PREVIEW_SIZE_SEEK_BAR = "seek_bar_new"
    const val PREF_LANGUAGE_KEY = "selected_language_new"
    const val FILE_NAME_KEY = "file_naming_new"

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

    fun getBooleanFromSPK(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(
            key,
            default
        )
    } // APKM(context = this).getBooleanFromSPK(APK.KEY_NIGHT_MODE, false)

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
        val masterKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedSharedPreferences: SharedPreferences =
            EncryptedSharedPreferences.create(
                context,
                APK.MY_SECRETS_PREFS_NAME,
                masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        return encryptedSharedPreferences.getString(key, "") ?: ""
    }

    fun saveMastersSecret(secret: String, key: String) {
        val masterKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            context,
            APK.MY_SECRETS_PREFS_NAME,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        encryptedSharedPreferences.edit {
            putString(key, secret).apply()
        }
    } //    AppPreferencesKeysMethods(context = this).saveMastersSecret(keyValue, AppPreferencesKeys.KEY_BIG_SECRET)

    fun delMastersSecret(key: String) {
        val masterKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            context,
            APK.MY_SECRETS_PREFS_NAME,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        encryptedSharedPreferences.edit {
            remove(key)
            apply()
        }
    } //    AppPreferencesKeysMethods(context = this).delMastersSecret(AppPreferencesKeys.KEY_BIG_SECRET)

    // -------------------------------------------------------------- encrypted Int гетеры и сетеры
    fun getCounter(key: String, default: Int): Int {
        val masterKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            context,
            APK.MY_SECRETS_PREFS_NAME,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getInt(key, default)
    } // AppPreferencesKeysMethods(context = this).getCounter()

    fun saveCounter(key: String, counter: Int) {
        val masterKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            context,
            APK.MY_SECRETS_PREFS_NAME,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit {
            putInt(key, counter).apply()
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


