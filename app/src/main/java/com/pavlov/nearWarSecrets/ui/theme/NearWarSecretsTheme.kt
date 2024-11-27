package com.pavlov.nearWarSecrets.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// моя палитра:
val MyLight = Color(0xFF0FE3C8)
val MyDark = Color(0xFF000000)
val MyCustom = Color(0xFF057E6F)

private val LightColorPalette = lightColors(
    primary = MyLight,
    primaryVariant = MyCustom,
    secondary = MyDark
)

private val DarkColorPalette = darkColors(
    primary = MyDark,
    primaryVariant = MyCustom,
    secondary = MyLight
)

val colors = LightColorPalette

@Composable
fun NearWarSecretsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    MaterialTheme(
        colors = colors,
        typography = Typography(
            h1 = MaterialTheme.typography.h1.copy(color = MyLight),
            h2 = MaterialTheme.typography.h2.copy(color = MyLight),
            h3 = MaterialTheme.typography.h3.copy(color = MyLight),
            h4 = MaterialTheme.typography.h4.copy(color = MyLight),
            h5 = MaterialTheme.typography.h5.copy(color = MyLight),
            h6 = MaterialTheme.typography.h6.copy(color = MyLight),
            subtitle1 = MaterialTheme.typography.subtitle1.copy(color = MyLight),
            subtitle2 = MaterialTheme.typography.subtitle2.copy(color = MyLight),
            body1 = MaterialTheme.typography.body1.copy(color = MyLight),
            body2 = MaterialTheme.typography.body2.copy(color = MyLight),
            button = MaterialTheme.typography.button.copy(color = MyLight),
            caption = MaterialTheme.typography.caption.copy(color = MyLight),
            overline = MaterialTheme.typography.overline.copy(color = MyLight)
        ),
        shapes = Shapes,
        content = content
    )
}