package com.musicflow.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicflow.app.player.PlaybackUiState
import com.musicflow.app.ui.components.GradientBackground
import com.musicflow.app.ui.components.MiniPlayer
import com.musicflow.app.ui.theme.AccentPrimary
import com.musicflow.app.ui.theme.BgSurface
import com.musicflow.app.ui.theme.DividerColor
import com.musicflow.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun MusicFlowRoot(vm: HomeViewModel = viewModel()) {
    val ctx = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }

    val ui by vm.ui.collectAsState()
    val favorites by vm.favorites.collectAsState()
    val history by vm.history.collectAsState()
    val download by vm.downloadState.collectAsState()

    var playState by remember { mutableStateOf(PlaybackUiState()) }
    var playPosition by remember { mutableIntStateOf(0) }
    var playDuration by remember { mutableIntStateOf(0) }

    // 观察播放器状态
    LaunchedEffect(Unit) {
        vm.player.state.collect { s ->
            playState = s
            playDuration = vm.player.duration().toInt()
        }
    }
    // 进度轮询
    LaunchedEffect(playState.isPlaying) {
        while (playState.isPlaying) {
            playPosition = vm.player.position().toInt()
            playDuration = vm.player.duration().toInt()
            delay(500)
        }
    }
    // 消息 → Snackbar
    LaunchedEffect(ui.message) {
        ui.message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    GradientBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                Column {
                    MiniPlayer(
                        state = playState.copy(positionMs = playPosition.toLong(), durationMs = playDuration.toLong()),
                        onToggle = { vm.player.toggle() }
                    )
                    NavigationBar(containerColor = BgSurface, tonalElevation = 0.dp) {
                        NavItem(tab == 0, "搜索", Icons.Rounded.Search) { tab = 0 }
                        NavItem(tab == 1, "收藏", Icons.Rounded.Favorite) { tab = 1 }
                        NavItem(tab == 2, "历史", Icons.Rounded.History) { tab = 2 }
                    }
                }
            }
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad)) {
                when (tab) {
                    0 -> HomeScreen(vm, ui, playState, download.progress,
                        isDownloading = !download.done && download.songName.isNotBlank() && download.error == null && download.written > 0,
                        onPlay = { vm.preview(it) }, onDownload = { vm.download(it) },
                        onFavorite = { vm.toggleFavorite(it) })
                    1 -> LibraryScreen("我的收藏", favorites, playState, onClick = { vm.preview(it) })
                    else -> LibraryScreen("搜索历史", history, playState, onClick = { vm.preview(it) })
                }
            }
        }
    }
}

@Composable
private fun NavItem(selected: Boolean, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
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
