package com.pavlov.MyShadowGallery

/**
 * === СТРУКТУРА ПРОЕКТА: ===
 *
 * Presentation Layer.
 * Сингл активити, без фрагментов. Presentation содержит ViewModel, который
 * взаимодействует с View (написан на компоуз) и вызывает Use Cases.
 *
 * Domain Layer.
 * Содержит Use Cases, которые реализуют бизнес-логику приложения.
 *
 * Data Layer.
 * Содержит Repositories, которые отвечают за получение и сохранение данных.
 *
 * === Дополнительно: ===
 *
 *  Theme.
 *  Сделал @Composable без xml представлений, Dark по умолчанию для любой системной настройки,
 *  так как визуально Light не вписывается в стиль (черный экран и падающие шрифты)
 *
 *  Навигация.
 *  NavGraph тоже @Composable. intent на входящие запросы от других приложений вместо мейн-активити
 *  завёл в навигационный граф.
 *
 *  DI.
 *  Dagger Hilt.
 */

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pavlov.MyShadowGallery.navigation.NavGraph
import com.pavlov.MyShadowGallery.theme.MyTheme
import com.pavlov.MyShadowGallery.ui.images.ImagesViewModel
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
                    imagesViewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    intent = intent
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setContent {
            MyTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    activity = this,
                    imagesViewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    intent = intent
                )
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
//            hideSystemUI()
        }
    }

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
