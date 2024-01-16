package com.pavlov.MyShadowGallery

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.pavlov.MyShadowGallery.file.StorageLogActivity
import com.pavlov.MyShadowGallery.security.KeyInputActivity
import com.pavlov.MyShadowGallery.security.ThreeStepsActivity
import com.pavlov.MyShadowGallery.util.AppPreferencesKeys
import com.pavlov.MyShadowGallery.util.AppPreferencesKeysMethods

import com.pavlov.MyShadowGallery.util.ThemeManager

class MainPageActivity : AppCompatActivity() {

//    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var threeStepsActivity: ThreeStepsActivity
    private var simblPass = "🏳️"
    private var simblMimic = "🏳️"
    private var simblEncryption = "🏳️"
    private var text = "🏳️"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        val pref1 = AppPreferencesKeysMethods(context = this).getBooleanFromSharedPreferences(AppPreferencesKeys.KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION)
        val pref2 = AppPreferencesKeysMethods(context = this).getMastersSecret(AppPreferencesKeys.KEY_BIG_SECRET).isBlank()
        if (pref1 && pref2) {
            val displayIntent = Intent(this, KeyInputActivity::class.java)
            startActivity(displayIntent)
        }

        var backgroundView = findViewById<ImageView>(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        val buttonLogin = findViewById<Button>(R.id.button_login)
        val buttonSearch = findViewById<Button>(R.id.button_search)
        val buttonGallery = findViewById<Button>(R.id.button_gallery)
        val buttonMedialib = findViewById<Button>(R.id.button_item_loader)
        val buttonStorageLog = findViewById<Button>(R.id.button_storage_log)
        val buttonSettings = findViewById<Button>(R.id.button_settings)
//        threeStepsActivity = ThreeStepsActivity()


        buttonLogin.setOnClickListener {
            goToThreeStepsActivity()
        }

        buttonSearch.setOnClickListener {  // флаг на отображение кнопки назад
            val displayIntent = Intent(this, SearchActivity::class.java)
            displayIntent.putExtra("showBackBtn", true)
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

        buttonSettings.setOnClickListener {
            val displayIntent = Intent(this, SettingsActivity::class.java)
            startActivity(displayIntent)
        }
        locker()
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
        val passKey = AppPreferencesKeysMethods(context = this).getBooleanFromSharedPreferences(AppPreferencesKeys.KEY_EXIST_OF_PASSWORD)
//            sharedPreferences.getBoolean(AppPreferencesKeys.KEY_EXIST_OF_PASSWORD, false)
        val EncryptionKey = AppPreferencesKeysMethods(context = this).getBooleanFromSharedPreferences(AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_K)
//            sharedPreferences.getBoolean(AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_K, false)
        val mimikKey = AppPreferencesKeysMethods(context = this).getBooleanFromSharedPreferences(AppPreferencesKeys.KEY_EXIST_OF_MIMICRY)
//            sharedPreferences.getBoolean(AppPreferencesKeys.KEY_EXIST_OF_MIMICRY, false)

        var keySimbl = findViewById<Button>(R.id.button_login)

        simblPass = if (passKey) {
            "🔐"
        } else {
            ""
        }
        simblMimic = if (mimikKey) {
            "🕶️"
        } else {
            ""
        }
        simblEncryption = if (EncryptionKey) {
            "🔏"
        } else {
            ""
        }
        text = "${simblPass}${simblMimic}${simblEncryption}"
        keySimbl.text = text

        if (text.length < 2) {
            keySimbl.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.kototeka_thumb_color))
            keySimbl.text = "🏳️"
        } else if (text.length < 4) {
            keySimbl.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.kototeka_thumb_color2))
        } else if (text.length < 6) {
            keySimbl.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yp_blue_light))
        } else if (text.length < 8) {
            keySimbl.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yp_blue))
        }
    }
}