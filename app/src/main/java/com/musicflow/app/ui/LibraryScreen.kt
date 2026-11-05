package com.musicflow.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicflow.app.data.Song
import com.musicflow.app.data.db.HistoryEntity
import com.musicflow.app.player.PlaybackUiState
import com.musicflow.app.ui.components.EmptyState
import com.musicflow.app.ui.components.SongRow
import com.musicflow.app.ui.theme.TextPrimary
import com.musicflow.app.ui.theme.TextSecondary

@Composable
fun LibraryScreen(
    title: String,
    records: List<HistoryEntity>,
    playState: PlaybackUiState,
    onClick: (Song) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold,
            color = TextPrimary, modifier = Modifier.padding(top = 12.dp, bottom = 2.dp))
        Text("共 ${records.size} 首", color = TextSecondary, fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp))
        if (records.isEmpty()) {
            EmptyState("这里还空着", "去搜索页找点喜欢的音乐吧")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(records, key = { it.rowId }) { e ->
                    val song = Song(e.songId, e.name, e.artist, source = e.source)
                    SongRow(
                        index = records.indexOf(e), song = song,
                        playing = playState.isPlaying && playState.title == song.name,
                        resolving = false,
                        onPlay = { onClick(song) },
                        onDownload = { },
                        onFavorite = { }
                    )
                }
            }
        }
    }
}
