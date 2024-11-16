package com.pavlov.MyShadowGallery

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.pavlov.MyShadowGallery.util.ThemeManager

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val videoView = findViewById<VideoView>(R.id.videoView0)
        val themeManager = ThemeManager
        val backMenuLayout = findViewById<LinearLayout>(R.id.act_abouttheapp_layout)
        var backgroundView = findViewById<ImageView>(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        val back = findViewById<Button>(R.id.button_back_from_abouttheapp)
        val shareButton = findViewById<Button>(R.id.button_settings_share)
        val helpButton = findViewById<Button>(R.id.button_settings_write_to_supp)
        val userAgreementButton = findViewById<Button>(R.id.button_settings_user_agreement)
        val developersPage = findViewById<Button>(R.id.developers_page)
        if (themeManager.isNightModeEnabled(this)) {// применяем тему в старте: ночная
            if (themeManager.isUserSwitchEnabled(this)) {
                videoView.visibility = View.GONE
                backMenuLayout.alpha = 1.0f
                backgroundView.alpha = 1.0f
            } else {
                videoView.visibility = View.VISIBLE
                videoView.alpha = 0.5f
                backMenuLayout.alpha = 1.0f
                backgroundView.alpha = 0.0f
                videoView.setVideoURI(Uri.parse("android.resource://${packageName}/${R.raw.murcat}"))
                videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    mediaPlayer.setVolume(0.0f, 0.0f)
                    mediaPlayer.start()
                }
            }
            scrollView()
        } else {  // дневная
            if (themeManager.isUserSwitchEnabled(this)) { // горы
                videoView.visibility = View.GONE
                backMenuLayout.alpha = 1.0f
                backgroundView.alpha = 0.5f
            } else { // котики
                videoView.visibility = View.GONE
                backMenuLayout.alpha = 1.0f
                backgroundView.alpha = 0.5f
            }
            scrollView()
        }

        // Получаем версию приложения из Gradle
        val appName = getString(R.string.app_name_in_main_page) // Название приложения из Gradle
        val appVersionText = getString(R.string.app_version_text) // "Версия приложения" из ресурсов строк
        val appVersion = getAppVersion() // Номер версии из Gradle
        val versionTextView: TextView = findViewById(R.id.version)
        versionTextView.text = "$appName $appVersionText $appVersion"

        back.setOnClickListener { // КНОПКА НАЗАД
            finish()
        }

        // КНОПКА ПОДЕЛИТЬСЯ
        shareButton.setOnClickListener {
            val appId = "com.pavlov.MyShadowGallery"
            val intent = Intent(Intent.ACTION_SEND)
            Intent.setType = "text/plain"
            intent.putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.share_app_text, appId)
            )
            startActivity(Intent.createChooser(intent, getString(R.string.share_app_title)))
        }

        // КНОПКА ТЕХПОДДЕРЖКИ
        helpButton.setOnClickListener {
            Intent(Intent.ACTION_SENDTO).apply {
                Intent.setData = Uri.parse(getString(R.string.support_email))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_email_text))
                startActivity(this)
            }
        }

        // КНОПКА политики конфиденциальности
        userAgreementButton.setOnClickListener {
            val url = getString(R.string.user_agreement_url)
            val intent = Intent(Intent.ACTION_VIEW)
            Intent.setData = Uri.parse(url)
            startActivity(intent)
        }

        // КНОПКА страницы разрабочика
        developersPage.setOnClickListener {
            val url = getString(R.string.developers_page_url)
            val intent = Intent(Intent.ACTION_VIEW)
            Intent.setData = Uri.parse(url)
            startActivity(intent)
        }
    }//конец ОнКриейт

    private fun getAppVersion(): String? {
        try {
            val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            return pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "N/A"
    }

    fun scrollView() {
        val scrollView = findViewById<ScrollView>(R.id.aboutTheAppActivity)
        scrollView?.post {
            scrollView.smoothScrollTo(0, 0) // Прокрутить вверх
        }
    }
}

