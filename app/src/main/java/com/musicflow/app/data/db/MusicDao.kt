package com.musicflow.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    @Insert
    suspend fun insert(e: HistoryEntity): Long

    @Query("SELECT * FROM records WHERE type = 0 ORDER BY createdAt DESC LIMIT 30")
    fun history(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM records WHERE type = 1 ORDER BY createdAt DESC")
    fun favorites(): Flow<List<HistoryEntity>>

    @Query("SELECT COUNT(*) FROM records WHERE type = 1 AND songId = :songId")
    suspend fun isFavorite(songId: String): Int

    @Query("DELETE FROM records WHERE type = 1 AND songId = :songId")
    suspend fun removeFavorite(songId: String)

    @Query("DELETE FROM records WHERE type = 0")
    suspend fun clearHistory()
}
