package com.newsapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Rich Teal accent against dark/light backgrounds
val TealAccent = Color(0xFF00BFA5)
val TealDark = Color(0xFF009688)
val TealLight = Color(0xFF64FFDA)
val CoralAccent = Color(0xFFFF6B6B)

private val DarkColorScheme = darkColorScheme(
    primary = TealAccent,
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF004D46),
    onPrimaryContainer = TealLight,
    secondary = Color(0xFFB2CCC7),
    onSecondary = Color(0xFF1D352F),
    secondaryContainer = Color(0xFF334B45),
    onSecondaryContainer = Color(0xFFCEE9E2),
    tertiary = Color(0xFFAACAE8),
    onTertiary = Color(0xFF103249),
    background = Color(0xFF0D1117),          // GitHub-dark inspired
    surface = Color(0xFF161B22),             // Slightly lighter surface
    surfaceVariant = Color(0xFF1E2530),      // Card backgrounds
    onBackground = Color(0xFFE6EDF3),
    onSurface = Color(0xFFE6EDF3),
    onSurfaceVariant = Color(0xFF8B949E),
    outline = Color(0xFF30363D),
    outlineVariant = Color(0xFF21262D),
    surfaceTint = TealAccent,
    error = CoralAccent,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = TealDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00251E),
    secondary = Color(0xFF4A6359),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCE8DC),
    onSecondaryContainer = Color(0xFF072017),
    tertiary = Color(0xFF3F6373),
    onTertiary = Color.White,
    background = Color(0xFFF6F8FA),          // Clean off-white
    surface = Color(0xFFFFFFFF),             // Pure white
    surfaceVariant = Color(0xFFF0F3F6),      // Subtle card bg
    onBackground = Color(0xFF1C1E21),
    onSurface = Color(0xFF1C1E21),
    onSurfaceVariant = Color(0xFF57606A),
    outline = Color(0xFFD0D7DE),
    outlineVariant = Color(0xFFE8EBEF),
    surfaceTint = TealDark,
    error = Color(0xFFCF222E),
    onError = Color.White,
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
)

@Composable
fun NewsAppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
