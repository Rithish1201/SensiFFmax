package com.sensiffmax.app.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * SensiFFMax Material 3 Theme
 *
 * Always dark — this is a cyberpunk esports utility.
 * Material 3 color scheme is mapped to our CyberColors.
 */
private val SensiFFMaxColorScheme = darkColorScheme(
    primary = CyberColors.CyberCyan,
    onPrimary = CyberColors.Background,
    primaryContainer = CyberColors.CyanDim,
    onPrimaryContainer = CyberColors.TextPrimary,

    secondary = CyberColors.CyanDim,
    onSecondary = CyberColors.Background,
    secondaryContainer = CyberColors.Elevated,
    onSecondaryContainer = CyberColors.TextPrimary,

    tertiary = CyberColors.Orange,
    onTertiary = CyberColors.Background,

    background = CyberColors.Background,
    onBackground = CyberColors.TextPrimary,

    surface = CyberColors.Surface,
    onSurface = CyberColors.TextPrimary,
    surfaceVariant = CyberColors.Elevated,
    onSurfaceVariant = CyberColors.TextSecondary,

    error = CyberColors.AlertRed,
    onError = CyberColors.TextPrimary,

    outline = CyberColors.Border,
    outlineVariant = CyberColors.BorderHighlight,

    inverseSurface = CyberColors.TextPrimary,
    inverseOnSurface = CyberColors.Background,
    inversePrimary = CyberColors.CyanDim,

    surfaceTint = Color.Transparent
)

@Composable
fun SensiFFMaxTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CyberColors.Background.toArgb()
            window.navigationBarColor = CyberColors.Background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = SensiFFMaxColorScheme,
        typography = SensiFFMaxTypography,
        shapes = SensiFFMaxShapes,
        content = content
    )
}
