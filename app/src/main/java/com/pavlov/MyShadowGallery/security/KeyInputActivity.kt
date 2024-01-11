package com.pavlov.MyShadowGallery.security

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.pavlov.MyShadowGallery.MainPageActivity
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.util.AppPreferencesKeys
import com.pavlov.MyShadowGallery.util.AppPreferencesKeysMethods
import kotlin.random.Random

class KeyInputActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var confirmable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_input)
        sharedPreferences =
            getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
        val generateButton = findViewById<Button>(R.id.button_generate)
        val helperText = findViewById<TextView>(R.id.edit_text_key_helper)
        val keyInputEditText = findViewById<EditText>(R.id.edit_text_key)
        val cancelButton = findViewById<Button>(R.id.button_cancel)
        val constantKey = findViewById<Button>(R.id.constant_key)
        val variableKey = findViewById<Button>(R.id.variable_key)
        val buttonOldKey = findViewById<Button>(R.id.button_old_key)

        if (sharedPreferences.getBoolean(
                AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK,
                false
            )
        ) {
            buttonOldKey.visibility = View.VISIBLE
        }

        keyInputEditText.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            val input = SpannableStringBuilder()
            for (i in start until end) {
                val c = source[i]
                if (c.toString().matches(Regex("[a-zA-Zа-яА-ЯñÑáéíóúüÜ0-9.,!?@#\$%^&*()_+-=:;<>{}\\[\\]\"'\\\\/\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}]+"))) {
                    input.append(c)
                } else {
                    helperText.text = getString(R.string.invalid_character)
                }
            }
            input
        })

        keyInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Не используется
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyValue = s?.toString()?.trim()

                if (keyValue?.isNotEmpty() == true) {
                    val byteCount = keyValue.toByteArray(Charsets.UTF_8).size
                    val remainingBytes = 16 - byteCount

                    val message = when {
                        remainingBytes > 0 -> {
                            getString(R.string.key_byte_count_message) + " $remainingBytes"
                        }

                        remainingBytes < 0 -> {
                            ""
                        }

                        else -> {
                            confirmable = true
                            "" // Для случая, когда количество байт равно 16
                        }
                    }

                    val containsInvalidCharacters = keyValue.contains(" ")
                    val isValid = !containsInvalidCharacters && remainingBytes >= 0

                    helperText.text = message
                    helperText.setTextColor(if (isValid) Color.GREEN else Color.RED)
                } else {
                    confirmable = false
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Не используется
            }
        })

        constantKey.setOnClickListener { // постоянный ключ
            val editor = sharedPreferences.edit()
            editor.putBoolean(AppPreferencesKeys.KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION, false)
            editor.apply()
            val keyValue = keyInputEditText.text.toString().trim()
            confirmButton(keyValue)
        }

        variableKey.setOnClickListener { // переменный ключ
            val editor = sharedPreferences.edit()
            editor.putBoolean(AppPreferencesKeys.KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION, true)
            editor.apply()
            val keyValue = keyInputEditText.text.toString().trim()
            confirmButton(keyValue)
        }

        buttonOldKey.setOnClickListener { // старый ключ
            val editor = sharedPreferences.edit()
            editor.putBoolean(AppPreferencesKeys.KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION, false)
            editor.apply()
            Toast.makeText(this, R.string.encryption_key_set, Toast.LENGTH_SHORT).show()
            val displayIntent = Intent(this, MainPageActivity::class.java)
            startActivity(displayIntent)
        }

        cancelButton.setOnClickListener { // Юзер выбрал: Не использовать ключ шифрования
            AppPreferencesKeysMethods(context = this).delMastersSecret(AppPreferencesKeys.KEY_BIG_SECRET)

            val editor = sharedPreferences.edit()
            editor.putBoolean(AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK, false)
            editor.putBoolean(AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_KLUCHIK, false)
            editor.remove(AppPreferencesKeys.ENCRYPTION_KLUCHIK)
            editor.apply()
            Toast.makeText(this, R.string.encryption_mode_set_no_encryption, Toast.LENGTH_SHORT)
                .show()
            val displayIntent = Intent(this, MainPageActivity::class.java)
            startActivity(displayIntent)
        }

        generateButton.setOnClickListener {
            val keyEditText = findViewById<EditText>(R.id.edit_text_key)
            val generatedKey = generateRandomKey(16) // Генерация 16-битного случайного ключа
            keyEditText.setText(generatedKey)
        }

    } // конец онКриейт

    private fun confirmButton(keyValue: String) {
        if (keyValue.isNotEmpty()) {
            AppPreferencesKeysMethods(context = this).saveMastersSecret(keyValue, AppPreferencesKeys.KEY_BIG_SECRET)
            val editor = sharedPreferences.edit()
            editor.putBoolean(AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK, true)
            editor.putBoolean(AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_KLUCHIK, true)
            editor.apply()
            Toast.makeText(this, R.string.encryption_key_set, Toast.LENGTH_SHORT).show()
            val displayIntent = Intent(this, MainPageActivity::class.java)
            startActivity(displayIntent)
        } else {
            Toast.makeText(this, R.string.encryption_key_not_set, Toast.LENGTH_SHORT).show()
        }
    }

    fun userEscape() { // пользователь сбегает и не вводит ключ
        val editor = sharedPreferences.edit()
        editor.putBoolean(AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK, false)
        editor.putBoolean(AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_KLUCHIK, true)
        editor.apply()
        Toast.makeText(this, R.string.encryption_key_not_set, Toast.LENGTH_SHORT).show()
        setResult(RESULT_CANCELED)
    }

    fun generateRandomKey(length: Int): String {
        val chars =
            ('a'..'f') + ('0'..'9') // Допустимые символы для ключа (шестнадцатеричные цифры a-f и цифры 0-9)
        val random = Random.Default
        val key = StringBuilder()

        repeat(length) {
            val randomChar = chars[random.nextInt(chars.size)]
            key.append(randomChar)
        }

        return key.toString()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        userEscape()
        val displayIntent = Intent(this, MainPageActivity::class.java)
        startActivity(displayIntent)
    }
}