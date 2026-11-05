package com.musicflow.app.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.musicflow.app.data.Song
import kotlinx.coroutines.flow.Flow

/** 歌单条目（单表设计：歌单名 + 歌曲信息，避免 JOIN，编译风险最低） */
@Entity(tableName = "playlist_items")
data class PlaylistItem(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val playlistName: String,
    val songId: String,
    val name: String,
    val artist: String = "",
    val source: String = "netease",
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PlaylistItem): Long

    @Query("SELECT * FROM playlist_items WHERE playlistName = :name ORDER BY addedAt DESC")
    fun itemsOf(name: String): Flow<List<PlaylistItem>>

    @Query("SELECT playlistName FROM playlist_items GROUP BY playlistName ORDER BY MIN(addedAt) DESC")
    fun allPlaylists(): Flow<List<String>>

    @Query("DELETE FROM playlist_items WHERE playlistName = :name AND songId = :songId")
    suspend fun remove(name: String, songId: String)

    @Query("DELETE FROM playlist_items WHERE playlistName = :name")
    suspend fun deletePlaylist(name: String)
}

/** 歌单业务封装 */
class PlaylistStore(private val dao: PlaylistDao) {

    val playlists: Flow<List<String>> = dao.allPlaylists()

    fun itemsOf(name: String): Flow<List<PlaylistItem>> = dao.itemsOf(name)

    suspend fun addSong(playlist: String, song: Song) = dao.insert(
        PlaylistItem(playlistName = playlist, songId = song.id,
            name = song.name, artist = song.artist, source = song.source)
    )

    suspend fun removeSong(playlist: String, songId: String) = dao.remove(playlist, songId)

    suspend fun deletePlaylist(name: String) = dao.deletePlaylist(name)
}