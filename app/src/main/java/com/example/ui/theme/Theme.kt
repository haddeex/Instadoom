package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val InstaFocusColorScheme = darkColorScheme(
    primary = InstaRose,
    secondary = InstaOrange,
    tertiary = InstaPurple,
    background = BackgroundBlack,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun InstaFocusTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = InstaFocusColorScheme,
        typography = Typography,
        content = content
    )
}

// For compatibility with default template usages
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    InstaFocusTheme(content = content)
}
