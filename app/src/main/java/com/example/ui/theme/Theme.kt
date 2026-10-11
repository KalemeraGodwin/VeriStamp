package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = RichBlack,
    primaryContainer = GoldDark,
    onPrimaryContainer = CrispWhite,
    secondary = GoldLight,
    onSecondary = RichBlack,
    secondaryContainer = CardDark,
    onSecondaryContainer = GoldLight,
    background = RichBlack,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = GoldBorder,
    outlineVariant = Color(0xFF2C2C2C),
    error = AccessDeniedRed,
    onError = CrispWhite
)

private val LightColorScheme = lightColorScheme(
    primary = GoldDark,
    onPrimary = CrispWhite,
    primaryContainer = GoldPrimary,
    onPrimaryContainer = RichBlack,
    secondary = GoldPrimary,
    onSecondary = RichBlack,
    secondaryContainer = SurfaceLightVariant,
    onSecondaryContainer = RichBlack,
    background = CrispWhite,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLightVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = GoldPrimary,
    outlineVariant = Color(0xFFDCDCDC),
    error = AccessDeniedRed,
    onError = CrispWhite
)

@Composable
fun VeriStampTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
