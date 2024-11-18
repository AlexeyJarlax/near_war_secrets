//package com.pavlov.MyShadowGallery.мусор
//
//import android.content.Context
//import android.content.Intent
//import android.content.SharedPreferences
//import android.graphics.drawable.TransitionDrawable
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.view.View
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.core.content.ContextCompat
//import com.pavlov.MyShadowGallery.ui.main.MainActivity
//import com.pavlov.MyShadowGallery.R
//import com.pavlov.MyShadowGallery.util.APK
//import com.pavlov.MyShadowGallery.util.APKM
//
//class TwoStepsForSaveActivity : AppCompatActivity() {
//
//    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var errorTextWeb: TextView
//    private lateinit var errorIcon: ImageView
//    private lateinit var inputButton: Button
//    private lateinit var yesButton: Button
//    private lateinit var noButton: Button
//    private lateinit var oldKeyButton: Button
//    private var isPasswordExists: Boolean = false // // ФЛАГ
//    private var buttonSecurity1: Boolean = false // // ФЛАГ
//    private var buttonSecurity2: Boolean = false // // ФЛАГ
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_three_steps)
//        sharedPreferences =
//            getSharedPreferences(APK.PREFS_NAME, Context.MODE_PRIVATE)
//        errorIcon = findViewById(R.id.error_icon)
//        errorTextWeb = findViewById(R.id.error_text_web)
//        inputButton = findViewById(R.id.retry_button)
//        yesButton = findViewById(R.id.yes_button)
//        noButton = findViewById(R.id.no_button)
//        oldKeyButton = findViewById(R.id.button_old_key)
//
//        isPasswordExists = intent.getBooleanExtra("isPasswordExists", false) // ФЛАГ
//        buttonSecurity1 = intent.getBooleanExtra("buttonSecurity1", false) // ФЛАГ
//        buttonSecurity2 = intent.getBooleanExtra("buttonSecurity3", false) // ФЛАГ
//
//        if (isPasswordExists) {
//            step3()
//        } else if (buttonSecurity1) {
//            step1()
//        } else if (buttonSecurity2) {
//            step3()
//        } else {
//            stepZero()
//        }
//    }
//    // три шага для входа в приложение
//
//    fun stepZero() {
//        errorIcon.setImageResource(R.drawable.ic_launcher_foreground)
//        errorTextWeb.text = resources.getString(R.string.step00)
//        inputButton.setOnClickListener {
//            step1()
//        }
//    }
//
//    private fun step1() { // ПАРОЛЬ
//        val emptyIcon = ContextCompat.getDrawable(this, android.R.color.transparent)
//        errorIcon.setImageDrawable(emptyIcon)
//        val drawable1 = ContextCompat.getDrawable(this, R.drawable.three_steps1)
//        val drawable2 = ContextCompat.getDrawable(this, R.drawable.three_steps2)
//        val transitionDrawable = TransitionDrawable(arrayOf(drawable2, drawable1))
//        errorIcon.background = transitionDrawable
//        transitionDrawable.startTransition(4000)
//        yesButton.visibility = View.VISIBLE
//        noButton.visibility = View.VISIBLE
//        yesButton.setOnClickListener {
//            val intent = Intent(this, SetPasswordActivity::class.java)
//            startActivity(intent)
//        }
//        noButton.setOnClickListener {
//            if(buttonSecurity1) {
//                goToMain()
//            } else {
//                step3()
//            }
//
//        }
//        val savedPassword = APKM(context = this).getMastersSecret(APK.KEY_SMALL_SECRET)
//        if (savedPassword.isNullOrBlank()) {
//            errorTextWeb.text = resources.getString(R.string.step01_01)
//            inputButton.visibility = View.GONE
//        } else {
//            errorTextWeb.text = resources.getString(R.string.step01_02)
//            inputButton.visibility = View.VISIBLE
//            inputButton.text = resources.getString(R.string.step01_03)
//            inputButton.setOnClickListener {
////                val delPassword = true
//                val intent = Intent(this, AuthActivity::class.java)
//                intent.putExtra("delPassword", true)
//                startActivity(intent)
//            }
//        }
//
//
//    }
//
//    private fun step3() { // КЛЮЧ ШИФРОВАНИЯ
//        errorTextWeb.text = resources.getString(R.string.step03_01)
//        inputButton.text = "?"
//        inputButton.visibility = View.GONE
//        yesButton.visibility = View.VISIBLE
//        noButton.visibility = View.VISIBLE
//        errorIcon.visibility = View.GONE
//
//        yesButton.setOnClickListener {
//            Toast.makeText(this, R.string.wait, Toast.LENGTH_SHORT).show()
//            val displayIntent = Intent(this, KeyInputActivity::class.java)
//            startActivity(displayIntent)
//        }
//        noButton.setOnClickListener {
//            APKM(context = this).delFromSP(APK.DEFAULT_KEY)
//            APKM(context = this).saveBooleanToSPK(APK.KEY_USE_THE_ENCRYPTION_K, false)
//            goToMain()
//        }
//
//    }
//
//    fun goToMain() {
//        val displayIntent = Intent(this, MainActivity::class.java)
//        startActivity(displayIntent)
//    }
//}