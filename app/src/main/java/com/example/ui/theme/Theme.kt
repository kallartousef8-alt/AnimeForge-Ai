package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color.White,
    primaryContainer = NeonPurpleDark,
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = NeonCyan,
    onSecondary = Color(0xFF001F29),
    secondaryContainer = Color(0xFF004D61),
    onSecondaryContainer = Color(0xFFC7F8FF),
    tertiary = NeonPink,
    onTertiary = Color.White,
    tertiaryContainer = NeonPinkDark,
    onTertiaryContainer = Color(0xFFFFD9E4),
    background = AnimeBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = AnimeSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = AnimeSurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = CardBorderGlow,
    outlineVariant = Color(0xFF252A50)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),
    tertiary = Color(0xFFDB2777),
    onTertiary = Color.White,
    background = AnimeBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = AnimeSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = AnimeSurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD5E1)
)

@Composable
fun AnimeForgeTheme(
    darkTheme: Boolean = true, // Default to stunning anime dark aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
