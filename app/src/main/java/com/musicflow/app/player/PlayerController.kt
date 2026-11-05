package com.musicflow.app.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 单例播放控制器，UI 直接观察状态 */
data class PlaybackUiState(
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val error: String? = null
)

class PlayerController private constructor(context: Context) {

    private val exo: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build()
    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state

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
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _state.value = _state.value.copy(error = error.message, isLoading = false)
            }
        })
    }

    fun play(url: String, title: String, artist: String) {
        _state.value = PlaybackUiState(title = title, artist = artist, isLoading = true)
        exo.setMediaItem(MediaItem.fromUri(url))
        exo.prepare()
        exo.playWhenReady = true
    }

    fun toggle() {
        if (exo.isPlaying) exo.pause() else exo.play()
        _state.value = _state.value.copy(isPlaying = exo.isPlaying)
    }

    fun seekTo(ms: Long) {
        exo.seekTo(ms)
        _state.value = _state.value.copy(positionMs = ms)
    }

    fun position(): Long = exo.currentPosition
    fun duration(): Long = if (exo.duration > 0) exo.duration else 0L
    fun release() = exo.release()

    companion object {
        @Volatile private var INSTANCE: PlayerController? = null
        fun get(context: Context): PlayerController =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlayerController(context).also { INSTANCE = it }
            }
    }
}
