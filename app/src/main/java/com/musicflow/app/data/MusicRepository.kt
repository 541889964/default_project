package com.musicflow.app.data

/** 仓库：UI 层只依赖它，方便日后换源/加缓存 */
class MusicRepository(private val api: MusicApi = MusicApi()) {

    suspend fun search(keyword: String, source: String = "netease"): List<Song> =
        api.search(keyword, source)

    suspend fun resolve(song: Song, lossless: Boolean = true): AudioUrl? =
        api.audioUrl(song, lossless)

    suspend fun lyric(song: Song): String = api.lyric(song)

    /** 给下载用的文件名 */
    fun fileName(song: Song, ext: String): String {
        val raw = if (song.artist.isBlank()) song.name else "${song.name} - ${song.artist}"
        return raw.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(80) + "." + ext
    }
}
