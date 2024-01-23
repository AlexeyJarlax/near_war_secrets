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
import com.pavlov.MyShadowGallery.MainPageActivity
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.SearchActivity
import com.pavlov.MyShadowGallery.SettingsActivity
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.util.APKM
import com.pavlov.MyShadowGallery.util.ThemeManager

class LoginActivity : AppCompatActivity() {

    private var delPassword: Boolean = false
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
        delPassword = intent.getBooleanExtra("delPassword", false) // ФЛАГ С intent
        loadingIndicator = findViewById(R.id.loading_indicator)
        sharedPreferences =
            getSharedPreferences(APK.PREFS_NAME, Context.MODE_PRIVATE)
        ThemeManager.applyTheme(this)
        backgroundView = findViewById(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        setPasswordButton = findViewById(R.id.setPasswordButton)
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText)
        oldPasswordText = findViewById(R.id.oldPasswordEditTextText)
        tryCounter = findViewById(R.id.try_counter)
        alarmCounter = findViewById(R.id.counter_alarm)
        updateAppLanguage()
        firstStart() // первый пуск приложения?
//        mimicryCheck() // Проверка на маскировку
//        parolchikCheck() // подготовка интерфейса
        listener() // TextChangedListener
        setPasswordButton() // клик по кнопке
        counter = APKM(context = this).getCounter(APK.KEY_COUNT_TRY, 30)
        tryCounter.text = counter.toString()
        tryCounter.visibility = View.INVISIBLE
        if (APKM(context = this).countBigSecrets()>0){
            APKM(context = this).saveBooleanToSPK(APK.KEY_EXIST_OF_ENCRYPTION_K, true)
        } else {
            APKM(context = this).saveBooleanToSPK(APK.KEY_EXIST_OF_ENCRYPTION_K, false)
        }
    }  // конец онкриейт

    private fun updateAppLanguage() {// Извлечение сохраненного языка
        val selectedLanguage = APKM(context = this).getStringFromSharedPreferences(APK.PREF_LANGUAGE_KEY)
        if (selectedLanguage.isNotEmpty()) {
            SettingsActivity.setAppLanguage(this, selectedLanguage)
        }
    }

    private fun firstStart() {  // ПЕРВЫЙ ЗАПУСК ?????
        if (!delPassword) {
            if (sharedPreferences.getBoolean(
                    APK.KEY_FIRST_RUN, true
                )
            ) { // Устанавливаем значения по умолчанию
                SettingsActivity.doClearStorage(applicationContext) // защита от злоумышленника
                with(sharedPreferences.edit()) {
                    putInt(
                        APK.KEY_PREVIEW_SIZE_SEEK_BAR, 30
                    )
                    putBoolean(
                        APK.KEY_FIRST_RUN, false
                    )
                    apply()
                }
                goToZeroActivity() // ИДЕМ В ТРИ ШАГА К ЗАЩИТЕ
            } else {
                if ( APKM(context = this).getBooleanFromSPK(APK.KEY_EXIST_OF_MIMICRY)// МИМИКРИРУЮЩИЙ ЗАПУСК ?????
                ) {
                    mimicry = true
                    entranceMimic()  // ИДЕМ В МИМИКРИРУЮЩЕЕ ОКНО
                } else {
                    mimicry = false
                    val savedPassword = APKM(context = this).getMastersSecret(APK.KEY_SMALL_SECRET)
                    if (savedPassword.isNullOrBlank()) {    // ЗАПОРОЛЕННЫЙ ЗАПУСК ?????
                        isPasswordExist = false
                        entranceMain()  // ИДЕМ В МЕЙН
                    } else {

                    }
                }
            }
        } else {

        }
    }

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
                    val oldPassword = APKM(context = this).getMastersSecret(APK.KEY_SMALL_SECRET)
                    if (oldPassword == oldPasswordEditText.text.toString().trim()) {
                        counter = 30
                        APKM(context = this).saveCounter(APK.KEY_COUNT_TRY, counter)
                        if (delPassword) {
                            doDelPassword()
                            toastIt(getString(R.string.del_password))
                            goToZeroActivityWithFlag()
                        } else {
                            entranceMain()
                        }
                    } else {
                        APKM(context = this).saveCounter(APK.KEY_COUNT_TRY, counter)
                        toastIt(getString(R.string.wrong_password))

                        if (counter < 27) {
                            isButtonEnabled = false
                            tryCounter.visibility = View.VISIBLE
                            tryCounter.text = getString(R.string.try_left) + counter.toString()
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
                                getString(R.string.last_warning)
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

    private fun doDelPassword() {
        APKM(context = this).delMastersSecret(APK.KEY_SMALL_SECRET)
        val sharedPreferences =
            getSharedPreferences(APK.PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(APK.KEY_EXIST_OF_PASSWORD, false)
        editor.apply()
    }

    private fun goToZeroActivity() {
        val intent = Intent(this, ThreeStepsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToZeroActivityWithFlag() {
        val intent = Intent(this, ThreeStepsActivity::class.java)
        intent.putExtra("isPasswordExists", true)
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