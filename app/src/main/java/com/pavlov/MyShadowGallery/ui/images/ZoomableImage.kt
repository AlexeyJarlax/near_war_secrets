package com.pavlov.MyShadowGallery.ui.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import android.net.Uri

/**
 * Компонент для отображения изображения с возможностью масштабирования и перемещения.
 */

@Composable
fun ZoomableImage(
    uri: Uri?,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Image(
        painter = rememberImagePainter(uri),
        contentDescription = "Zoomable Image",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f) // Ограничение масштаба от 1x до 5x
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    )
}