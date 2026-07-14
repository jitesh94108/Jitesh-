package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ArtisticPurpleLight,
    secondary = ArtisticPurple,
    tertiary = ArtisticPurpleDark,
    background = ArtisticBackgroundDark,
    surface = ArtisticSurfaceDark,
    onPrimary = ArtisticTextLight,
    onSecondary = Color.White,
    onBackground = ArtisticTextDark,
    onSurface = ArtisticTextDark,
    surfaceVariant = ArtisticBorderDark,
    onSurfaceVariant = ArtisticOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = ArtisticPurple,
    secondary = ArtisticPurpleLight,
    tertiary = ArtisticPurpleDark,
    background = ArtisticBackgroundLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = ArtisticTextLight,
    onBackground = ArtisticTextLight,
    onSurface = ArtisticTextLight,
    surfaceVariant = ArtisticSurfaceLight,
    onSurfaceVariant = ArtisticOnSurfaceVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force our high-fidelity custom "Artistic Flair" theme!
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
