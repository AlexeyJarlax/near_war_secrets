package com.pavlov.MyShadowGallery.util

import android.content.Context
import android.provider.Settings.Secure.putString
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.random.Random

class PasswordManager(context: Context) {

    private val KEY_ALIAS = "MyShadowGalleryKey"
    private val IV_LENGTH = 12
    private val passwordKey = "password"
    private val passwordKeyWithIV = passwordKey + "_IV"
    private val preferences = EncryptedSharedPreferences.create(
        "secure_prefs",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun hasPassword(): Boolean {
        return preferences.contains(passwordKey) && preferences.contains(passwordKeyWithIV)
    }

    fun savePassword(password: String, context: Context) {
        val encryptedPassword = encryptPassword(password, context)
        val iv = ByteArray(IV_LENGTH)
        Random.nextBytes(iv)

        preferences.edit {
//            putString(passwordKey, encryptedPassword)
            putString(passwordKey + "_IV", Base64.encodeToString(iv, Base64.DEFAULT))
        }
    }

    private fun encryptPassword(password: String, context: Context): ByteArray {
        val key = getOrCreateSecretKey(context)
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedPassword = cipher.doFinal(password.toByteArray())
        return encryptedPassword
    }

    fun getPassword(context: Context): String? {
        val encryptedPassword = preferences.getString(passwordKey, "") ?: return null
        val ivString = preferences.getString(passwordKey + "_IV", "") ?: return null
        val iv = Base64.decode(ivString, Base64.DEFAULT)

        val key = getOrCreateSecretKey(context)
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        val decryptedPassword = cipher.doFinal(Base64.decode(encryptedPassword, Base64.DEFAULT))

        return String(decryptedPassword)
    }

    private fun getOrCreateSecretKey(context: Context): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        return if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.getKey(KEY_ALIAS, null) as SecretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
}