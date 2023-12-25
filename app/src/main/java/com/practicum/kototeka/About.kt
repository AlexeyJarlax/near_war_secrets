package com.practicum.kototeka

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.practicum.kototeka.util.ThemeManager

class About : AppCompatActivity() {

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

        // КНОПКА политики конфиденциальности
        userAgreementButton.setOnClickListener {
            val url = getString(R.string.user_agreement_url)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}

