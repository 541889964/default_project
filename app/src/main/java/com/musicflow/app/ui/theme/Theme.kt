package com.musicflow.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
    val context = LocalContext.current
    // Android 12+ 用系统动态取色（Material You），低版本回退自研深空配色
    val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        runCatching { dynamicDarkColorScheme(context) }.getOrDefault(DarkColors)
    } else DarkColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}