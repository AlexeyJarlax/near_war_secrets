package com.pavlov.MyShadowGallery

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.pavlov.MyShadowGallery.util.AppPreferencesKeys
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
//                            confirmButton.isEnabled =
//                                false // Заблокировать кнопку, если байтов меньше 16
                            "рекомендуется добавить: $remainingBytes байт"
                        }

                        remainingBytes < 0 -> {
//                            confirmButton.isEnabled =
//                                false // Заблокировать кнопку, если байтов больше 16
//                            "излишние: ${-remainingBytes} байт"
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
//                    helperText.text = ""
//                    confirmButton.isEnabled = true // Разблокировать кнопку при пустом вводе
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Не используется
            }
        })

        fun confirmButton() {
            val keyValue = keyInputEditText.text.toString().trim()

            if (keyValue.isNotEmpty()) {
                val editor = sharedPreferences.edit()
                editor.putString(AppPreferencesKeys.ENCRYPTION_KLUCHIK, keyValue)
                editor.putBoolean(AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK, true)
                editor.putBoolean(AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_KLUCHIK, true)
                editor.apply()
                Toast.makeText(this, "Ключ шифрования задан", Toast.LENGTH_SHORT).show()
                val displayIntent = Intent(this, MainActivity::class.java)
                startActivity(displayIntent)
            } else {
                Toast.makeText(this, "Ключ шифрования не задан", Toast.LENGTH_SHORT).show()
//                userEscape()
            }
//            finish()
        }

        constantKey.setOnClickListener { // постоянный ключ
            val editor = sharedPreferences.edit()
            editor.putBoolean(AppPreferencesKeys.KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION, false)
            confirmButton()
        }

        variableKey.setOnClickListener { // переменный ключ
            val editor = sharedPreferences.edit()
            editor.putBoolean(AppPreferencesKeys.KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION, true)
            confirmButton()
        }

        buttonOldKey.setOnClickListener { // старый ключ
            val editor = sharedPreferences.edit()
            editor.putBoolean(AppPreferencesKeys.KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION, false)
            Toast.makeText(this, "Ключ шифрования задан", Toast.LENGTH_SHORT).show()
            val displayIntent = Intent(this, MainActivity::class.java)
            startActivity(displayIntent)
        }


        cancelButton.setOnClickListener { // Юзер выбрал: Не использовать ключ шифрования
            val editor = sharedPreferences.edit()
            editor.putBoolean(AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK, false)
            editor.putBoolean(AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_KLUCHIK, false)
            editor.remove(AppPreferencesKeys.ENCRYPTION_KLUCHIK)
            editor.apply()
            Toast.makeText(this, "Файлы будут сохранены без шифрования", Toast.LENGTH_SHORT)
                .show()
//            finish() // Завершение активности KeyInputActivity без передачи значения ключа
            val displayIntent = Intent(this, MainActivity::class.java)
            startActivity(displayIntent)
        }

        generateButton.setOnClickListener {
            val keyEditText = findViewById<EditText>(R.id.edit_text_key)
            val generatedKey = generateRandomKey(16) // Генерация 16-битного случайного ключа
            keyEditText.setText(generatedKey)
        }


    }

    fun userEscape() { // пользователь сбегает и не вводит ключ
        val editor = sharedPreferences.edit()
        editor.putBoolean(AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK, false)
        editor.putBoolean(AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_KLUCHIK, true)
        editor.apply()
        Toast.makeText(this, "Ключ шифрования не задан", Toast.LENGTH_SHORT).show()
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
//        finish()
        val displayIntent = Intent(this, MainActivity::class.java)
        startActivity(displayIntent)
    }
}