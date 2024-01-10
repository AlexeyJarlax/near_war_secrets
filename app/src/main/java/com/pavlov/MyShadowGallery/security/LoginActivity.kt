package com.pavlov.MyShadowGallery.security

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.pavlov.MyShadowGallery.MainPageActivity
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.SearchActivity
import com.pavlov.MyShadowGallery.SettingsActivity
import com.pavlov.MyShadowGallery.util.AppPreferencesKeys
import com.pavlov.MyShadowGallery.util.ThemeManager

class LoginActivity : AppCompatActivity() {

    private lateinit var loadingIndicator: ProgressBar
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var backgroundView: ImageView
    private lateinit var setPasswordButton: Button
    private lateinit var oldPasswordEditText: EditText
    private lateinit var oldPasswordText: TextView
    private var isPasswordExist = true
    private var mimicry = false
    var counter = 30
    private lateinit var tryCounter: TextView
    private lateinit var alarmCounter: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        loadingIndicator = findViewById(R.id.loading_indicator)
        sharedPreferences =
            getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
        backgroundView = findViewById(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        setPasswordButton = findViewById(R.id.setPasswordButton)
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText)
        oldPasswordText = findViewById(R.id.oldPasswordEditTextText)
        tryCounter = findViewById(R.id.try_counter)
        alarmCounter = findViewById(R.id.counter_alarm)
        firstStart() // первый пуск приложения?
//        mimicryCheck() // Проверка на маскировку
//        parolchikCheck() // подготовка интерфейса
        listener() // TextChangedListener
        setPasswordButton() // клик по кнопке
        counter = masterAliasInt()
        tryCounter.text = counter.toString()
        tryCounter.visibility = View.INVISIBLE
    }  // конец онкриейт

    private fun firstStart() {  // ПЕРВЫЙ ЗАПУСК ?????

        if (sharedPreferences.getBoolean(
                AppPreferencesKeys.KEY_FIRST_RUN, true
            )
        ) { // Устанавливаем значения по умолчанию

            with(sharedPreferences.edit()) {
                putInt(
                    AppPreferencesKeys.KEY_PREVIEW_SIZE_SEEK_BAR, 30
                )
                putBoolean(
                    AppPreferencesKeys.KEY_FIRST_RUN, false
                )
                apply()
            }
            goToZeroActivity() // ИДЕМ В ТРИ ШАГА К ЗАЩИТЕ
        } else {
//            with(sharedPreferences.edit()) {
//                putBoolean(
//                    AppPreferencesKeys.KEY_FIRST_RUN,
//                    false
//                )
//                apply()// Помечаем, что приложение уже запускалось
//            }
            if (sharedPreferences.getBoolean(   // МИМИКРИРУЮЩИЙ ЗАПУСК ?????
                    AppPreferencesKeys.KEY_MIMICRY_SWITCH, false
                )
            ) {
                mimicry = true
                entranceMimic()  // ИДЕМ В МИМИКРИРУЮЩЕЕ ОКНО
            } else {
                mimicry = false
                val savedPassword = masterAlias()
                if (savedPassword.isNullOrBlank()) {    // ЗАПОРОЛЕННЫЙ ЗАПУСК ?????
                    isPasswordExist = false
                    entranceMain()  // ИДЕМ В МЕЙН
                } else {

                }
            }
        }
    }

//    private fun mimicryCheck() { // Проверка на маскировку
//
//    }

//    fun parolchikCheck() {
//        val savedPassword = masterAlias()
//        if (savedPassword.isNullOrBlank()) {
//            isPasswordExist = false
//            entranceMain()
//        } else {
//        }
//    }

    fun listener() {
        oldPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
                // Не используется
            }

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {

            }

            override fun afterTextChanged(s: Editable?) {
                // Не нужно выполнять проверку здесь
            }
        })
    }

    fun setPasswordButton() {
        var isButtonEnabled = true

        setPasswordButton.setOnClickListener {
            counter = counter - 1
            if (isButtonEnabled) {
                if (isPasswordExist) {
                    val oldPassword = masterAlias()
                    if (oldPassword == oldPasswordEditText.text.toString().trim()) {
                        counter = 30
                        saveCounter()
                        entranceMain()
                    } else {
                        saveCounter()
                        toastIt("Пароль не совпадает")

                        if (counter < 27) {
                            isButtonEnabled = false
                            tryCounter.visibility = View.VISIBLE
                            tryCounter.text = "Попыток: ${counter.toString()}"
                            setPasswordButton.visibility = View.INVISIBLE
                            loadingIndicator.visibility = View.VISIBLE
                            Handler().postDelayed({
                                setPasswordButton.visibility = View.VISIBLE
                                loadingIndicator.visibility = View.INVISIBLE
                                isButtonEnabled = true
                            }, (2000).toLong())
                        }
                        if (counter < 10) {
                            alarmCounter.text =
                                "! После последней попытки произойдет сброс настроек и очистка хранилища !"
                        }
                        if (counter < 1) {
                            SettingsActivity.doClearStorage(applicationContext)
                            SettingsActivity.doResetSettingsAndClearStorage(applicationContext)
                            SettingsActivity.finishAffinity(applicationContext)
                        }
                    }
                }
            }
        }
    }

    fun masterAlias(): String? {
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            AppPreferencesKeys.MY_SECRETS_PREFS_NAME,
            masterAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString(AppPreferencesKeys.KEY_SMALL_SECRET, "")
    }

    fun masterAliasInt(): Int {
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            AppPreferencesKeys.MY_SECRETS_PREFS_NAME,
            masterAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getInt(AppPreferencesKeys.KEY_COUNT_TRY, 30)
    }

    private fun saveCounter() {
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            AppPreferencesKeys.MY_SECRETS_PREFS_NAME,
            masterAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit {
            putInt(AppPreferencesKeys.KEY_COUNT_TRY, counter).apply()
//            toastIt("счетчик изменён")
        }
    }

    fun goToZeroActivity() {
        val intent = Intent(this, ThreeStepsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun entranceMain() {
        val intent = Intent(this, MainPageActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun entranceMimic() {
        val intent = Intent(this, SearchActivity::class.java)
        intent.putExtra("isPasswordExists", true) // Set the flag based on your requirement
        startActivity(intent)
        finish()
    }

    private fun toastIt(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}