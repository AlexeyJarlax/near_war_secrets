package com.pavlov.MyShadowGallery.navigation

import android.app.Activity
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.data.model.BottomNavItem
import com.pavlov.MyShadowGallery.data.model.IconType
import com.pavlov.MyShadowGallery.data.model.NavDestinations

@Composable
fun BottomNavigationBar(navController: NavHostController, activity: Activity) {
    val items = listOf(
        BottomNavItem(
            "Фото",
            NavDestinations.IMAGES,
            icon = IconType.VectorIcon(Icons.Default.Image)
        ),
        BottomNavItem(
            "Журнал",
            NavDestinations.STORAGE_LOG,
            icon = IconType.VectorIcon(Icons.Default.Storage)
        ),
        BottomNavItem(
            "Опции",
            NavDestinations.SETTINGS,
            icon = IconType.VectorIcon(Icons.Default.Settings)
        ),
        BottomNavItem("FAQ", NavDestinations.ABOUT, icon = IconType.VectorIcon(Icons.Default.Info)),
        BottomNavItem(
            "Выход",
            NavDestinations.EXIT,
            icon = IconType.ResourceIcon(R.drawable.door_open_30dp)
        )
    )
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                modifier = Modifier.weight(1f),
                icon = {
                    when (val iconType = item.icon) {
                        is IconType.VectorIcon -> Icon(
                            imageVector = iconType.imageVector,
                            contentDescription = null
                        )

                        is IconType.ResourceIcon -> Icon(
                            painter = painterResource(id = iconType.resourceId),
                            contentDescription = null
                        )
                    }
                },
                label = { Text(item.title, maxLines = 1) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == NavDestinations.EXIT) {
                        activity.finish()
                    } else if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = item.route != NavDestinations.SETTINGS
                            popUpTo(
                                navController.graph.startDestinationRoute ?: NavDestinations.AUTH
                            ) {
                                saveState = item.route != NavDestinations.SETTINGS
                            }
                        }
                    }
                }
            )
        }
    }
}