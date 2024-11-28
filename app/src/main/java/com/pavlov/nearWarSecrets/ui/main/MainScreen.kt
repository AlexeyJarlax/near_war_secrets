package com.pavlov.nearWarSecrets.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.pavlov.nearWarSecrets.data.model.NavDestinations
import com.pavlov.nearWarSecrets.navigation.BottomNavigationBar
import com.pavlov.nearWarSecrets.ui.about.AboutScreen
import com.pavlov.nearWarSecrets.ui.itemLoader.ItemLoaderScreen
import com.pavlov.nearWarSecrets.ui.settings.SettingsScreen
import com.pavlov.nearWarSecrets.ui.storageLog.StorageLogScreen

@Composable
fun MainScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavDestinations.AUTH,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(NavDestinations.ITEM_LOADER) { ItemLoaderScreen() }
            composable(NavDestinations.STORAGE_LOG) { StorageLogScreen(navController) }
            composable(NavDestinations.SETTINGS) {
                SettingsScreen(
                    navController = navController,
                    onNavigateBack = { navController.popBackStack() },
                    onAboutClicked = { navController.navigate(NavDestinations.ABOUT) },
                    onSecuritySettingsClicked = { navController.navigate(NavDestinations.TWO_STEPS_FOR_SAVE) }
                )
            }
            composable(NavDestinations.ABOUT) { AboutScreen(navController) }
        }
    }
}