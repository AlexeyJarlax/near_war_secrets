package com.pavlov.MyShadowGallery

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.pavlov.MyShadowGallery.file.StorageLogActivity
import com.pavlov.MyShadowGallery.security.KeyInputActivity
import com.pavlov.MyShadowGallery.security.ThreeStepsActivity
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.util.APKM

import com.pavlov.MyShadowGallery.util.ThemeManager

class MainPageActivity : AppCompatActivity() {

    private var simblPass = "🏳️"
    private var simblMimic = "🏳️"
    private var simblEncryption = "🏳️"
    private var text = "🏳️"
    private var pref1 = false
    private var pref2 = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var buttonSecurity1: Button
    private lateinit var buttonSecurity2: Button
    private lateinit var buttonSecurity3: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)
        var backgroundView = findViewById<ImageView>(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        val buttonLogin = findViewById<Button>(R.id.button_login)
        val buttonSearch = findViewById<Button>(R.id.button_search)
        val buttonGallery = findViewById<Button>(R.id.button_gallery)
        val buttonMedialib = findViewById<Button>(R.id.button_item_loader)
        val buttonStorageLog = findViewById<Button>(R.id.button_storage_log)
        val buttonSettings = findViewById<Button>(R.id.button_settings)
        val buttonHowDoesIsWork = findViewById<Button>(R.id.how_does_is_work)
        buttonSecurity1 = findViewById<Button>(R.id.button_security1)
        buttonSecurity2 = findViewById<Button>(R.id.button_security2)
        buttonSecurity3 = findViewById<Button>(R.id.button_security3)

        buttonLogin.setOnClickListener {
            val displayIntent = Intent(this, AboutActivity::class.java)
            startActivity(displayIntent)
        }

        buttonSecurity1.setOnClickListener {
            val displayIntent = Intent(this, ThreeStepsActivity::class.java)
            displayIntent.putExtra("buttonSecurity1", true)
            startActivity(displayIntent)
        }
        buttonSecurity2.setOnClickListener {
            val displayIntent = Intent(this, ThreeStepsActivity::class.java)
            displayIntent.putExtra("buttonSecurity2", true)
            startActivity(displayIntent)
        }
        buttonSecurity3.setOnClickListener {
            val displayIntent = Intent(this, ThreeStepsActivity::class.java)
            displayIntent.putExtra("buttonSecurity3", true)
            startActivity(displayIntent)
        }

        buttonGallery.setOnClickListener { // флаг ItemLoaderActivity в режиме Галереи
            val displayIntent = Intent(this, ItemLoaderActivity::class.java)
            displayIntent.putExtra("hideConstraintLayout", true)
            startActivity(displayIntent)
        }

        buttonMedialib.setOnClickListener {
            val displayIntent = Intent(this, ItemLoaderActivity::class.java)
            startActivity(displayIntent)
        }

        buttonStorageLog.setOnClickListener {
            val displayIntent = Intent(this, StorageLogActivity::class.java)
            startActivity(displayIntent)
        }

        buttonHowDoesIsWork.setOnClickListener {
            val displayIntent = Intent(this, FAQActivity::class.java)
            startActivity(displayIntent)
        }

        buttonSearch.setOnClickListener {  // флаг на отображение кнопки назад
            val displayIntent = Intent(this, SearchActivity::class.java)
            displayIntent.putExtra("showBackBtn", true)
            startActivity(displayIntent)
        }

        buttonSettings.setOnClickListener {
            val displayIntent = Intent(this, SettingsActivity::class.java)
            startActivity(displayIntent)
        }
        // Запуск методов locker() и prestart() в отдельном потоке с задержкой
        handler.postDelayed({
            locker()
            prestart()
        }, 300) // Задержка в миллисекундах (в данном случае, 500 миллисекунд или 0.5 секунды)

    } // конец OnCreate

    fun goToThreeStepsActivity() {
        val displayIntent = Intent(this, ThreeStepsActivity::class.java)
        startActivity(displayIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var backgroundView = findViewById<ImageView>(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
    }

    private fun locker() {
        val passKey = APKM(context = this).getBooleanFromSPK(APK.KEY_EXIST_OF_PASSWORD, false)
        val encryptionKeyName: Boolean =
            APKM(context = this).getIntFromSP(APK.DEFAULT_KEY) != 0
        val mimikKey = APKM(context = this).getBooleanFromSPK(APK.KEY_EXIST_OF_MIMICRY, false)

        if (passKey) {
            simblPass = "🔐"
            buttonSecurity1.text = simblPass
            buttonSecurity1.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
        } else {
            simblPass = "🏳️"
            buttonSecurity1.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.kototeka_thumb_color2))
        }

        if (mimikKey) {
            simblMimic = "🕶️"
            buttonSecurity2.text = simblMimic
            buttonSecurity2.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
        } else {
            simblMimic = "🏳️"
            buttonSecurity2.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.kototeka_thumb_color2))
        }

        if (encryptionKeyName) {
            simblEncryption = APKM(context = this).getDefauldKeyName()
            buttonSecurity3.text = simblEncryption
            buttonSecurity3.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
        } else {
            simblEncryption = "🏳️"
            buttonSecurity3.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.kototeka_thumb_color2))
        }

//        simblPass = if (passKey) {
//            "🔐"
//        } else {
//            "🏳️"
//        }
//        simblMimic = if (mimikKey) {
//            "🕶️"
//        } else {
//            "🏳️"
//        }
//        simblEncryption = if (encryptionKeyName) {
//            APKM(context = this).getDefauldKeyName()
//        } else {
//            "🏳️"
//        }
//            keySimbl.backgroundTintList =
//                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.kototeka_thumb_color))
//            keySimbl.text = "🏳️"
//        } else if (text.length < 4) {
//            keySimbl.backgroundTintList =
//                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.kototeka_thumb_color2))
//        } else if (text.length < 6) {
//            keySimbl.backgroundTintList =
//                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yp_blue_light))
//        } else if (text.length < 8) {
//            keySimbl.backgroundTintList =
//                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yp_blue))
//        }
    }

    private fun prestart() {
        pref1 = APKM(context = this).getBooleanFromSPK(APK.KEY_DELETE_AFTER_SESSION, true)
        pref2 = APKM(context = this).getBooleanFromSPK(APK.KEY_EXIST_OF_ENCRYPTION_K, false)
        if (pref1 && !pref2) {
            val displayIntent = Intent(this, KeyInputActivity::class.java)
            startActivity(displayIntent)
            finish()
        }
    }
}