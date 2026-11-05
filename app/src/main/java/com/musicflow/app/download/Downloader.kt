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
 * 下载器：流式写盘，兼容 Android 8.1 ~ 14。
 * 10+ 写公共 Music 目录(MediaStore)，8.1 写应用外部私有目录(免存储权限)。
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

                val target = resolveTarget(fileName)
                body.byteStream().use { input ->
                    FileOutputStream(target).use { out ->
                        val buf = ByteArray(64 * 1024)
                        var written = 0L
                        var read: Int
                        while (input.read(buf).also { read = it } != -1) {
                            out.write(buf, 0, read)
                            written += read
                            _state.value = DownloadState(
                                songName = fileName, total = total, written = written,
                                progress = if (total > 0) ((written * 100) / total).toInt() else 0
                            )
                        }
                    }
                }
                // 10+ 需要把文件登记进媒体库，否则音乐App扫不到
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    publishToMediaStore(fileName, target)
                }
                _state.value = DownloadState(songName = fileName, progress = 100,
                    total = total, written = total, done = true, savedPath = target.absolutePath)
                Result.success(target.absolutePath)
            }
        } catch (e: Exception) {
            _state.value = DownloadState(songName = fileName, error = e.message ?: "下载失败")
            Result.failure(e)
        }
    }

    private fun resolveTarget(fileName: String): File {
        val dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "").apply { mkdirs() }
        } else {
            File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "").apply { mkdirs() }
        }
        return File(dir, fileName)
    }

    private fun publishToMediaStore(fileName: String, file: File) {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, if (fileName.endsWith(".flac")) "audio/flac" else "audio/mpeg")
            put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/MusicFlow")
            put(MediaStore.Audio.Media.IS_PENDING, 0)
        }
        runCatching { context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values) }
    }
}
