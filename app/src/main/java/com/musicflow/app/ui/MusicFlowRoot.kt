package com.musicflow.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicflow.app.player.PlaybackUiState
import com.musicflow.app.ui.components.GradientBackground
import com.musicflow.app.ui.components.LyricSheet
import com.musicflow.app.ui.components.MiniPlayer
import com.musicflow.app.ui.theme.AccentPrimary
import com.musicflow.app.ui.theme.BgSurface
import com.musicflow.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun MusicFlowRoot(vm: HomeViewModel = viewModel()) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }

    val ui by vm.ui.collectAsState()
    val favorites by vm.favorites.collectAsState()
    val history by vm.history.collectAsState()
    val playlists by vm.playlists.collectAsState()
    val download by vm.downloadState.collectAsState()

    var playState by remember { mutableStateOf(PlaybackUiState()) }
    var playPosition by remember { mutableIntStateOf(0) }
    var playDuration by remember { mutableIntStateOf(0) }
    var showLyric by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.player.state.collect { s ->
            playState = s
            playDuration = vm.player.duration().toInt()
        }
    }
    LaunchedEffect(playState.isPlaying) {
        while (playState.isPlaying) {
            playPosition = vm.player.position().toInt()
            playDuration = vm.player.duration().toInt()
            delay(500)
        }
    }
    LaunchedEffect(ui.message) {
        ui.message?.let { snackbar.showSnackbar(it); vm.clearMessage() }
    }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                Column {
                    MiniPlayer(
                        state = playState.copy(
                            positionMs = playPosition.toLong(),
                            durationMs = playDuration.toLong()
                        ),
                        onToggle = { vm.player.toggle() },
                        onPrev = { vm.player.previous() },
                        onNext = { vm.player.next() },
                        onLyric = {
                            val cur = ui.songs.firstOrNull { it.name == playState.title }
                                ?: com.musicflow.app.data.Song(
                                    id = "", name = playState.title, artist = playState.artist
                                )
                            vm.loadLyric(cur)
                            showLyric = true
                        }
                    )
                    NavigationBar(containerColor = BgSurface, tonalElevation = 0.dp) {
                        NavItem(tab == 0, "搜索", Icons.Rounded.Search) { tab = 0 }
                        NavItem(tab == 1, "本地", Icons.Rounded.Folder) {
                            tab = 1; vm.scanLocal()
                        }
                        NavItem(tab == 2, "收藏", Icons.Rounded.Favorite) { tab = 2 }
                        NavItem(tab == 3, "歌单", Icons.Rounded.QueueMusic) { tab = 3 }
                    }
                }
            }
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad)) {
                when (tab) {
                    0 -> HomeScreen(
                        vm = vm, ui = ui, playState = playState,
                        downloadProgress = download.progress,
                        isDownloading = !download.done && download.songName.isNotBlank()
                                && download.error == null && download.written > 0,
                        onPlay = { vm.preview(it) },
                        onDownload = { vm.download(it) },
                        onFavorite = { vm.toggleFavorite(it) },
                        onLyric = { vm.loadLyric(it); showLyric = true }
                    )
                    1 -> LocalScreen(vm, ui, playState)
                    2 -> LibraryScreen("我的收藏", favorites, playState, onClick = { vm.playFromLibrary(it) })
                    else -> PlaylistScreen(vm, playlists, ui, playState)
                }
            }
        }

        if (showLyric) {
            LyricSheet(
                title = ui.lyricTitle,
                lyric = ui.lyricText,
                loading = ui.lyricLoading,
                onDismiss = { showLyric = false; vm.clearLyric() }
            )
        }
    }
}

@Composable
private fun NavItem(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected, onClick = onClick,
        icon = { Icon(icon, label, tint = if (selected) AccentPrimary else TextSecondary) },
        label = { Text(label, fontSize = 11.sp, color = if (selected) AccentPrimary else TextSecondary) },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = AccentPrimary.copy(alpha = 0.14f),
            selectedIconColor = AccentPrimary,
            selectedTextColor = AccentPrimary
        )
    )
}