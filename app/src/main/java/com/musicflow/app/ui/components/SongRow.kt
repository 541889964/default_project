package com.musicflow.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicflow.app.data.Song
import com.musicflow.app.ui.theme.AccentGreen
import com.musicflow.app.ui.theme.AccentPrimary
import com.musicflow.app.ui.theme.BgElevated
import com.musicflow.app.ui.theme.BgSurface
import com.musicflow.app.ui.theme.DividerColor
import com.musicflow.app.ui.theme.TextPrimary
import com.musicflow.app.ui.theme.TextSecondary

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
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        color = BgSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Row(
            Modifier.background(Brush.verticalGradient(listOf(BgElevated, BgSurface)))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = String.format("%02d", index + 1),
                color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(22.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = song.name, color = TextPrimary, fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOf(song.artist, song.album).filter { it.isNotBlank() }.joinToString(" · "),
                    color = TextSecondary, fontSize = 12.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp)
                )
            }
            RoundIconBtn(
                tint = AccentGreen,
                onClick = onFavorite
            ) { Icon(Icons.Rounded.Favorite, null, Modifier.size(19.dp), tint = AccentGreen) }
            RoundIconBtn(
                tint = TextPrimary,
                onClick = onDownload
            ) { Icon(Icons.Rounded.Download, null, Modifier.size(19.dp), tint = TextPrimary) }
            RoundIconBtn(tint = AccentPrimary, onClick = onPlay) {
                if (resolving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AccentPrimary
                    )
                } else {
                    Icon(
                        if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        null, Modifier.size(20.dp), tint = AccentPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundIconBtn(tint: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    val scale by animateFloatAsState(1f, label = "btn")
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFF0D1117))
            .clickable(onClick = onClick)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) { content() }
}
