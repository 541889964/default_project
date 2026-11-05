package com.musicflow.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicflow.app.data.Song
import com.musicflow.app.player.PlaybackUiState
import com.musicflow.app.ui.components.EmptyState
import com.musicflow.app.ui.components.SongRow
import com.musicflow.app.ui.theme.AccentPrimary
import com.musicflow.app.ui.theme.BgSurface
import com.musicflow.app.ui.theme.DividerColor
import com.musicflow.app.ui.theme.TextPrimary
import com.musicflow.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    ui: HomeUiState,
    playState: PlaybackUiState,
    downloadProgress: Int,
    isDownloading: Boolean,
    onPlay: (Song) -> Unit,
    onDownload: (Song) -> Unit,
    onFavorite: (Song) -> Unit,
    onLyric: (Song) -> Unit,
    onToggleEq: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "MusicFlow", fontSize = 26.sp, fontWeight = FontWeight.Bold,
                color = TextPrimary, modifier = Modifier.weight(1f)
            )
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (ui.eqEnabled) AccentPrimary.copy(alpha = 0.2f) else BgSurface)
                    .clickable { onToggleEq() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Tune, "均衡器",
                    tint = if (ui.eqEnabled) AccentPrimary else TextSecondary
                )
            }
        }
        Text(
            "探索你的音乐宇宙", color = TextSecondary, fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = ui.query, onValueChange = vm::onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("搜索歌曲、歌手…", color = TextSecondary, fontSize = 14.sp) },
                singleLine = true, shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgSurface, unfocusedContainerColor = BgSurface,
                    focusedBorderColor = AccentPrimary, unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    cursorColor = AccentPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); vm.search() })
            )
            Button(
                onClick = { keyboard?.hide(); vm.search() },
                enabled = !ui.searching,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                modifier = Modifier.padding(top = 8.dp)
            ) { Text(if (ui.searching) "…" else "搜索", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
        }

        if (isDownloading) {
            Column(Modifier.padding(top = 10.dp)) {
                Text("下载中 $downloadProgress%", color = AccentPrimary, fontSize = 11.sp)
                LinearProgressIndicator(
                    progress = { downloadProgress / 100f },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    color = AccentPrimary, trackColor = DividerColor
                )
            }
        }

        when {
            ui.songs.isEmpty() && ui.searching -> EmptyState("正在搜索…")
            ui.songs.isEmpty() -> EmptyState("输入关键词开始搜索", "支持网易云曲库 · 可预览可下载")
            else -> LazyColumn(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ui.songs, key = { it.id }) { song ->
                    SongRow(
                        index = ui.songs.indexOf(song), song = song,
                        playing = playState.isPlaying && playState.title == song.name,
                        resolving = ui.resolvingId == song.id,
                        onPlay = { onPlay(song) },
                        onDownload = { onDownload(song) },
                        onFavorite = { onFavorite(song) }
                    )
                }
            }
        }
    }
}