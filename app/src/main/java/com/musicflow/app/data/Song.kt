package com.musicflow.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 搜索结果中的一首歌 */
@Serializable
data class Song(
    val id: String,
    val name: String,
    val artist: String = "",
    val album: String = "",
    val source: String = "netease"
)

/** 取直链结果 */
@Serializable
data class AudioUrl(
    val url: String,
    val br: Int = 0,
    val size: Long = 0,
    val ext: String = "mp3"
)

@Serializable
internal data class RawSong(
    val id: String = "",
    val name: String = "",
    @SerialName("artist") val artists: List<String> = emptyList(),
    val album: String = "",
    val source: String = "netease"
)

@Serializable
internal data class SearchResponse(
    val total: Int = 0,
    val songs: List<RawSong> = emptyList(),
    val error: String? = null
)

@Serializable
internal data class UrlResponse(
    val url: String = "",
    val br: Int = 0,
    val size: Long = 0,
    val ext: String = "mp3",
    val error: String? = null
)
