package com.pavlov.MyShadowGallery.theme.uiComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Правильный импорт
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import com.pavlov.MyShadowGallery.R

val colorResId: Int = R.color.my_normal_blue
val alpha: Float = 0.5f

@Composable
fun BackgroundImage(
    imageResId: Int,
) {
    Image(
        painter = painterResource(imageResId),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
    )
}

@Composable
fun BackgroundImageWithDarkOverlay(
    imageResId: Int,
    overlayColor: Color = Color.Black.copy(alpha = 0.3f)
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayColor)
        )
    }
}

@Composable
fun SolidBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = colorResId).copy(alpha = alpha))
    )
}
