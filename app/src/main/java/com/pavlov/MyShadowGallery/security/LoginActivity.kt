package com.pavlov.MyShadowGallery.security

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.pavlov.MyShadowGallery.MainPageActivity
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.SearchActivity
import com.pavlov.MyShadowGallery.util.AppPreferencesKeys
import com.pavlov.MyShadowGallery.util.ThemeManager

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var backgroundView: ImageView
    private lateinit var setPasswordButton: Button
    private lateinit var newPasswordEditText: EditText
    private lateinit var newPasswordEditText2: EditText
    private lateinit var oldPasswordEditText: EditText
    private lateinit var oldPasswordText: TextView
    private var isPasswordExist = true
    private var oldPasswordType = ""
    private var mimicry = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences =
            getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
        backgroundView = findViewById(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        setPasswordButton = findViewById(R.id.setPasswordButton)
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText)
        oldPasswordText = findViewById(R.id.oldPasswordEditTextText)
        firstStart() // первый пуск приложения?
        mimicryCheck() // Проверка на маскировку
        parolchikCheck() // подготовка интерфейса
        listener() // TextChangedListener
        setPasswordButton() // клик по кнопке
    }  // конец онкриейт

    fun firstStart() { // первый пуск приложения?
//        var isFirstRun = sharedPreferences.getBoolean(AppPreferencesKeys.KEY_FIRST_RUN, false)
        if (sharedPreferences.getBoolean(AppPreferencesKeys.KEY_FIRST_RUN, false)) { // Устанавливаем значения по умолчанию
            with(sharedPreferences.edit()) {
                putInt(AppPreferencesKeys.KEY_PREVIEW_SIZE_SEEK_BAR,
                    30
                )
                putBoolean(AppPreferencesKeys.KEY_FIRST_RUN,
                    false
                )
                apply()// Помечаем, что приложение уже запускалось
            }
            goToZeroActivity()
        } else {
            with(sharedPreferences.edit()) {
                putBoolean(AppPreferencesKeys.KEY_FIRST_RUN,
                    false
                )
                apply()// Помечаем, что приложение уже запускалось
            }
        }
    }

    fun mimicryCheck() { // Проверка на маскировку
        if (sharedPreferences.getBoolean(
                AppPreferencesKeys.KEY_MIMICRY_SWITCH,
                false
            )
        ) {
            mimicry = true
            entranceMimic()
        } else {
            mimicry = false
        }
    }

    fun parolchikCheck() {
        val savedPassword = masterAlias()
        if (savedPassword.isNullOrBlank()) {
            isPasswordExist = false
            entranceMain()
        } else {
//            toastIt("Обнаружен ранее сохраненный пароль")
//            toastIt("Для смены пароля введите старый и новый пароли")
        }
    }

    fun listener() {
        oldPasswordEditText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // Не используется
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
//                val tipeText = oldPasswordEditText.text.toString().trim()
//                if (tipeText.isNotEmpty()) {
//                    oldPasswordType = tipeText
//                }
                }

                override fun afterTextChanged(s: Editable?) {
                    // Не нужно выполнять проверку здесь
                }
            })
    }

    fun setPasswordButton() {
        setPasswordButton.setOnClickListener { // подтвердить пароль

            if (isPasswordExist) {
                val oldPassword = masterAlias()
                if (oldPassword == oldPasswordEditText.text.toString().trim()) {
                    entranceMain()
                }
            }
        }
    }

    fun masterAlias(): String? {
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences: SharedPreferences =
            EncryptedSharedPreferences.create(
                AppPreferencesKeys.SMALL_SECRETS_PREFS_NAME,
                masterAlias,
                applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        return sharedPreferences.getString(AppPreferencesKeys.KEY_SMALL_SECRET, "")
    }

    fun goToZeroActivity() {
        val intent = Intent(this, ThreeStepsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun entranceMain() {
        val intent = Intent(this, MainPageActivity::class.java)
//        intent.putExtra("isPasswordExists", true) // Set the flag based on your requirement
        startActivity(intent)
        finish()
    }

    private fun entranceMimic() {
        val intent = Intent(this, SearchActivity::class.java)
        intent.putExtra("isPasswordExists", true) // Set the flag based on your requirement
        startActivity(intent)
        finish()
    }


    // Функция для проверки валидности ввода
    fun isValidInput(input: String): Boolean {
        // Регулярное выражение для проверки на допустимые символы
        val regex =
            Regex("[a-zA-Zа-яА-ЯñÑáéíóúüÜ0-9.,!?@#\$%^&*()_+-=:;<>{}\\[\\]\"'\\\\/\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}]+")
        return regex.matches(input)
    }

    private fun toastIt(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}