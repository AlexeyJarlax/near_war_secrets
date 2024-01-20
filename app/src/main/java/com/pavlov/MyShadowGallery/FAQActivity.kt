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
        videoView = findViewById(R.id.video_view_faq)
//        val backMenuLayout = findViewById<LinearLayout>(R.id.act_abouttheapp_layout)
        val buttonBack = findViewById<Button>(R.id.button_back_from_faq)
        val buttonAddPicture = findViewById<Button>(R.id.button_how_to_add_picture)
        val buttonSharePicture = findViewById<Button>(R.id.button_settings_share)
        val buttonAcceptPicture = findViewById<Button>(R.id.button_how_to_accept_picture)
        val buttonSettSecure = findViewById<Button>(R.id.button_how_to_sett_secure)
        val aboutTheDeveloperButton = findViewById<Button>(R.id.about_the_app)


        buttonBack.setOnClickListener { // КНОПКА НАЗАД
                finish()
            }

            buttonAddPicture.setOnClickListener {
                val resource = "android.resource://${packageName}/${R.raw.video_faq_make_picture}"
                videoPlay(resource)
//                videoView.visibility = View.GONE
            }

            buttonSharePicture.setOnClickListener {
                val resource = "android.resource://${packageName}/${R.raw.video_faq_send}"
                videoPlay(resource)
//                videoView.visibility = View.GONE
            }

            buttonAcceptPicture.setOnClickListener {
                val resource = "android.resource://${packageName}/${R.raw.video__faq_shere_back}"
                videoPlay(resource)
            }

            buttonSettSecure.setOnClickListener {
                val resource = "android.resource://${packageName}/${R.raw.video_faq_deff}"
                videoPlay(resource)
            }

        aboutTheDeveloperButton.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
    }

    fun videoPlay(resource: String) {
        videoView.visibility = View.VISIBLE
        videoView.setVideoURI(Uri.parse(resource))
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = false
            mediaPlayer.setVolume(0.0f, 0.0f)
            mediaPlayer.start()
        }

        videoView.setOnCompletionListener {
            // Событие завершения воспроизведения
            videoView.visibility = View.GONE
        }
    }

//    fun scrollView() {
//        val scrollView = findViewById<ScrollView>(R.id.aboutTheAppActivity)
//        scrollView?.post {
//            scrollView.smoothScrollTo(0, 0) // Прокрутить вверх
//        }
//    }
}

