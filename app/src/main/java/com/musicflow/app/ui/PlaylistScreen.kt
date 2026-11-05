package com.musicflow.app.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicflow.app.data.Song
import com.musicflow.app.player.PlaybackUiState
import com.musicflow.app.ui.components.EmptyState
import com.musicflow.app.ui.theme.AccentPrimary
import com.musicflow.app.ui.theme.BgElevated
import com.musicflow.app.ui.theme.BgSurface
import com.musicflow.app.ui.theme.DividerColor
import com.musicflow.app.ui.theme.TextPrimary
import com.musicflow.app.ui.theme.TextSecondary

@Composable
fun PlaylistScreen(
    vm: HomeViewModel,
    playlists: List<String>,
    ui: HomeUiState,
    playState: PlaybackUiState
) {
    var newName by rememberSaveable { mutableStateOf("") }
    var opened by remember { mutableStateOf("") }

    val createPlaylist: (String) -> Unit = { raw ->
        val name = raw.trim()
        if (name.isNotEmpty()) {
            vm.openPlaylist(name)
            opened = name
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Text(
            "我的歌单", fontSize = 24.sp, fontWeight = FontWeight.Bold,
            color = TextPrimary, modifier = Modifier.padding(top = 12.dp, bottom = 10.dp)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newName, onValueChange = { newName = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("新建歌单名称…", color = TextSecondary, fontSize = 14.sp) },
                singleLine = true, shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgSurface, unfocusedContainerColor = BgSurface,
                    focusedBorderColor = AccentPrimary, unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    cursorColor = AccentPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    createPlaylist(newName); newName = ""
                })
            )
            Button(
                onClick = { createPlaylist(newName); newName = "" },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(Icons.Rounded.Add, "新建", Modifier.padding(end = 4.dp), tint = TextPrimary)
                Text("新建")
            }
        }

        val showEmpty = playlists.isEmpty() && opened.isBlank()
        if (showEmpty) {
            EmptyState("还没有歌单", "在上面输入名字就能建一个")
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playlists, key = { it }) { name ->
                    PlaylistCard(
                        name = name,
                        isOpen = opened == name,
                        ui = ui,
                        playState = playState,
                        onToggle = {
                            if (opened == name) {
                                opened = ""
                                vm.closePlaylist()
                            } else {
                                opened = name
                                vm.openPlaylist(name)
                            }
                        },
                        onDelete = { vm.deletePlaylist(name) },
                        onPlaySong = { song -> vm.playFromLibrary(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistCard(
    name: String,
    isOpen: Boolean,
    ui: HomeUiState,
    playState: PlaybackUiState,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onPlaySong: (Song) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        color = BgSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier
                .background(Brush.verticalGradient(listOf(BgElevated, BgSurface)))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.QueueMusic, null,
                    Modifier.padding(end = 10.dp), tint = AccentPrimary
                )
                Column(Modifier.weight(1f).clickable(onClick = onToggle)) {
                    Text(
                        name, color = TextPrimary, fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold, maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (isOpen) "收起" else "点击展开",
                        color = TextSecondary, fontSize = 11.sp
                    )
                }
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(50)).clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Delete, "删除", tint = TextSecondary)
                }
            }

            if (isOpen) {
                val list = ui.playlistItems
                if (list.isEmpty()) {
                    Text(
                        "歌单还是空的", color = TextSecondary, fontSize = 12.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                } else {
                    Column(Modifier.padding(top = 10.dp)) {
                        for (i in list.indices) {
                            val it = list[i]
                            val song = Song(it.songId, it.name, it.artist, source = it.source)
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { onPlaySong(song) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${i + 1}", color = TextSecondary, fontSize = 12.sp,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Text(
                                    "${it.name} - ${it.artist}", color = TextPrimary,
                                    fontSize = 13.sp, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (playState.isPlaying && playState.title == it.name) {
                                    Text("♪", color = AccentPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}