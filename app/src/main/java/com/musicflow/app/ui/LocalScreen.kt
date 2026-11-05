package com.musicflow.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicflow.app.data.LocalTrack
import com.musicflow.app.player.PlaybackUiState
import com.musicflow.app.ui.components.EmptyState
import com.musicflow.app.ui.components.SongRow
import com.musicflow.app.ui.theme.AccentPrimary
import com.musicflow.app.ui.theme.DividerColor
import com.musicflow.app.ui.theme.TextPrimary
import com.musicflow.app.ui.theme.TextSecondary

@Composable
fun LocalScreen(vm: HomeViewModel, ui: HomeUiState, playState: PlaybackUiState) {
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("本地音乐", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    if (ui.scanning) "正在扫描…" else "共 ${ui.localTracks.size} 首",
                    color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp)
                )
            }
            Button(
                onClick = { vm.scanLocal(force = true) },
                enabled = !ui.scanning,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
            ) { Text(if (ui.scanning) "扫描中" else "重新扫描", fontSize = 13.sp) }
        }

        if (ui.scanning) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                color = AccentPrimary, trackColor = DividerColor
            )
        }

        when {
            ui.localTracks.isEmpty() && !ui.scanning ->
                EmptyState("没有找到本地音频", "点击右上角重新扫描，或先下载几首歌")
            else -> LazyColumn(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ui.localTracks, key = { it.id }) { track: LocalTrack ->
                    SongRow(
                        index = ui.localTracks.indexOf(track),
                        song = track.toSong(),
                        playing = playState.isPlaying && playState.title == track.title,
                        resolving = false,
                        onPlay = { vm.playLocal(track) },
                        onDownload = { },
                        onFavorite = { }
                    )
                }
            }
        }
    }
}