package com.pavlov.nearWarSecrets.ui.main

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.pavlov.nearWarSecrets.navigation.BottomNavigationBar
import com.pavlov.nearWarSecrets.ui.about.AboutScreen
import com.pavlov.nearWarSecrets.ui.itemLoader.ItemLoaderScreen
import com.pavlov.nearWarSecrets.ui.settings.SettingsScreen
import com.pavlov.nearWarSecrets.ui.storageLog.StorageLogScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "item_loader",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("item_loader") {
                ItemLoaderScreen()
            }
            composable("storage_log") {
                StorageLogScreen(navController)
            }
            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    onNavigateBack = { navController.popBackStack() },
                    onAboutClicked = { navController.navigate("about") },
                    onSecuritySettingsClicked = { navController.navigate("two_steps_for_save") }
                )
            }
            composable("about") {
                AboutScreen(navController)
            }
        }
    }
}