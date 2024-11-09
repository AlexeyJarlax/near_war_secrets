package com.pavlov.MyShadowGallery.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pavlov.MyShadowGallery.ui.theme.MyShadowGalleryTheme

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