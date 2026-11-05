package com.musicflow.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun historyDao(): MusicDao

    companion object {
        @Volatile private var INSTANCE: MusicDatabase? = null
        fun build(context: Context): MusicDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "musicflow.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
