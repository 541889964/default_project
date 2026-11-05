package com.musicflow.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.musicflow.app.MusicFlowApp
import com.musicflow.app.data.Song
import com.musicflow.app.download.DownloadState
import com.musicflow.app.player.PlayerController
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
    val resolvingId: String? = null
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as MusicFlowApp).container
    private val repo = container.repository
    private val historyStore = container.history
    val downloader = container.downloader
    val player = PlayerController.get(app)

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    val history = historyStore.history.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val favorites = historyStore.favorites.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val downloadState: StateFlow<DownloadState> = downloader.state

    fun onQueryChange(q: String) { _ui.value = _ui.value.copy(query = q) }

    fun search(keyword: String = _ui.value.query) {
        val k = keyword.trim()
        if (k.isEmpty() || _ui.value.searching) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(searching = true, message = null)
            try {
                val list = repo.search(k)
                _ui.value = _ui.value.copy(searching = false, songs = list,
                    message = if (list.isEmpty()) "没找到结果" else null)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(searching = false, message = "搜索失败：${e.message}")
            }
        }
    }

    fun preview(song: Song) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(resolvingId = song.id, message = null)
            val audio = repo.resolve(song, lossless = false)
            _ui.value = _ui.value.copy(resolvingId = null)
            if (audio == null) {
                _ui.value = _ui.value.copy(message = "该曲无可用在线音频")
                return@launch
            }
            player.play(audio.url, song.name, song.artist)
            historyStore.recordSearch(song)
        }
    }

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
                message = if (result.isSuccess) "已下载：$name" else "下载失败：${result.exceptionOrNull()?.message}"
            )
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val added = historyStore.toggleFavorite(song)
            _ui.value = _ui.value.copy(message = if (added) "已收藏" else "已取消收藏")
        }
    }

    fun clearMessage() { _ui.value = _ui.value.copy(message = null) }
}
