package com.musicflow.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 音乐数据源客户端。
 * 直连聚合 API，OkHttp + kotlinx.serialization，无额外网络框架依赖。
 */
class MusicApi(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val base = "https://music-api.gdstudio.xyz/api.php"

    suspend fun search(keyword: String, source: String = "netease", count: Int = 25): List<Song> =
        withContext(Dispatchers.IO) {
            val url = "$base?types=search&source=$source&name=${enc(keyword)}&count=$count&pages=1"
            val body = get(url) ?: return@withContext emptyList()
            val resp = runCatching { json.decodeFromString<SearchResponse>(body) }.getOrNull()
                ?: return@withContext emptyList()
            resp.songs.map {
                Song(id = it.id, name = it.name, artist = it.artists.joinToString(" / "),
                     album = it.album, source = it.source.ifBlank { source })
            }
        }

    suspend fun audioUrl(song: Song, preferLossless: Boolean = true): AudioUrl? =
        withContext(Dispatchers.IO) {
            val brs = if (preferLossless) listOf("999", "320") else listOf("320")
            for (br in brs) {
                val url = "$base?types=url&source=${song.source}&id=${song.id}&br=$br"
                val body = get(url) ?: continue
                val resp = runCatching { json.decodeFromString<UrlResponse>(body) }.getOrNull() ?: continue
                if (resp.url.startsWith("http")) {
                    val ext = if (resp.ext.isNotBlank()) resp.ext else if (br == "999") "flac" else "mp3"
                    return@withContext AudioUrl(resp.url, resp.br, resp.size, ext)
                }
            }
            null
        }

    /** 取歌词（LRC 文本）；无歌词返回空串 */
    suspend fun lyric(song: Song): String = withContext(Dispatchers.IO) {
        val url = "$base?types=lyric&source=${song.source}&id=${song.id}"
        val body = get(url) ?: return@withContext ""
        runCatching {
            val o = json.parseToJsonElement(body).jsonObject
            o["lyric"]?.jsonPrimitive?.contentOrNull
                ?: o["lrc"]?.jsonObject?.get("lyric")?.jsonPrimitive?.contentOrNull
                ?: ""
        }.getOrDefault("")
    }

    private fun get(url: String, retries: Int = 3): String? {
        var last: Exception? = null
        repeat(retries) { i ->
            try {
                val req = Request.Builder().url(url)
                    .header("User-Agent", UA)
                    .header("Referer", "https://music.gdstudio.xyz/")
                    .build()
                client.newCall(req).execute().use { r ->
                    if (r.isSuccessful) return r.body?.string()
                }
            } catch (e: Exception) {
                last = e
                Thread.sleep(1200L * (i + 1))
            }
        }
        last?.printStackTrace()
        return null
    }

    private fun enc(s: String) = java.net.URLEncoder.encode(s, "UTF-8")

    companion object {
        const val UA = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Mobile Safari/537.36"
    }
}
