package com.pavlov.MyShadowGallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pavlov.MyShadowGallery.ui.MyShadowGalleryTheme

class MainPageActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyShadowGalleryTheme {
                MainPageScreen()
            }
        }
    }
}