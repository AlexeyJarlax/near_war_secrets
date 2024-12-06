package com.pavlov.nearWarSecrets.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: IconType
)

sealed class IconType {
    data class VectorIcon(val imageVector: ImageVector) : IconType()
    data class ResourceIcon(val resourceId: Int) : IconType()
}

/** Если нужно использовать ImageVector:

BottomNavItem(
title = "Info",
route = "info",
icon = IconType.VectorIcon(Icons.Default.Info)
)

Если нужно использовать ресурс:

BottomNavItem(
title = "Door",
route = "door",
icon = IconType.ResourceIcon(R.drawable.door_open_30dp)
)
*/