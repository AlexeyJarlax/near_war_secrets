package com.pavlov.MyShadowGallery.theme.uiComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/** возможность зажать пальец на кнопке, чтобы получить подсказку (пока не решил, где хочу применить)*/

@Composable
fun AccessibleButton(
    modifier: Modifier = Modifier,
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var showTooltip by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        showTooltip = true
                    },
                    onPress = {
                        tryAwaitRelease()
                        showTooltip = false
                    }
                )
            }
    ) {
        Button(onClick = onClick) {
            content()
        }
        if (showTooltip) {
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(8.dp)
            ) {
                Text(
                    text = contentDescription,
                    color = Color.White,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}