package com.pavlov.nearWarSecrets.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pavlov.nearWarSecrets.navigation.NavGraph
import com.pavlov.nearWarSecrets.ui.theme.MyShadowGalleryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyShadowGalleryTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    activity = this,
                    modifier = Modifier
                )
            }
        }
    }
}