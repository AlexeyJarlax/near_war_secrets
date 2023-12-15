package com.practicum.kototeka

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.ImageView
import com.practicum.kototeka.util.ThemeManager

class MainActivity : AppCompatActivity() {

    companion object {
        private const val KEY_INPUT_SHOWN_KEY = "key_input_shown"
        private const val SETTINGS_REQUEST_CODE = 1
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this) // Применяю тему сразу при запуске
        setContentView(R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val buttonLogin = findViewById<Button>(R.id.button_login)
        val buttonSearch = findViewById<Button>(R.id.button_search)
        val buttonMedialib = findViewById<Button>(R.id.button_item_loader)
        val buttonMyGallery = findViewById<Button>(R.id.button_my_gallery)
        val buttonSettings = findViewById<Button>(R.id.button_settings)

//        val isKeyInputShown = sharedPreferences.getBoolean(KEY_INPUT_SHOWN_KEY, false)
//
//        if (!isKeyInputShown) {
//            val displayIntent = Intent(this, KeyInputActivity::class.java)
//            startActivity(displayIntent)
//            sharedPreferences.edit().putBoolean(KEY_INPUT_SHOWN_KEY, true).apply()
//        } else {
//            // Handle normal login flow
//        }

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
            val displayIntent = Intent(this, GalleryActivity::class.java)
            startActivity(displayIntent)
        }

        buttonSettings.setOnClickListener {
            val displayIntent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(displayIntent, SETTINGS_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val switchState = data?.getBooleanExtra("switchState", false) ?: false
            updateBackgroundImage(switchState)
        }
    }

    // Функция для обновления фона активности в зависимости от состояния переключателя
    private fun updateBackgroundImage(isChecked: Boolean) {
        val backgroundView = findViewById<ImageView>(R.id.background_image)
        if (isChecked) {
            backgroundView.setImageResource(R.drawable.gachi)
        } else {
            backgroundView.setImageResource(R.drawable.cat)
        }
    }

    override fun onResume() {
        super.onResume()
        val isKeyInputShown = sharedPreferences.getBoolean(KEY_INPUT_SHOWN_KEY, false)

        if (!isKeyInputShown) {
            val displayIntent = Intent(this, KeyInputActivity::class.java)
            startActivity(displayIntent)
            sharedPreferences.edit().putBoolean(KEY_INPUT_SHOWN_KEY, true).apply()
        }
    }
}