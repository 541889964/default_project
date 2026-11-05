package com.musicflow.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 记录表：type=0 搜索历史, type=1 收藏
 */
@Entity(tableName = "records")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val songId: String = "",
    val name: String,
    val artist: String = "",
    val source: String = "netease",
    val type: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
