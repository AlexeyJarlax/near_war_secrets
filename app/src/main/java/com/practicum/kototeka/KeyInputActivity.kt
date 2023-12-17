package com.practicum.kototeka

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlin.random.Random

class KeyInputActivity : AppCompatActivity() {

    companion object {
        var encryptionKey: String = ""
        const val EXTRA_KEY_VALUE = "key_value"
        const val DEF_KEY_VALUE = "7@1&0Sex7gEwq#51"
    }
//    var encryptionKey: String  = ""// Переменная для хранения ключа шифрования

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_input)

        val confirmButton = findViewById<Button>(R.id.button_confirm)
        val cancelButton = findViewById<Button>(R.id.button_cancel)
        val generateButton = findViewById<Button>(R.id.button_generate)
        val helperText = findViewById<TextView>(R.id.edit_text_key_helper)
        val keyInputEditText = findViewById<EditText>(R.id.edit_text_key)

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
                            "излишние: ${-remainingBytes} байт"
                        }

                        else -> {
                            confirmButton.isEnabled =
                                true
                            "" // Для случая, когда количество байт равно 16
                    }}

                    val containsInvalidCharacters = keyValue.contains(" ")
                    val isValid = !containsInvalidCharacters && remainingBytes >= 0

                    helperText.text = message
                    helperText.setTextColor(if (isValid) Color.GREEN else Color.RED)
                } else {
                    helperText.text = ""
                    confirmButton.isEnabled = true // Разблокировать кнопку при пустом вводе
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Не используется
            }
        })

        confirmButton.setOnClickListener {
            val keyValue = keyInputEditText.text.toString().trim()

            if (keyValue.isNotEmpty()) {
                encryptionKey = keyValue// Здесь можно использовать значение keyValue
                Toast.makeText(this, "Ключ шифрования задан", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ключ шифрования не задан", Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
            }
            finish()
        }


        cancelButton.setOnClickListener {
            if (encryptionKey.isEmpty()) {
                encryptionKey = ""
                Toast.makeText(this, "Ключ шифрования не задан", Toast.LENGTH_SHORT).show()
            }
            setResult(RESULT_CANCELED)
            finish() // Завершение активности KeyInputActivity без передачи значения ключа
        }

        generateButton.setOnClickListener {
            val keyEditText = findViewById<EditText>(R.id.edit_text_key)
            val generatedKey = generateRandomKey(16) // Генерация 16-битного случайного ключа
            keyEditText.setText(generatedKey)
        }
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
        setResult(RESULT_CANCELED)
        finish()
    }
}