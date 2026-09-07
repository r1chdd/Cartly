package com.rtech.cartly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FavoriteRed = Color(0xFFE24B4A)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1D9E75),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA8EDD6),
    onPrimaryContainer = Color(0xFF00211A),
    secondary = Color(0xFF4C635B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFEAE0),
    onSecondaryContainer = Color(0xFF08201A),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF2C2C2A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2C2C2A),
    surfaceVariant = Color(0xFFF0F4F1),
    onSurfaceVariant = Color(0xFF888888),
    outlineVariant = Color(0xFFEEEEEE),
    error = Color(0xFFA32D2D),
    errorContainer = Color(0xFFFCEBEB),
    onErrorContainer = Color(0xFF7A1E1E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4BC79F),
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF00513B),
    onPrimaryContainer = Color(0xFFA8EDD6),
    secondary = Color(0xFFB4CFC2),
    onSecondary = Color(0xFF20352C),
    secondaryContainer = Color(0xFF374C42),
    onSecondaryContainer = Color(0xFFCFEAE0),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE2E2E0),
    surface = Color(0xFF1C1C1C),
    onSurface = Color(0xFFE2E2E0),
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = Color(0xFF9C9C9C),
    outlineVariant = Color(0xFF2E2E2E),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF472322),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun CartlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}