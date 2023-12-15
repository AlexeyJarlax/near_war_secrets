package com.practicum.kototeka

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.practicum.kototeka.util.ThemeManager


class SettingsActivity : AppCompatActivity() {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_settings)

        val videoView = findViewById<VideoView>(R.id.videoView)
        val themeManager = ThemeManager
        val backMenuLayout = findViewById<LinearLayout>(R.id.act_settings_layout)

        if (themeManager.isNightModeEnabled(this)) {
            videoView.alpha = 0.5f
            backMenuLayout.alpha = 0.5f
            videoView.setVideoURI(Uri.parse("android.resource://${packageName}/${R.raw.murcat}"))
            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
                // Уменьшаем звук в два раза
                mediaPlayer.setVolume(0.0f, 0.0f)
                mediaPlayer.start()
            }
        } else {
        }

        val button_weather = findViewById<Button>(R.id.button_weather)
        val back = findViewById<Button>(R.id.button_back_from_settings) // КНОПКА НАЗАД
        back.setOnClickListener {
            finish()
        }

        val buttonClearStorage = findViewById<Button>(R.id.clearing_the_storage) // Функция для удаления всех файлов из хранилища приложения
        buttonClearStorage.setOnClickListener {
            val externalFilesDir = getExternalFilesDir(null)
            val fileList = externalFilesDir?.listFiles()
            if (fileList != null) {
                for (file in fileList) {
                    file.delete()
                }
            }
            val internalFilesDir = filesDir
            val internalFileList = internalFilesDir?.listFiles()
            if (internalFileList != null) {
                for (file in internalFileList) {
                    file.delete()
                }
            }
            val originalAndPreviews = getDir("originalAndPreviews", Context.MODE_PRIVATE)
            val originalAndPreviewsList = originalAndPreviews?.listFiles()
            if (originalAndPreviewsList != null) {
                for (file in originalAndPreviewsList) {
                    file.delete()
                }
            }
            Toast.makeText(this, "Хранилище очищено!", Toast.LENGTH_SHORT).show()
        }


        val seekBar = findViewById<SeekBar>(R.id.preview_size) // seekBar размера для привью
        val sizeLabel = findViewById<TextView>(R.id.preview_size_label)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = when (progress) {
                    0 -> 0
                    1 -> 50
                    2 -> 100
                    3 -> 200
                    else -> 400
                }
                sizeLabel.text = "Размер превью: $size"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Ничего не делаем
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Ничего не делаем
            }
        })

        // ТУМБЛЕР НОЧНОЙ И ДНЕВНОЙ ТЕМЫ (РЕАЛИЗАЦИЯ ВЫНЕСЕНА В ОТДЕЛЬНЫЙ КЛАСС)
        val switchDarkMode: SwitchCompat = findViewById(R.id.switch_dark_mode)
        switchDarkMode.isChecked = ThemeManager.isNightModeEnabled(this)
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            ThemeManager.setNightModeEnabled(this, isChecked)
            ThemeManager.applyTheme(this) // Добавьте эту строку здесь
        }

        // ТУМБЛЕР СТИЛЯ\ТЕМЫ КОТОТЕКА\ГАЧИМУЧИ
        val switchCompat: SwitchCompat = findViewById(R.id.button_tumbler)
        switchCompat.isChecked = ThemeManager.getSwitchState(this)

        switchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                switchCompat.thumbTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.gachimuchi_thumb_color))
                // Изменить фон активности в состоянии "Гачимучи"
                val resultIntent = Intent()
                resultIntent.putExtra("switchState", isChecked)
                setResult(RESULT_OK, resultIntent)
            } else {
                switchCompat.thumbTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.kototeka_thumb_color))
                val resultIntent = Intent()
                resultIntent.putExtra("switchState", isChecked)
                setResult(RESULT_OK, resultIntent)
            }
            ThemeManager.saveSwitchState(this, isChecked)
        }

        // КНОПКА ПОДЕЛИТЬСЯ
        val shareButton = findViewById<Button>(R.id.button_settings_share)
        shareButton.setOnClickListener {
            val appId = "com.Practicum.kototeka"
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.share_app_text, appId)
            )
            startActivity(Intent.createChooser(intent, getString(R.string.share_app_title)))
        }

        // КНОПКА ТЕХПОДДЕРЖКИ
        val helpButton = findViewById<Button>(R.id.button_settings_write_to_supp)
        helpButton.setOnClickListener {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(getString(R.string.support_email))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_email_text))
                startActivity(this)
            }
        }

        // КНОПКА ПОЛЬЗОВАТЕЛЬСКОГО СОГЛАШЕНИЯ
        val userAgreementButton = findViewById<Button>(R.id.button_settings_user_agreement)
        userAgreementButton.setOnClickListener {
            val url = getString(R.string.user_agreement_url)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        // КНОПКА ПОГОДА
        button_weather.setOnClickListener {

        }
    }
}