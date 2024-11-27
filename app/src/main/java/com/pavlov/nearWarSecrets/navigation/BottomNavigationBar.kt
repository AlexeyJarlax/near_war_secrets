package com.pavlov.nearWarSecrets.navigation

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pavlov.nearWarSecrets.data.model.BottomNavItem

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Loader", "item_loader", Icons.Default.Add),
        BottomNavItem("Log", "storage_log", Icons.Default.Storage),
        BottomNavItem("Settings", "settings", Icons.Default.Settings),
        BottomNavItem("About", "about", Icons.Default.Info)
    )
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Избегаем множественного создания экземпляров
                        launchSingleTop = true
                        // Сохраняем состояние при возврате
                        restoreState = item.route != "settings"
                        // Remove previous destinations from the stack
                        popUpTo(navController.graph.startDestinationRoute ?: "main") {
                            saveState = item.route != "settings"
                        }
                    }
                }
            )
        }
    }
}