package com.pavlov.MyShadowGallery.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pavlov.MyShadowGallery.data.model.NavDestinations
import com.pavlov.MyShadowGallery.ui.about.AboutScreen
import com.pavlov.MyShadowGallery.ui.auth.AuthScreen
import com.pavlov.MyShadowGallery.ui.images.shared.SharedScreen
import com.pavlov.MyShadowGallery.ui.images.ImagesScreen
import com.pavlov.MyShadowGallery.ui.images.loaded.LoadedScreen
import com.pavlov.MyShadowGallery.ui.images.ImagesViewModel
import com.pavlov.MyShadowGallery.ui.keyinput.KeyInputScreen
import com.pavlov.MyShadowGallery.ui.setpassword.SetPasswordScreen
import com.pavlov.MyShadowGallery.ui.settings.SettingsScreen
import com.pavlov.MyShadowGallery.ui.storageLog.StorageLogScreen
import com.pavlov.MyShadowGallery.ui.twosteps.TwoStepsForSaveScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    activity: Activity,
    imagesViewModel: ImagesViewModel,
    modifier: Modifier = Modifier,
    intent: Intent?
) {
    // Обработка intent внутри компонуемой функции (из-за лагов вынес сюда из мейна)
    LaunchedEffect(intent) {
        intent?.let { receivedIntent ->
            val action = receivedIntent.action
            val type = receivedIntent.type

            when {
                // Обработка ACTION_SEND (обработка намерения поделиться изображением)
                action == Intent.ACTION_SEND && type != null && type.startsWith("image/") -> {
                    val uri: Uri? = receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM)
                    uri?.let {
                        imagesViewModel.addReceivedPhoto(it)
                        imagesViewModel.setAnImageWasSharedWithUsNow(true)
                        navController.navigate(NavDestinations.EXTRACTER) {
                            popUpTo(NavDestinations.IMAGES) { inclusive = false }
                        }
                    }
                }

                // Обработка ACTION_VIEW (а тут обработка намерения "открыть" изображение)
                action == Intent.ACTION_VIEW && type != null && type.startsWith("image/") -> {
                    val uri: Uri? = receivedIntent.data
                    uri?.let {
                        imagesViewModel.addReceivedPhoto(it)
                        imagesViewModel.setAnImageWasSharedWithUsNow(true)
                        navController.navigate(NavDestinations.EXTRACTER) {
                            popUpTo(NavDestinations.IMAGES) { inclusive = false }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(navController)) {
                BottomNavigationBar(navController, activity)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavDestinations.AUTH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavDestinations.AUTH) {
                AuthScreen(
                    onNavigateToItemLoader = { navController.navigate(NavDestinations.IMAGES) },
                    onNavigateToTwoStepsForSave = { navController.navigate(NavDestinations.TWO_STEPS_FOR_SAVE) }
                )
            }
            composable(NavDestinations.SET_PASSWORD) {
                SetPasswordScreen(
                    onPasswordSet = { navController.navigate(NavDestinations.IMAGES) }
                )
            }
            composable(NavDestinations.TWO_STEPS_FOR_SAVE) {
                TwoStepsForSaveScreen(
                    onNavigateToSetPassword = { navController.navigate(NavDestinations.SET_PASSWORD) },
                    onNavigateToMain = {
                        navController.navigate(NavDestinations.IMAGES) {
                            popUpTo(NavDestinations.TWO_STEPS_FOR_SAVE) { inclusive = true }
                        }
                    },
                    onNavigateToKeyInput = { navController.navigate(NavDestinations.KEY_INPUT) }
                )
            }
            composable(NavDestinations.KEY_INPUT) {
                KeyInputScreen(
                    onNavigateToMain = {
                        navController.navigate(NavDestinations.IMAGES) {
                            popUpTo(NavDestinations.KEY_INPUT) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavDestinations.IMAGES) {
                ImagesScreen(
                    itemLoaderScreen = { LoadedScreen(viewModel = imagesViewModel) },
                    extractedImagesScreen = { SharedScreen(viewModel = imagesViewModel) }
                )
            }

            composable(NavDestinations.LOADER) {
                LoadedScreen(viewModel = imagesViewModel)
            }

            composable(NavDestinations.EXTRACTER) {
                SharedScreen(viewModel = imagesViewModel)
            }

            composable(NavDestinations.STORAGE_LOG) {
                StorageLogScreen(navController)
            }

            composable(NavDestinations.SETTINGS) {
                SettingsScreen(
                    navController = navController,
                    onAboutClicked = { navController.navigate(NavDestinations.ABOUT) },
                    onSecuritySettingsClicked = { navController.navigate(NavDestinations.TWO_STEPS_FOR_SAVE) }
                )
            }
            composable(NavDestinations.ABOUT) {
                AboutScreen(navController)
            }
        }
    }
}

private fun shouldShowBottomBar(navController: NavHostController): Boolean {
    val currentDestination = navController.currentBackStackEntry?.destination?.route
    return when (currentDestination) {
        NavDestinations.AUTH, NavDestinations.KEY_INPUT, NavDestinations.SET_PASSWORD -> false
        else -> true
    }
}