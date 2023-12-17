package com.practicum.kototeka

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.practicum.kototeka.util.MyCompObj

import com.practicum.kototeka.util.ThemeManager

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences(MyCompObj.PREFS_NAME, Context.MODE_PRIVATE)
        ThemeManager.applyTheme(this) // Применяю ночную тему
        var backgroundView = findViewById<ImageView>(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        val buttonLogin = findViewById<Button>(R.id.button_login)
        val buttonSearch = findViewById<Button>(R.id.button_search)
        val buttonMedialib = findViewById<Button>(R.id.button_item_loader)
        val buttonMyGallery = findViewById<Button>(R.id.button_my_gallery)
        val buttonSettings = findViewById<Button>(R.id.button_settings)

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
            startActivityForResult(displayIntent, MyCompObj.SETTINGS_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == MyCompObj.SETTINGS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            val gachiModeState = data?.getBooleanExtra(MyCompObj.USER_SWITCH, false) ?: false
//            updateBackgroundImage(gachiModeState)
        var backgroundView = findViewById<ImageView>(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        }


    // Функция для обновления фона активности в зависимости от состояния переключателя
//    private fun updateBackgroundImage(isChecked: Boolean) {
//        val backgroundView = findViewById<ImageView>(R.id.background_image)
//        if (isChecked) {
//            backgroundView.setImageResource(R.drawable.mountains)
//        } else {
//            backgroundView.setImageResource(R.drawable.cat)
//        }
//    }

    override fun onResume() {
        super.onResume()
        val isKeyInputShown = sharedPreferences.getBoolean(MyCompObj.KEY_INPUT_SHOWN_KEY, false)
        val keySimbl = findViewById<Button>(R.id.button_login)

        if (!isKeyInputShown) {
            val displayIntent = Intent(this, KeyInputActivity::class.java)
            startActivity(displayIntent)
            sharedPreferences.edit().putBoolean(MyCompObj.KEY_INPUT_SHOWN_KEY, true).apply()
            keySimbl.setText("🔐")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                keySimbl.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yp_blue))
            } else {  // Для версий до Lollipop
                keySimbl.setBackgroundColor(ContextCompat.getColor(this, R.color.yp_blue))
            }
        }
    }
}