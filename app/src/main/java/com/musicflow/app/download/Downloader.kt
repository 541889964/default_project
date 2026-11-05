package com.musicflow.app.download

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

/** 下载任务状态 */
data class DownloadState(
    val songName: String = "",
    val progress: Int = 0,
    val total: Long = 0,
    val written: Long = 0,
    val done: Boolean = false,
    val error: String? = null,
    val savedPath: String? = null
)

/**
 * 下载器：文件写入【公共】音乐目录，系统音乐App 可扫到。
 * - Android 10+ (Q)：MediaStore.Audio 插入 → 公共 Music/MusicFlow
 * - Android 8.1 ~ 9：直接写 /storage/emulated/0/Music/MusicFlow（需 WRITE_EXTERNAL_STORAGE）
 */
class Downloader(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow(DownloadState())
    val state: StateFlow<DownloadState> = _state

    suspend fun download(url: String, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        _state.value = DownloadState(songName = fileName)
        try {
            val req = Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13)")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext Result.failure(Exception("HTTP ${resp.code}"))
                val body = resp.body ?: return@withContext Result.failure(Exception("空响应"))
                val total = body.contentLength()
                val mime = if (fileName.endsWith(".flac", true)) "audio/flac" else "audio/mpeg"

                val savedPath: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveViaMediaStore(body.byteStream(), fileName, mime, total)
                } else {
                    saveLegacy(body.byteStream(), fileName, total)
                }

                _state.value = DownloadState(
                    songName = fileName, progress = 100,
                    total = total, written = total, done = true, savedPath = savedPath
                )
                Result.success(savedPath)
            }
        } catch (e: Exception) {
            _state.value = DownloadState(songName = fileName, error = e.message ?: "下载失败")
            Result.failure(e)
        }
    }

    /** Android 10+：MediaStore 直接写公共 Music 目录 */
    private fun saveViaMediaStore(input: java.io.InputStream, fileName: String, mime: String, total: Long): String {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, mime)
            put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/MusicFlow")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("MediaStore 插入失败")
        resolver.openOutputStream(uri)?.use { out ->
            copyWithProgress(input, out, total)
        } ?: throw IllegalStateException("无法打开输出流")
        values.clear()
        values.put(MediaStore.Audio.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri.toString()
    }

    /** Android 8.1 ~ 9：直接写公共 Music 目录文件 */
    private fun saveLegacy(input: java.io.InputStream, fileName: String, total: Long): String {
        @Suppress("DEPRECATION")
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val dir = File(root, "MusicFlow").apply { mkdirs() }
        val target = File(dir, fileName)
        FileOutputStream(target).use { out -> copyWithProgress(input, out, total) }
        return target.absolutePath
    }

    private fun copyWithProgress(input: java.io.InputStream, out: OutputStream, total: Long) {
        val buf = ByteArray(64 * 1024)
        var written = 0L
        var read: Int
        while (input.read(buf).also { read = it } != -1) {
            out.write(buf, 0, read)
            written += read
            _state.value = DownloadState(
                songName = _state.value.songName, total = total, written = written,
                progress = if (total > 0) ((written * 100) / total).toInt() else 0
            )
        }
        out.flush()
    }
}