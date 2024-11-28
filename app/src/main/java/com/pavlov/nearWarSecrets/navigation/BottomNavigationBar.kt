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
import com.pavlov.nearWarSecrets.data.model.NavDestinations

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Loader", NavDestinations.ITEM_LOADER, Icons.Default.Add),
        BottomNavItem("Log", NavDestinations.STORAGE_LOG, Icons.Default.Storage),
        BottomNavItem("Settings", NavDestinations.SETTINGS, Icons.Default.Settings),
        BottomNavItem("About", NavDestinations.ABOUT, Icons.Default.Info)
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
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = item.route != NavDestinations.SETTINGS
                            popUpTo(navController.graph.startDestinationRoute ?: NavDestinations.MAIN) {
                                saveState = item.route != NavDestinations.SETTINGS
                            }
                        }
                    }
                }
            )
        }
    }
}