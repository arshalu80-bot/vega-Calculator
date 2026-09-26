package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBluePrimary,
    onPrimary = TextWhite,
    primaryContainer = ElectricBlueLight,
    onPrimaryContainer = TextWhite,
    secondary = KeyFunctionBackground,
    onSecondary = TextSilver,
    background = BackgroundPitchBlack,
    onBackground = TextWhite,
    surface = BackgroundSurfaceNavy,
    onSurface = TextWhite,
    surfaceVariant = KeyNumericBackground,
    onSurfaceVariant = TextMuted
)

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // The calculator app intentionally uses a pure Pitch Black & Electric Blue dark theme
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
