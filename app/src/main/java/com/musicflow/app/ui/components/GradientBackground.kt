package com.musicflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.musicflow.app.ui.theme.GradientEnd
import com.musicflow.app.ui.theme.GradientStart

@Composable
fun GradientBackground(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(GradientStart, GradientEnd),
                    center = Offset(200f, -200f),
                    radius = 1400f
                )
            )
    ) { content() }
}
