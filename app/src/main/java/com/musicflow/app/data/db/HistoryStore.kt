package com.musicflow.app.data.db

import com.musicflow.app.data.Song
import kotlinx.coroutines.flow.Flow

/** 历史 / 收藏 业务封装 */
class HistoryStore(private val dao: MusicDao) {

    val history: Flow<List<HistoryEntity>> = dao.history()
    val favorites: Flow<List<HistoryEntity>> = dao.favorites()

    suspend fun recordSearch(song: Song) {
        dao.insert(HistoryEntity(songId = song.id, name = song.name,
            artist = song.artist, source = song.source, type = 0))
    }

    suspend fun toggleFavorite(song: Song): Boolean {
        return if (dao.isFavorite(song.id) > 0) {
            dao.removeFavorite(song.id); false
        } else {
            dao.insert(HistoryEntity(songId = song.id, name = song.name,
                artist = song.artist, source = song.source, type = 1)); true
        }
    }

    suspend fun isFavorite(songId: String) = dao.isFavorite(songId) > 0

    suspend fun clearHistory() = dao.clearHistory()
}
