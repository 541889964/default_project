package com.musicflow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = AccentPrimary,
    secondary = AccentSecondary,
    tertiary = AccentGreen,
    background = BgDeep,
    surface = BgSurface,
    surfaceVariant = BgElevated,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = DividerColor
)

@Composable
fun MusicFlowTheme(content: @Composable () -> Unit) {
    // 始终深色：音乐类 App 行业惯例，视觉沉浸
    isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}
