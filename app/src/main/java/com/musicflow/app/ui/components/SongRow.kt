package com.musicflow.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicflow.app.data.Song
import com.musicflow.app.ui.theme.AccentGreen
import com.musicflow.app.ui.theme.AccentPrimary
import com.musicflow.app.ui.theme.AccentSecondary
import com.musicflow.app.ui.theme.BgElevated
import com.musicflow.app.ui.theme.BgSurface
import com.musicflow.app.ui.theme.DividerColor
import com.musicflow.app.ui.theme.TextPrimary
import com.musicflow.app.ui.theme.TextSecondary

private val coverGradients = listOf(
    listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
    listOf(Color(0xFFF093FB), Color(0xFFF5576C)),
    listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)),
    listOf(Color(0xFF43E97B), Color(0xFF38F9D7)),
    listOf(Color(0xFFFA709A), Color(0xFFFEE140)),
    listOf(Color(0xFF30CFD0), Color(0xFF330867))
)

@Composable
fun SongRow(
    index: Int,
    song: Song,
    playing: Boolean,
    resolving: Boolean,
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    onFavorite: () -> Unit
) {
    val grad = coverGradients[index % coverGradients.size]
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)),
        color = BgSurface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier
                .background(Brush.verticalGradient(listOf(BgElevated, BgSurface)))
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 封面视觉块
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Brush.linearGradient(grad)),
                contentAlignment = Alignment.Center
            ) {
                if (playing) {
                    Icon(
                        Icons.Rounded.Pause, "播放中",
                        Modifier.size(24.dp), tint = Color.White
                    )
                } else {
                    Icon(
                        Icons.Rounded.MusicNote, null,
                        Modifier.size(26.dp), tint = Color.White
                    )
                }
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = song.name, color = TextPrimary, fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOf(song.artist, song.album)
                        .filter { it.isNotBlank() }.joinToString(" · "),
                    color = TextSecondary, fontSize = 12.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp)
                )
            }

            RoundBtn(tint = AccentGreen, onClick = onFavorite) {
                Icon(Icons.Rounded.FavoriteBorder, "收藏", Modifier.size(18.dp), tint = AccentGreen)
            }
            RoundBtn(tint = TextPrimary, onClick = onDownload) {
                Icon(Icons.Rounded.Download, "下载", Modifier.size(18.dp), tint = TextPrimary)
            }
            PlayBtn(resolving = resolving, playing = playing, onClick = onPlay)
        }
    }
}

@Composable
private fun RoundBtn(tint: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    val scale by animateFloatAsState(1f, label = "btn")
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFF0D1117))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun PlayBtn(resolving: Boolean, playing: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(AccentPrimary, AccentSecondary)))
            .clickable(enabled = !resolving, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (resolving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White
            )
        } else {
            Icon(
                if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                "播放", Modifier.size(24.dp), tint = Color.White
            )
        }
    }
}