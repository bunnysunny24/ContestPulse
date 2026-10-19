package com.bunny.contestpulse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Modern Glass & Minimalist Palette (Inspired by iOS 17 / Nothing OS / Material You)
val ObsidianBg = Color(0xFF09090B)
val GlassSurface = Color(0xFF141418)
val GlassSurfaceHigh = Color(0xFF1C1C22)
val GlassSurfacePill = Color(0xFF24242C)

// Frosted glass border hairlines
val GlassBorder = Color(0x26FFFFFF)       // 15% White
val GlassBorderSubtle = Color(0x12FFFFFF) // 7% White
val GlassHighlight = Color(0x1FFFFFFF)

// Sleek Brand & Platform Accent Colors
val CodeforcesBlue = Color(0xFF38BDF8)     // Sky Blue
val CodeChefOrange = Color(0xFFFB923C)     // Warm Amber/Orange
val LeetCodeYellow = Color(0xFFFBBF24)     // Golden Amber
val EmeraldLive = Color(0xFF34D399)        // Mint Emerald
val AccentViolet = Color(0xFF818CF8)       // Indigo Violet
val AccentGlow = Color(0xFF6366F1)

// Typography & Contrast
val TextPrimary = Color(0xFFF4F4F5)
val TextSecondary = Color(0xFFA1A1AA)
val TextMuted = Color(0xFF71717A)
val ErrorRed = Color(0xFFF87171)

// Gradient Brushes
val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.16f),
        Color.White.copy(alpha = 0.04f)
    )
)

val AmbientBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0E0E14),
        Color(0xFF09090B),
        Color(0xFF08080A)
    )
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentViolet,
    onPrimary = Color.White,
    secondary = AccentGlow,
    onSecondary = Color.White,
    background = ObsidianBg,
    surface = GlassSurface,
    surfaceVariant = GlassSurfaceHigh,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White,
    outline = GlassBorder
)

@Composable
fun ContestPulseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
