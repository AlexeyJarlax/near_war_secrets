package com.pavlov.MyShadowGallery

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.pavlov.MyShadowGallery.util.AppPreferencesKeys
import com.pavlov.MyShadowGallery.util.AppPreferencesKeysMethods
import com.pavlov.MyShadowGallery.util.ThemeManager
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    //    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var buttonClearStorage: Button
    private lateinit var resetSettings: Button
    private lateinit var personalData: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val savedContext = this
//        val videoView = findViewById<VideoView>(R.id.videoView)
//        val themeManager = ThemeManager
        val backMenuLayout = findViewById<LinearLayout>(R.id.act_settings_layout)
        var backgroundView = findViewById<ImageView>(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        val aboutTheDeveloperButton = findViewById<Button>(R.id.about_the_app)
        val languageOptions = findViewById<Button>(R.id.language_options)
        val back = findViewById<Button>(R.id.button_back_from_settings)
        val useTheEncryptionKey: SwitchCompat = findViewById(R.id.use_the_encryption_key)
        val delEKWhenClosingTheSession: SwitchCompat =
            findViewById(R.id.delete_the_encryption_key_when_closing_the_session)
        val switchDarkMode: SwitchCompat = findViewById(R.id.switch_dark_mode)
        val switchCompat: SwitchCompat = findViewById(R.id.button_tumbler)
        val mimicrySwitch: SwitchCompat = findViewById(R.id.disguise)
        buttonClearStorage = findViewById<Button>(R.id.clearing_the_storage) // удал всех файлов
        clearStorage()
        resetSettings = findViewById<Button>(R.id.reset_settings) // сброс настроек
        personalData = findViewById<Button>(R.id.personal_data) // персональный данные
        resetSettingsAndClearStorage()
        // Загрузка сохраненных значений
        useTheEncryptionKey.isChecked =
            AppPreferencesKeysMethods(context = this).loadSwitchValue(AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_KLUCHIK)
        delEKWhenClosingTheSession.isChecked =
            AppPreferencesKeysMethods(context = this).loadSwitchValue(AppPreferencesKeys.KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION)
        mimicrySwitch.isChecked =
            AppPreferencesKeysMethods(context = this).loadSwitchValue(AppPreferencesKeys.KEY_EXIST_OF_MIMICRY)
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
            AppPreferencesKeysMethods(context = this)
                .loadPreviewSizeValue(AppPreferencesKeys.KEY_PREVIEW_SIZE_SEEK_BAR)
                ?: AppPreferencesKeys.DEFAULT_PREVIEW_SIZE
        previewSizeSeekBar.post {
            previewSizeSeekBar.progress = previewSizeSeekBarProgress
            sizeLabel.text =
                "$previewScalingFactorLabel ${previewSizeSeekBarProgress}x${previewSizeSeekBarProgress}"
        }

        back.setOnClickListener { // КНОПКА НАЗАД
            finish()
        }

        var clickCount = 0
        personalData.setOnClickListener {
            when (clickCount % 2) {
                0 -> personalData.text =
                    "Чувствительная информация и персональные данные могут быть полностью удалены нажатием кнопки ОЧИСТИТЬ ВСЕ ДАННЫЕ, расположенной выше"

                1 -> personalData.text = getString(R.string.personal_data)
            }
            clickCount++
        }

        aboutTheDeveloperButton.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        languageOptions.setOnClickListener {
            showLanguageSelectionDialog()
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
                AppPreferencesKeys.KEY_EXIST_OF_MIMICRY,
                isChecked
            )
        }

    } // конец onCreate

    fun clearStorage() {
        buttonClearStorage.setOnClickListener { // чистим хранилище
            doClearStorage(applicationContext)
        }
    }

    fun resetSettingsAndClearStorage() { // Сброс настроек
        resetSettings.setOnClickListener { // сброс настроек
            doClearStorage(applicationContext)
            doResetSettingsAndClearStorage(applicationContext)
            finishAffinity(applicationContext)
        }
    }

    companion object {
        fun doClearStorage(context: Context) {
            val externalFilesDir = context.filesDir
            val fileList = externalFilesDir?.listFiles()
            if (fileList != null) {
                for (file in fileList) {
                    file.delete()
                }
            }
            val internalFilesDir = context.filesDir
            val internalFileList = internalFilesDir?.listFiles()
            if (internalFileList != null) {
                for (file in internalFileList) {
                    file.delete()
                }
            }
            val originalAndPreviews = context.getDir("originalAndPreviews", Context.MODE_PRIVATE)
            val originalAndPreviewsList = originalAndPreviews?.listFiles()
            if (originalAndPreviewsList != null) {
                for (file in originalAndPreviewsList) {
                    file.delete()
                }
            }
            Toast.makeText(context, "Хранилище очищено!", Toast.LENGTH_SHORT).show()
        }

        fun doResetSettingsAndClearStorage(context: Context) {
            // Удаление обычного (нешифрованного) хранилища
            val nonEncryptedSharedPreferences: SharedPreferences =
                context.getSharedPreferences(
                    AppPreferencesKeys.PREFS_NAME,
                    Context.MODE_PRIVATE
                )
            nonEncryptedSharedPreferences.edit().clear().apply()

        // Удаление шифрованного хранилища
            val encryptedSharedPreferences: SharedPreferences =
                context.getSharedPreferences(
                    AppPreferencesKeys.MY_SECRETS_PREFS_NAME,
                    Context.MODE_PRIVATE
                )
            encryptedSharedPreferences.edit().clear().apply()

        }

        fun finishAffinity(context: Context) {
            val intent = Intent(context, MainPageActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }


    private fun showLanguageSelectionDialog() {
        val languageOptions = arrayOf("Русский", "English", "汉语")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выберите язык приложения")

        builder.setItems(languageOptions) { dialog, which ->
            // Обработка выбора языка
            when (which) {
                0 -> setAppLanguage("ru")
                1 -> setAppLanguage("en")
                2 -> setAppLanguage("zh")
            }
            dialog.dismiss()

            // Обновление языка приложения
            updateAppLanguage()
        }

        builder.show()
    }

    private fun setAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.setLocale(locale)

        val resources = resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun updateAppLanguage() {
        val intent = Intent(this, MainPageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

//    private fun setAppLanguage(languageCode: String) {
//        val locale = Locale(languageCode)
//        Locale.setDefault(locale)
//        val config = Configuration(resources.configuration)
//        config.setLocale(locale)
//
//        // Сохранение выбранного языка в настройках приложения (если необходимо)
//        val preferences = getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
//        preferences.edit().putString(AppPreferencesKeys.APP_LANGUAGE, languageCode).apply()
//        // Обновление ресурсов приложения
//
//        // Перезапуск активити, чтобы применить изменения
//        val newContext = createConfigurationContext(config)
//        startActivity(
//            Intent(
//                this,
//                MainPageActivity::class.java
//            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        )
//        finish()
//    }

}
