//package com.pavlov.MyShadowGallery.ui.item_loader
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.viewModels
//import com.pavlov.MyShadowGallery.ui.theme.MyShadowGalleryTheme
//
//class ItemLoaderActivity : ComponentActivity() {
//
//    companion object {
//        const val EXTRA_MODE = "extra_mode"
//        const val MODE_FULL = "mode_full"
//        const val MODE_STORAGE = "mode_storage"
//    }
//
//    private val viewModel: ItemLoaderViewModel by viewModels()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Получаем режим из Intent
//        val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_FULL
//
//        setContent {
//            MyShadowGalleryTheme {
//                ItemLoaderScreen(
//                    viewModel = viewModel,
//                    onBack = { finish() },
//                    mode = mode
//                )
//            }
//        }
//    }
//}