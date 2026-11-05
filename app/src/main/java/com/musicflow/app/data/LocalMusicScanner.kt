package com.musicflow.app.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 本地扫描到的一首音频 */
data class LocalTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: String,
    val path: String
) {
    fun toSong() = Song(id = "local:$id", name = title, artist = artist, album = album, source = "local")
}

/**
 * 全盘音频扫描器。
 * 用 MediaStore.Audio 索引（分区存储下这是唯一可靠的全盘音乐来源，
 * 直接遍历 /sdcard 在 Android 10+ 会被作用域存储拦住）。
 */
class LocalMusicScanner(private val context: Context) {

    suspend fun scan(): List<LocalTrack> = withContext(Dispatchers.IO) {
        val out = ArrayList<LocalTrack>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.DURATION + " > 30000"
        runCatching {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, null,
                MediaStore.Audio.Media.TITLE + " ASC"
            )?.use { c ->
                val iId = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val iTitle = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val iArtist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val iAlbum = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val iDur = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val iData = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                while (c.moveToNext()) {
                    val id = c.getLong(iId)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                    )
                    out.add(
                        LocalTrack(
                            id = id,
                            title = c.getString(iTitle) ?: "未知歌曲",
                            artist = c.getString(iArtist) ?: "未知歌手",
                            album = c.getString(iAlbum) ?: "",
                            durationMs = c.getLong(iDur),
                            uri = contentUri.toString(),
                            path = c.getString(iData) ?: ""
                        )
                    )
                }
            }
        }
        out
    }
}