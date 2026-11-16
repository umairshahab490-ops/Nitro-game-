package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NitroDarkColorScheme = darkColorScheme(
    primary = NeonPink,
    onPrimary = TextPrimary,
    secondary = BoostCyan,
    onSecondary = ObsidianBg,
    tertiary = CashGreen,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextMuted,
    error = DangerRed,
    onError = TextPrimary,
    outline = BorderHairline
)

@Composable
fun NitroRushTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NitroDarkColorScheme,
        typography = NitroRushTypography,
        content = content
    )
}
