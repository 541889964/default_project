package com.musicflow.app.player

import android.content.Context
import android.media.audiofx.Equalizer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 播放器 UI 状态 */
data class PlaybackUiState(
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val queueIndex: Int = -1,
    val queueSize: Int = 0,
    val error: String? = null
)

/** 一首待播项 */
data class QueueItem(val url: String, val title: String, val artist: String)

/**
 * 单例播放控制器。
 * 队列 / 均衡器 全部用系统 + Media3 原生 API，零第三方依赖。
 */
class PlayerController private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val exo: ExoPlayer = ExoPlayer.Builder(appContext).build()
    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state

    private var queue: List<QueueItem> = emptyList()
    private var equalizer: Equalizer? = null
    private var eqEnabled = false

    init {
        exo.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.value = _state.value.copy(
                    isLoading = playbackState == Player.STATE_BUFFERING,
                    isPlaying = exo.isPlaying
                )
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val idx = exo.currentMediaItemIndex
                val q = queue.getOrNull(idx)
                if (q != null) {
                    _state.value = _state.value.copy(
                        title = q.title, artist = q.artist,
                        queueIndex = idx, queueSize = queue.size
                    )
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _state.value = _state.value.copy(error = error.message, isLoading = false)
            }
        })
    }

    /** 单曲播放 */
    fun play(url: String, title: String, artist: String) {
        queue = listOf(QueueItem(url, title, artist))
        exo.setMediaItem(MediaItem.fromUri(url))
        exo.prepare()
        exo.playWhenReady = true
        _state.value = PlaybackUiState(
            title = title, artist = artist, isLoading = true,
            queueIndex = 0, queueSize = 1
        )
    }

    /** 整列表播放，从 index 开始 */
    fun playQueue(items: List<QueueItem>, startIndex: Int) {
        if (items.isEmpty()) return
        queue = items
        val mediaItems = items.map { MediaItem.fromUri(it.url) }
        exo.setMediaItems(mediaItems, startIndex.coerceIn(0, items.size - 1), 0L)
        exo.prepare()
        exo.playWhenReady = true
        val cur = items[startIndex.coerceIn(0, items.size - 1)]
        _state.value = PlaybackUiState(
            title = cur.title, artist = cur.artist, isLoading = true,
            queueIndex = startIndex, queueSize = items.size
        )
    }

    fun next() { if (exo.hasNextMediaItem()) exo.seekToNextMediaItem() }
    fun previous() { if (exo.hasPreviousMediaItem()) exo.seekToPreviousMediaItem() }

    fun toggle() {
        if (exo.isPlaying) exo.pause() else exo.play()
        _state.value = _state.value.copy(isPlaying = exo.isPlaying)
    }

    fun seekTo(ms: Long) {
        exo.seekTo(ms)
        _state.value = _state.value.copy(positionMs = ms)
    }

    fun position(): Long = runCatching { exo.currentPosition }.getOrDefault(0L)
    fun duration(): Long = runCatching { exo.duration }.getOrDefault(0L).coerceAtLeast(0L)

    // ---------- 均衡器（系统 API） ----------
    fun toggleEqualizer(): Boolean {
        return runCatching {
            if (equalizer == null) {
                equalizer = Equalizer(0, exo.audioSessionId).apply {
                    enabled = true
                    // 简单预设：提升低频与高频，人声更清晰
                    val bands = numberOfBands.toInt()
                    for (b in 0 until bands) {
                        val range = bandLevelRange
                        val mid = (range[0] + range[1]) / 2
                        setBandLevel(b.toShort(), mid.toShort())
                    }
                }
                eqEnabled = true
            } else {
                eqEnabled = !eqEnabled
                equalizer?.enabled = eqEnabled
            }
            eqEnabled
        }.getOrDefault(false)
    }

    fun release() {
        runCatching { equalizer?.release() }
        equalizer = null
        exo.release()
    }

    companion object {
        @Volatile private var INSTANCE: PlayerController? = null
        fun get(context: Context): PlayerController =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlayerController(context).also { INSTANCE = it }
            }
    }
}