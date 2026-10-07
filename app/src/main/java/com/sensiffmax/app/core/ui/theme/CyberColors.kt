package com.sensiffmax.app.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * SensiFFMax Cyber Color System
 *
 * Dark cyberpunk palette designed for a premium esports utility.
 * All colors are defined once here — never scatter color constants.
 */
object CyberColors {
    // Backgrounds
    val Background = Color(0xFF05070A)
    val Surface = Color(0xFF0B1016)
    val Elevated = Color(0xFF111923)
    val ElevatedHigh = Color(0xFF1A2332)

    // Accents
    val CyberCyan = Color(0xFF00E5FF)
    val CyanDim = Color(0xFF00AFC7)
    val CyanGlow = Color(0x3300E5FF) // ~20% opacity cyan for glow effects
    val CyanSubtle = Color(0x1400E5FF) // ~8% opacity cyan

    // Status
    val AlertRed = Color(0xFFFF3B4A)
    val Orange = Color(0xFFFF7A00)
    val Success = Color(0xFF00FF9D)
    val NeonRed = AlertRed
    val NeonAmber = Orange
    val SurfaceVariant = Elevated

    // Text
    val TextPrimary = Color(0xFFF5F7FA)
    val TextSecondary = Color(0xFF8B98A8)
    val TextTertiary = Color(0xFF5A6577)

    // Borders
    val Border = Color(0x14FFFFFF) // white ~8% opacity
    val BorderHighlight = Color(0x33FFFFFF) // white ~20% opacity
    val BorderCyan = Color(0x5500E5FF) // cyan ~33% opacity

    // Gradients helpers
    val GradientCyanStart = Color(0xFF00E5FF)
    val GradientCyanEnd = Color(0xFF00AFC7)
    val GradientDarkStart = Color(0xFF111923)
    val GradientDarkEnd = Color(0xFF05070A)
}
