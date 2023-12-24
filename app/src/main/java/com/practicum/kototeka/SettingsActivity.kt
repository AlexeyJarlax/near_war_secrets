package com.practicum.kototeka

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.practicum.kototeka.util.AppPreferencesKeys
import com.practicum.kototeka.util.AppPreferencesKeysMethods
import com.practicum.kototeka.util.ThemeManager

class SettingsActivity : AppCompatActivity() {
    @SuppressLint("UseSwitchCompatOrMaterialCode")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_settings)
        val savedContext = this
//        appPreferencesKeysMethods = AppPreferencesKeysMethods(context = this)
        val videoView = findViewById<VideoView>(R.id.videoView)
        val themeManager = ThemeManager
        val backMenuLayout = findViewById<LinearLayout>(R.id.act_settings_layout)
        var backgroundView = findViewById<ImageView>(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        val back = findViewById<Button>(R.id.button_back_from_settings)
        val resetSettings = findViewById<Button>(R.id.reset_settings) // сброс настроек
        val buttonClearStorage = findViewById<Button>(R.id.clearing_the_storage) // удал всех файлов
        val useTheEncryptionKey: SwitchCompat = findViewById(R.id.use_the_encryption_key)
        val delEKWhenClosingTheSession: SwitchCompat =
            findViewById(R.id.delete_the_encryption_key_when_closing_the_session)
        val switchDarkMode: SwitchCompat = findViewById(R.id.switch_dark_mode)
        val switchCompat: SwitchCompat = findViewById(R.id.button_tumbler)
        val shareButton = findViewById<Button>(R.id.button_settings_share)
        val helpButton = findViewById<Button>(R.id.button_settings_write_to_supp)
        val userAgreementButton = findViewById<Button>(R.id.button_settings_user_agreement)
        val mimicrySwitch: SwitchCompat = findViewById(R.id.disguise)
        val aboutTheDeveloperButton: Button = findViewById(R.id.about_the_app)
        // Загрузка сохраненных значений
        useTheEncryptionKey.isChecked =
            AppPreferencesKeysMethods(context = this).loadSwitchValue(AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_KLUCHIK)
        delEKWhenClosingTheSession.isChecked =
            AppPreferencesKeysMethods(context = this).loadSwitchValue(AppPreferencesKeys.KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION)
        mimicrySwitch.isChecked =
            AppPreferencesKeysMethods(context = this).loadSwitchValue(AppPreferencesKeys.KEY_MIMICRY_SWITCH)
        // Загрузка ебаного SeekBar с его конченными значениями
        val previewSizeSeekBar: SeekBar = findViewById(R.id.preview_size)
        val imagePreviewForSeekbar1: ImageView = findViewById(R.id.image_preview_for_seekbar_1)
        val imagePreviewForSeekbar2: ImageView = findViewById(R.id.image_preview_for_seekbar_2)
        val imagePreviewForSeekbar3: ImageView = findViewById(R.id.image_preview_for_seekbar_3)
        previewSizeSeekBar.progress = 1
        val sizeLabel = findViewById<TextView>(R.id.preview_size_label)
        val previewScalingFactorLabel: String by lazy {
            getString(R.string.preview_scaling_factor)
        }
        val previewSizeSeekBarProgress =
            AppPreferencesKeysMethods(context = this).loadPreviewSizeValue(AppPreferencesKeys.KEY_PREVIEW_SIZE_SEEK_BAR)
        previewSizeSeekBar.post {
            previewSizeSeekBar.progress = previewSizeSeekBarProgress
            sizeLabel.text = "$previewScalingFactorLabel ${previewSizeSeekBarProgress}x${previewSizeSeekBarProgress}"
        }

        if (themeManager.isNightModeEnabled(this)) {// применяем тему в старте: ночная
            if (themeManager.isUserSwitchEnabled(this)) { // горы
                videoView.alpha = 0.0f
                backMenuLayout.alpha = 1.0f
                backgroundView.alpha = 1.0f
            } else { // котики
                videoView.alpha = 0.5f
                backMenuLayout.alpha = 1.0f
                backgroundView.alpha = 0.0f
                videoView.setVideoURI(Uri.parse("android.resource://${packageName}/${R.raw.murcat}"))
                videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    // Уменьшаем звук в два раза
                    mediaPlayer.setVolume(0.0f, 0.0f)
                    mediaPlayer.start()
                }
            }
        } else {  // дневная
            if (themeManager.isUserSwitchEnabled(this)) { // горы
                videoView.alpha = 0.0f
                backMenuLayout.alpha = 1.0f
                backgroundView.alpha = 0.5f
            } else { // котики
                videoView.alpha = 0.0f
                backMenuLayout.alpha = 1.0f
                backgroundView.alpha = 0.5f
            }
        }

        back.setOnClickListener { // КНОПКА НАЗАД
            finish()
        }

        resetSettings.setOnClickListener { // сброс настроек
            resetSettings(this, AppPreferencesKeys.PREFS_NAME)
            val intent = Intent(applicationContext, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        buttonClearStorage.setOnClickListener { // чистим хранилище
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

        // Использовать ключ шифрования
        useTheEncryptionKey.setOnCheckedChangeListener { _, isChecked -> // Использовать ключ шифрования
            AppPreferencesKeysMethods(context = this).saveSwitchValue(
                AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_KLUCHIK,
                isChecked
            )
        }

        previewSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = progress + 1
                sizeLabel.text = "$previewScalingFactorLabel ${size}x${size}"

                // Загружаем изображения из файла и масштабируем его
                val originalBitmap1 = BitmapFactory.decodeResource(resources, R.drawable.m100)
                val originalBitmap2 = BitmapFactory.decodeResource(resources, R.drawable.c100)
                val originalBitmap3 = BitmapFactory.decodeResource(resources, R.drawable.t100)
                val scaledBitmap1 = Bitmap.createScaledBitmap(originalBitmap1, size, size, false)
                val scaledBitmap2 = Bitmap.createScaledBitmap(originalBitmap2, size, size, false)
                val scaledBitmap3 = Bitmap.createScaledBitmap(originalBitmap3, size, size, false)
                imagePreviewForSeekbar1.setImageBitmap(scaledBitmap1)
                imagePreviewForSeekbar2.setImageBitmap(scaledBitmap2)
                imagePreviewForSeekbar3.setImageBitmap(scaledBitmap3)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Ничего не делаем
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    val size = it.progress + 1
                    sizeLabel.text = "$previewScalingFactorLabel ${size}x${size}"
                    AppPreferencesKeysMethods(context = savedContext).savePreviewSizeValue(
                        AppPreferencesKeys.KEY_PREVIEW_SIZE_SEEK_BAR,
                        size
                    )
                }
            }
        })

        // ТУМБЛЕР НОЧНОЙ И ДНЕВНОЙ ТЕМЫ (РЕАЛИЗАЦИЯ ВЫНЕСЕНА В ОТДЕЛЬНЫЙ КЛАСС)
        switchDarkMode.isChecked = ThemeManager.isNightModeEnabled(this)
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            ThemeManager.setNightModeEnabled(this, isChecked)
            ThemeManager.applyTheme(this) // Добавьте эту строку здесь
        }

        // Пользовательский стиль
        switchCompat.isChecked = ThemeManager.isUserSwitchEnabled(this)
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    switchCompat.thumbTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.mount_thumb_color))
                } else {  // Для версий до Lollipop
                    switchCompat.setBackgroundColor(
                        ContextCompat.getColor(
                            this,
                            R.color.mount_thumb_color
                        )
                    )
                }
                val resultIntent = Intent()
                resultIntent.putExtra(AppPreferencesKeys.KEY_USER_SWITCH, isChecked)
                setResult(RESULT_OK, resultIntent)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    switchCompat.thumbTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.kototeka_thumb_color))
                } else {  // Для версий до Lollipop
                    switchCompat.setBackgroundColor(
                        ContextCompat.getColor(
                            this,
                            R.color.kototeka_thumb_color
                        )
                    )
                }
                val resultIntent = Intent()
                resultIntent.putExtra(AppPreferencesKeys.KEY_USER_SWITCH, isChecked)
                setResult(RESULT_OK, resultIntent)
            }
            ThemeManager.saveUserSwitch(this, isChecked)
            ThemeManager.applyUserSwitch(this)
            recreate()
        }


        // КНОПКА ПОДЕЛИТЬСЯ
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
        helpButton.setOnClickListener {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(getString(R.string.support_email))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_email_text))
                startActivity(this)
            }
        }

        // КНОПКА ПОЛЬЗОВАТЕЛЬСКОГО СОГЛАШЕНИЯ
        userAgreementButton.setOnClickListener {
            val url = getString(R.string.user_agreement_url)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        // Обработка события для Удалить ключ при закрытии сессии
        delEKWhenClosingTheSession.setOnCheckedChangeListener { _, isChecked ->
            AppPreferencesKeysMethods(context = this).saveSwitchValue(
                AppPreferencesKeys.KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION,
                isChecked
            )
        }

        // Обработка события для Маскировки
        mimicrySwitch.setOnCheckedChangeListener { _, isChecked ->
            AppPreferencesKeysMethods(context = this).saveSwitchValue(
                AppPreferencesKeys.KEY_MIMICRY_SWITCH,
                isChecked
            )
        }

        // Обработка события для: О разработчике
        aboutTheDeveloperButton.setOnClickListener {

        }
    }

    fun resetSettings(context: Context, sharedPreferencesName: String) {
        val sharedPreferences =
            context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

}
