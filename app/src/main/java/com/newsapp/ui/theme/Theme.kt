package com.newsapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF), // White primary for B&W feel
    secondary = Color(0xFFB0BEC5), // Subtle gray
    tertiary = Color(0xFFE0E0E0),
    background = Color(0xFF000000), // Pure Black background
    surface = Color(0xFF121212), // Dark gray surface
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF000000), // Black primary
    secondary = Color(0xFF546E7A),
    tertiary = Color(0xFF424242),
    background = Color(0xFFF8F9FA), // Clean off-white
    surface = Color(0xFFFFFFFF), // Pure white surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun NewsAppTheme(
    darkTheme: Boolean = true, // We want a discord-like dark look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
