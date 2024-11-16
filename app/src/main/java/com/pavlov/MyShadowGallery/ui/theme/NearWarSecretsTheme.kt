package com.pavlov.MyShadowGallery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// моя палитра:
val MyBlack = Color(0xFF1C1E27)
val MyBlueLight = Color(0xFF9FBBF3)
//val MySecondaryBackground = colorResource(id = R.color.my_normal_blue)

private val LightColorPalette = lightColors(
    primary = Color(0xFF6200EE),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6)
)

private val DarkColorPalette = darkColors(
    primary = Color(0xFFBB86FC),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6)
)

val colors = LightColorPalette

@Composable
fun MyShadowGalleryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    MaterialTheme(
        colors = colors,
        typography = Typography(
            h1 = MaterialTheme.typography.h1.copy(color = MyBlack),
            h2 = MaterialTheme.typography.h2.copy(color = MyBlack),
            h3 = MaterialTheme.typography.h3.copy(color = MyBlack),
            h4 = MaterialTheme.typography.h4.copy(color = MyBlack),
            h5 = MaterialTheme.typography.h5.copy(color = MyBlack),
            h6 = MaterialTheme.typography.h6.copy(color = MyBlack),
            subtitle1 = MaterialTheme.typography.subtitle1.copy(color = MyBlack),
            subtitle2 = MaterialTheme.typography.subtitle2.copy(color = MyBlack),
            body1 = MaterialTheme.typography.body1.copy(color = MyBlack),
            body2 = MaterialTheme.typography.body2.copy(color = MyBlack),
            button = MaterialTheme.typography.button.copy(color = MyBlack),
            caption = MaterialTheme.typography.caption.copy(color = MyBlack),
            overline = MaterialTheme.typography.overline.copy(color = MyBlack)
        ),
        shapes = Shapes,
        content = content
    )
}