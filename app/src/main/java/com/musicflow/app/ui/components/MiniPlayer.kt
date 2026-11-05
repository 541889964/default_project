package com.musicflow.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicflow.app.player.PlaybackUiState
import com.musicflow.app.ui.theme.AccentPrimary
import com.musicflow.app.ui.theme.AccentSecondary
import com.musicflow.app.ui.theme.BgElevated
import com.musicflow.app.ui.theme.BgSurface
import com.musicflow.app.ui.theme.DividerColor
import com.musicflow.app.ui.theme.TextPrimary
import com.musicflow.app.ui.theme.TextSecondary

@Composable
fun MiniPlayer(
    state: PlaybackUiState,
    onToggle: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onLyric: () -> Unit
) {
    val visible = state.title.isNotBlank()
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(BgElevated, BgSurface)))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        state.title, color = TextPrimary, fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (state.isLoading) "缓冲中…" else state.artist,
                        color = TextSecondary, fontSize = 12.sp, maxLines = 1,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp)
                    )
                }
                SmallIcon(onPrev) { Icon(Icons.Rounded.SkipPrevious, "上一曲", Modifier.size(22.dp), tint = TextPrimary) }
                Box(
                    Modifier
                        .padding(horizontal = 8.dp)
                        .size(42.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.linearGradient(listOf(AccentPrimary, AccentSecondary)))
                        .clickable(onClick = onToggle),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = TextPrimary)
                    } else {
                        Icon(
                            if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            "播放", Modifier.size(22.dp), tint = TextPrimary
                        )
                    }
                }
                SmallIcon(onNext) { Icon(Icons.Rounded.SkipNext, "下一曲", Modifier.size(22.dp), tint = TextPrimary) }
                SmallIcon(onLyric) { Icon(Icons.Rounded.Subtitles, "歌词", Modifier.size(20.dp), tint = AccentPrimary) }
            }
            if (state.durationMs > 0) {
                LinearProgressIndicator(
                    progress = { (state.positionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = AccentPrimary,
                    trackColor = DividerColor
                )
            }
        }
    }
}

@Composable
private fun SmallIcon(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}