package com.musicflow.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.musicflow.app.MusicFlowApp
import com.musicflow.app.data.LocalTrack
import com.musicflow.app.data.Song
import com.musicflow.app.data.db.PlaylistItem
import com.musicflow.app.download.DownloadState
import com.musicflow.app.player.PlayerController
import com.musicflow.app.player.QueueItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val query: String = "",
    val searching: Boolean = false,
    val songs: List<Song> = emptyList(),
    val message: String? = null,
    val resolvingId: String? = null,
    // 本地曲库
    val scanning: Boolean = false,
    val localTracks: List<LocalTrack> = emptyList(),
    val localScanned: Boolean = false,
    // 歌词
    val lyricTitle: String = "",
    val lyricText: String = "",
    val lyricLoading: Boolean = false,
    // 歌单
    val eqEnabled: Boolean = false,
    val currentPlaylist: String = "",
    val playlistItems: List<PlaylistItem> = emptyList()
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as MusicFlowApp).container
    private val repo = container.repository
    private val historyStore = container.history
    private val playlistStore = container.playlists
    private val scanner = container.localScanner
    val downloader = container.downloader
    val player = PlayerController.get(app)

    private var playlistJob: Job? = null

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    val history = historyStore.history.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val favorites = historyStore.favorites.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val playlists = playlistStore.playlists.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val downloadState: StateFlow<DownloadState> = downloader.state

    // ---------- 搜索 ----------
    fun onQueryChange(q: String) { _ui.value = _ui.value.copy(query = q) }

    fun search(keyword: String = _ui.value.query) {
        val k = keyword.trim()
        if (k.isEmpty() || _ui.value.searching) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(searching = true, message = null)
            try {
                val list = repo.search(k)
                _ui.value = _ui.value.copy(
                    searching = false, songs = list,
                    message = if (list.isEmpty()) "没找到结果" else null
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(searching = false, message = "搜索失败：${e.message}")
            }
        }
    }

    // ---------- 在线播放：整个结果列表入队 ----------
    fun preview(song: Song) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(resolvingId = song.id, message = null)
            val audio = repo.resolve(song, lossless = false)
            _ui.value = _ui.value.copy(resolvingId = null)
            if (audio == null) {
                _ui.value = _ui.value.copy(message = "该曲无可用在线音频")
                return@launch
            }
            val list = _ui.value.songs
            val idx = list.indexOfFirst { it.id == song.id }
            if (idx >= 0) {
                val queue = list.map { QueueItem("", it.name, it.artist) }.toMutableList()
                queue[idx] = QueueItem(audio.url, song.name, song.artist)
                player.playQueue(queue.toList(), idx)
            } else {
                player.play(audio.url, song.name, song.artist)
            }
            historyStore.recordSearch(song)
        }
    }

    /** 本地曲目播放：整个本地列表入队 */
    fun playLocal(track: LocalTrack) {
        val list = _ui.value.localTracks
        val idx = list.indexOfFirst { it.id == track.id }
        if (idx < 0) return
        val queue = list.map { QueueItem(it.uri, it.title, it.artist) }
        player.playQueue(queue, idx)
    }

    /** 收藏/历史里点击 → 在线解析后播放 */
    fun playFromLibrary(song: Song) = preview(song)

    // ---------- 下载 ----------
    fun download(song: Song) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(resolvingId = song.id, message = null)
            val audio = repo.resolve(song, lossless = true)
            _ui.value = _ui.value.copy(resolvingId = null)
            if (audio == null) {
                _ui.value = _ui.value.copy(message = "该曲无可用下载链接")
                return@launch
            }
            val name = repo.fileName(song, audio.ext)
            val result = downloader.download(audio.url, name)
            _ui.value = _ui.value.copy(
                message = if (result.isSuccess) "已保存到 音乐/MusicFlow：$name"
                else "下载失败：${result.exceptionOrNull()?.message}"
            )
        }
    }

    // ---------- 收藏 ----------
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val added = historyStore.toggleFavorite(song)
            _ui.value = _ui.value.copy(message = if (added) "已收藏" else "已取消收藏")
        }
    }

    // ---------- 本地扫描 ----------
    fun scanLocal(force: Boolean = false) {
        if (_ui.value.scanning) return
        if (_ui.value.localScanned && !force) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(scanning = true, message = null)
            try {
                val tracks = scanner.scan()
                _ui.value = _ui.value.copy(
                    scanning = false, localTracks = tracks, localScanned = true,
                    message = if (tracks.isEmpty()) "未找到本地音频（可能未授权）" else "扫描到 ${tracks.size} 首"
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(scanning = false, localScanned = true,
                    message = "扫描失败：${e.message}")
            }
        }
    }

    // ---------- 歌词 ----------
    fun loadLyric(song: Song) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(lyricTitle = song.name, lyricText = "", lyricLoading = true)
            val text = runCatching { repo.lyric(song) }.getOrDefault("")
            _ui.value = _ui.value.copy(
                lyricLoading = false,
                lyricText = text.ifBlank { "暂无歌词" }
            )
        }
    }

    fun clearLyric() { _ui.value = _ui.value.copy(lyricTitle = "", lyricText = "", lyricLoading = false) }

    // ---------- 歌单 ----------
    fun openPlaylist(name: String) {
        playlistJob?.cancel()
        _ui.value = _ui.value.copy(currentPlaylist = name)
        playlistJob = viewModelScope.launch {
            playlistStore.itemsOf(name).collect { items ->
                _ui.value = _ui.value.copy(playlistItems = items)
            }
        }
    }

    fun closePlaylist() {
        playlistJob?.cancel()
        playlistJob = null
        _ui.value = _ui.value.copy(currentPlaylist = "", playlistItems = emptyList())
    }

    fun addToPlaylist(playlist: String, song: Song) {
        val name = playlist.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            playlistStore.addSong(name, song)
            _ui.value = _ui.value.copy(message = "已加入歌单「$name」")
        }
    }

    fun deletePlaylist(name: String) {
        viewModelScope.launch {
            playlistStore.deletePlaylist(name)
            _ui.value = _ui.value.copy(message = "已删除歌单「$name」")
        }
    }

    fun toggleEq() {
        val on = player.toggleEqualizer()
        _ui.value = _ui.value.copy(
            eqEnabled = on,
            message = if (on) "均衡器已开启" else "均衡器已关闭"
        )
    }

    fun clearMessage() { _ui.value = _ui.value.copy(message = null) }
}