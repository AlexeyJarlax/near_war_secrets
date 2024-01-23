package com.pavlov.MyShadowGallery.security

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.util.APKM
import com.pavlov.MyShadowGallery.util.ThemeManager

class SetPasswordActivity : AppCompatActivity() {

    private lateinit var backgroundView: ImageView
    private lateinit var setPasswordButton: Button
    private lateinit var newPasswordEditText: EditText
    private lateinit var newPasswordEditText2: EditText
    private lateinit var oldPasswordEditText: EditText
    private lateinit var oldPasswordText: TextView
    private var isPasswordExist = true
    private var oldPasswordType = ""
    private var newPasswordText2 = ""
    private lateinit var helperText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_password)

        backgroundView = findViewById<ImageView>(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        setPasswordButton = findViewById(R.id.setPasswordButton)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        newPasswordEditText2 = findViewById(R.id.newPasswordEditText2)
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText)
        oldPasswordText = findViewById(R.id.oldPasswordEditTextText)
        helperText = findViewById(R.id.edit_text_key_helper)
        startPreparation() // подготовка интерфейса

        // Установка InputFilter для проверки символов в реальном времени
        newPasswordEditText.filters = arrayOf(PasswordInputFilter())
        newPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Не используется
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Не нужно выполнять проверку здесь
            }

            override fun afterTextChanged(s: Editable?) {
                // Не нужно выполнять проверку здесь
            }
        })

        oldPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Не используется
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val tipeText = oldPasswordEditText.text.toString().trim()
                if (tipeText.isNotEmpty()) {
                    oldPasswordType = tipeText
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Не нужно выполнять проверку здесь
            }
        })
        newPasswordEditText2.filters = arrayOf(PasswordInputFilter())
        newPasswordEditText2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Не используется
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                // Не нужно выполнять проверку здесь
            }
        })


        setPasswordButton.setOnClickListener { // подтвердить пароль
            if (isPasswordExist) {
                val oldPassword =
                    APKM(context = this).getMastersSecret(APK.KEY_SMALL_SECRET)
                if (oldPassword == oldPasswordType) {
                    confirmButton()
                } else {
                    helperText.text = getString(R.string.wrong_password)
                }
            } else {
                confirmButton()
            }
        }
    }  // конец онкриейт

    private fun startPreparation() {  // извлекаем парольку
        val savedPassword =
            APKM(context = this).getMastersSecret(APK.KEY_SMALL_SECRET)
        if (savedPassword.isNullOrBlank()) {
            oldPasswordEditText.visibility = View.INVISIBLE
            oldPasswordText.visibility = View.INVISIBLE
            isPasswordExist = false
        } else {
            toastIt(getString(R.string.to_change_password))
        }
    }

    private fun saveMasterSSecret(password: String) {
        APKM(context = this).saveMastersSecret(
            password,
            APK.KEY_SMALL_SECRET
        )
        toastIt(getString(R.string.save_password))
        oldPasswordType = ""
        isPasswordExist = true

        val sharedPreferences =
            getSharedPreferences(APK.PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(APK.KEY_EXIST_OF_PASSWORD, true)
        editor.apply()
        val intent = Intent(this, ThreeStepsActivity::class.java)
        intent.putExtra("isPasswordExists", true) // Set the flag based on your requirement
        startActivity(intent)
        finish()
    }

    inner class PasswordInputFilter : InputFilter {
        private val regex =
            Regex(APK.REGEX)

        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val input = source?.subSequence(start, end).toString()
            if (!regex.matches(input)) {
                helperText.text = getString(R.string.invalid_character)
                return ""
            } else {
                helperText.text = ""
            }
            return null
        }
    }

    // Функция для проверки валидности ввода
    fun isValidInput(input: String): Boolean {
        // Регулярное выражение для проверки на допустимые символы
        val regex =
            Regex(APK.REGEX)
        return regex.matches(input)
    }

    // Функция для проверки ввода перед сохранением правильного значения
    fun confirmButton() {
        val keyValue = newPasswordEditText.text.toString().trim()
        val keyValue2 = newPasswordEditText2.text.toString().trim()
        if (isValidInput(keyValue)) {
            if (keyValue.isNotBlank() && newPasswordText2.isBlank()) {
                newPasswordEditText2.visibility = View.VISIBLE
                helperText.text = ""

            }
            if (keyValue == keyValue2) {
                saveMasterSSecret(keyValue)
            } else {
                if (keyValue2.isNotBlank()) {
                    helperText.text = getString(R.string.wrong_password)
                }
            }

        } else {
            // Обработка случая, когда ввод не является валидным
            helperText.text = getString(R.string.invalid_character)
            // Очистить поле ввода или выполнить другие необходимые действия
            newPasswordEditText.setText("")
        }
    }

    private fun toastIt(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}