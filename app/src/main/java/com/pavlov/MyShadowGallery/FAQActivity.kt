package com.pavlov.MyShadowGallery

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.pavlov.MyShadowGallery.util.ThemeManager

class FAQActivity : AppCompatActivity() {

    private lateinit var videoView : VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)
        videoView = findViewById(R.id.videoView0)
//        val backMenuLayout = findViewById<LinearLayout>(R.id.act_abouttheapp_layout)
        val back = findViewById<Button>(R.id.button_back_from_abouttheapp)
        val buttonAddPicture = findViewById<Button>(R.id.button_how_to_add_picture)
        val buttonSharePicture = findViewById<Button>(R.id.button_settings_share)
        val buttonAcceptPicture = findViewById<Button>(R.id.button_how_to_accept_picture)
        val buttonSettSecure = findViewById<Button>(R.id.button_how_to_sett_secure)

            back.setOnClickListener { // КНОПКА НАЗАД
                finish()
            }

            buttonAddPicture.setOnClickListener {
                val resource = "android.resource://${packageName}/${R.raw.murcat}"
                videoPlay(resource)
            }

            buttonSharePicture.setOnClickListener {
                val resource = "android.resource://${packageName}/${R.raw.murcat}"
                videoPlay(resource)
            }

            buttonAcceptPicture.setOnClickListener {
                val resource = "android.resource://${packageName}/${R.raw.murcat}"
                videoPlay(resource)
            }

            buttonSettSecure.setOnClickListener {
                val resource = "android.resource://${packageName}/${R.raw.murcat}"
                videoPlay(resource)
            }

    }

    fun videoPlay (resource: String) {// применяем тему в старте: ночная
        videoView.visibility = View.VISIBLE
        videoView.alpha = 0.5f
//                backMenuLayout.alpha = 1.0f
        videoView.setVideoURI(Uri.parse(resource))
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = false
            mediaPlayer.setVolume(0.0f, 0.0f)
            mediaPlayer.start()
//                    scrollView()
        }
    }

//    fun scrollView() {
//        val scrollView = findViewById<ScrollView>(R.id.aboutTheAppActivity)
//        scrollView?.post {
//            scrollView.smoothScrollTo(0, 0) // Прокрутить вверх
//        }
//    }
}

