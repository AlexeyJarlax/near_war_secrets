package com.pavlov.MyShadowGallery

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.pavlov.MyShadowGallery.util.AppPreferencesKeys

import com.pavlov.MyShadowGallery.util.ThemeManager

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
        ThemeManager.applyTheme(this) // Применяю ночную тему
        var backgroundView = findViewById<ImageView>(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        val buttonLogin = findViewById<Button>(R.id.button_login)
        val buttonSearch = findViewById<Button>(R.id.button_search)
        val buttonMedialib = findViewById<Button>(R.id.button_item_loader)
        val buttonMyGallery = findViewById<Button>(R.id.button_my_gallery)
        val buttonSettings = findViewById<Button>(R.id.button_settings)

        // Проверяем, является ли текущий запуск приложения первым
        val isFirstRun = sharedPreferences.getBoolean(AppPreferencesKeys.KEY_FIRST_RUN, true)

        if (isFirstRun) { // Устанавливаем значения по умолчанию
            with(sharedPreferences.edit()) {
                putInt(AppPreferencesKeys.KEY_PREVIEW_SIZE_SEEK_BAR, 30)
                putBoolean(AppPreferencesKeys.KEY_FIRST_RUN, false) // Помечаем, что приложение уже запускалось
                apply()
            }
        }

        buttonLogin.setOnClickListener {
            val displayIntent = Intent(this, KeyInputActivity::class.java)
            startActivity(displayIntent)
        }

        buttonSearch.setOnClickListener {
            val displayIntent = Intent(this, SearchActivity::class.java)
            startActivity(displayIntent)
        }

        buttonMedialib.setOnClickListener {
            val displayIntent = Intent(this, ItemLoaderActivity::class.java)
            startActivity(displayIntent)
        }

        buttonMyGallery.setOnClickListener {
            val displayIntent = Intent(this, StorageLogActivity::class.java)
            startActivity(displayIntent)
        }

        buttonSettings.setOnClickListener {
            val displayIntent = Intent(this, SettingsActivity::class.java)
            startActivity(displayIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var backgroundView = findViewById<ImageView>(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        }


    override fun onResume() {
        super.onResume()
        val isExistsOfEncryptionKey = sharedPreferences.getBoolean(AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK, false)
        val isUseTheEncryptionKey = sharedPreferences.getBoolean(AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_KLUCHIK, false)
        val keySimbl = findViewById<Button>(R.id.button_login)
        if (isExistsOfEncryptionKey) {
            keySimbl.setText("🔐")
            keySimbl.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yp_blue))
        } else {
            keySimbl.setText("🔓")
            keySimbl.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.kototeka_thumb_color))
        }
        if (!isExistsOfEncryptionKey && isUseTheEncryptionKey) {
            val displayIntent = Intent(this, KeyInputActivity::class.java)
            startActivity(displayIntent)
        }

    }
}