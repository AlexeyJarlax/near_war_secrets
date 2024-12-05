package com.pavlov.nearWarSecrets

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.pavlov.nearWarSecrets.data.model.NavDestinations
import com.pavlov.nearWarSecrets.navigation.NavGraph
import com.pavlov.nearWarSecrets.theme.MyTheme
import com.pavlov.nearWarSecrets.ui.Images.ImagesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ImagesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    activity = this,
                    imagesVewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    intent = intent // Передаем intent в NavGraph
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Обновляем intent и перенастраиваем компоновку
        setContent {
            MyTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    activity = this,
                    imagesVewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    intent = intent // Передаем новый intent
                )
            }
        }
    }

    /**
     * Function to handle incoming intents.
     * Accepts images shared from other apps.
     */
//    private fun handleIntent(intent: Intent?) {
//        if (intent == null) return
//
//        val action = intent.action
//        val type = intent.type
//
//        if (action == Intent.ACTION_SEND && type != null) {
//            if (type.startsWith("image/")) {
//                val uri: Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)
//                uri?.let {
//                    viewModel.addReceivedPhoto(it)
//                    navController.navigate(NavDestinations.EXTRACTER) {
//                        popUpTo(NavDestinations.IMAGES) { inclusive = false }
//                    }
//                }
//            }
//        } else if (action == Intent.ACTION_SEND_MULTIPLE && type != null) {
//            if (type.startsWith("image/")) {
//                val uris: ArrayList<Uri>? = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
//                uris?.let {
//                    viewModel.addReceivedPhotos(it)
//                    navController.navigate(NavDestinations.EXTRACTER) {
//                        popUpTo(NavDestinations.IMAGES) { inclusive = false }
//                    }
//                }
//            }
//        }
//    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    /**
     * Function to hide system UI (status bar and navigation bar).
     */
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(
                android.view.WindowInsets.Type.statusBars() or
                        android.view.WindowInsets.Type.navigationBars()
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }
}
