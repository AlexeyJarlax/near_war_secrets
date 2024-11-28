package com.pavlov.nearWarSecrets.navigation

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pavlov.nearWarSecrets.data.model.NavDestinations
import com.pavlov.nearWarSecrets.ui.about.AboutScreen
import com.pavlov.nearWarSecrets.ui.auth.AuthScreen
import com.pavlov.nearWarSecrets.ui.itemLoader.ItemLoaderScreen
import com.pavlov.nearWarSecrets.ui.keyinput.KeyInputScreen
import com.pavlov.nearWarSecrets.ui.main.MainScreen
import com.pavlov.nearWarSecrets.ui.setpassword.SetPasswordScreen
import com.pavlov.nearWarSecrets.ui.settings.SettingsScreen
import com.pavlov.nearWarSecrets.ui.storageLog.StorageLogScreen
import com.pavlov.nearWarSecrets.ui.twosteps.TwoStepsForSaveScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    activity: Activity,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(navController)) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavDestinations.ITEM_LOADER,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavDestinations.AUTH) {
                AuthScreen(
                    onNavigateToMain = { navController.navigate(NavDestinations.MAIN) },
                    onNavigateToTwoStepsForSave = { navController.navigate(NavDestinations.TWO_STEPS_FOR_SAVE) }
                )
            }
            composable(NavDestinations.MAIN) {
                MainScreen(navController) // MainScreen больше не нужен как Scaffold
            }
            composable(NavDestinations.SET_PASSWORD) {
                SetPasswordScreen(
                    onPasswordSet = { navController.navigate(NavDestinations.TWO_STEPS_FOR_SAVE) }
                )
            }
            composable(NavDestinations.TWO_STEPS_FOR_SAVE) {
                TwoStepsForSaveScreen(
                    onNavigateToSetPassword = { navController.navigate(NavDestinations.SET_PASSWORD) },
                    onNavigateToMain = {
                        navController.navigate(NavDestinations.MAIN) {
                            popUpTo(NavDestinations.TWO_STEPS_FOR_SAVE) { inclusive = true }
                        }
                    },
                    onNavigateToKeyInput = { navController.navigate(NavDestinations.KEY_INPUT) }
                )
            }
            composable(NavDestinations.KEY_INPUT) {
                KeyInputScreen(
                    onNavigateToMain = {
                        navController.navigate(NavDestinations.MAIN) {
                            popUpTo(NavDestinations.KEY_INPUT) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavDestinations.ITEM_LOADER) {
                ItemLoaderScreen()
            }
            composable(NavDestinations.STORAGE_LOG) {
                StorageLogScreen(navController)
            }
            composable(NavDestinations.SETTINGS) {
                SettingsScreen(
                    navController = navController,
                    onNavigateBack = { navController.popBackStack() },
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

// показывать ли нижнее меню
private fun shouldShowBottomBar(navController: NavHostController): Boolean {
    return true
}