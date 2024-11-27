package com.pavlov.nearWarSecrets.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Моя палитра: Color(0xFF0FE3C8)
val My1 = Color(0xFF525252) // фон кнопок /
val My2 = Color(0xFF000000)
val My3 = Color(0xFF00EED4) // тексты в полях
val My4 = Color(0xFF000000) // задний фон
val My5 = Color(0xFFE91E63)
val My6 = Color(0xFF00D5C1) // текст в кнопках
val My7 = Color(0xFF87E01A)

private val LightColorPalette = lightColors(
    primary = Color.Green,
    primaryVariant = Color.Magenta,
    secondary = Color.Yellow,
    background = Color.White, // Фон
    surface = Color.Cyan, // Поверхности
    onPrimary = Color.Red, // Цвет текста на primary
    onSecondary = Color.Blue, // Цвет текста на secondary
)

private val DarkColorPalette = darkColors(
    primary = My1,
    primaryVariant = My2,
    secondary = My3,
    background = My4, // Темный фон по умолчанию
    surface = My5, // Темные поверхности
    onPrimary = My6, // Цвет текста на primary
    onSecondary = My7, // Цвет текста на secondary
)

@Composable
fun NearWarSecretsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    customBackgroundColor: Color = if (darkTheme) DarkColorPalette.background else LightColorPalette.background,
    customIconColor: Color = if (darkTheme) DarkColorPalette.onPrimary else LightColorPalette.onPrimary,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette.copy(
            background = customBackgroundColor,
            surface = customBackgroundColor,
            onPrimary = customIconColor,
            onSecondary = customIconColor,
        )
    } else {
        LightColorPalette.copy(
            background = customBackgroundColor,
            surface = customBackgroundColor,
            onPrimary = customIconColor,
            onSecondary = customIconColor
        )
    }

    MaterialTheme(
        colors = colors,
        typography = Typography(
            h1 = MaterialTheme.typography.h1.copy(color = colors.secondary),
            h2 = MaterialTheme.typography.h2.copy(color = colors.secondary),
            h3 = MaterialTheme.typography.h3.copy(color = colors.secondary),
            h4 = MaterialTheme.typography.h4.copy(color = colors.secondary),
            h5 = MaterialTheme.typography.h5.copy(color = colors.secondary),
            h6 = MaterialTheme.typography.h6.copy(color = colors.secondary),
            subtitle1 = MaterialTheme.typography.subtitle1.copy(color = colors.secondary),
            subtitle2 = MaterialTheme.typography.subtitle2.copy(color = colors.secondary),
            body1 = MaterialTheme.typography.body1.copy(color = colors.secondary),
            body2 = MaterialTheme.typography.body2.copy(color = colors.secondary),
            button = MaterialTheme.typography.button.copy(color = colors.onPrimary),
            caption = MaterialTheme.typography.caption.copy(color = colors.secondary),
            overline = MaterialTheme.typography.overline.copy(color = colors.secondary)
        ),
        shapes = Shapes,
        content = content
    )
}
