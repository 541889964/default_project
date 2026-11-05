package com.musicflow.app

import com.musicflow.app.data.MusicRepository
import com.musicflow.app.data.db.HistoryStore
import com.musicflow.app.data.db.MusicDatabase
import com.musicflow.app.download.Downloader

/** 极简手写依赖容器，替代 Hilt，减少 KSP 生成与云编译风险 */
class AppContainer(app: MusicFlowApp) {
    private val db: MusicDatabase by lazy { MusicDatabase.build(app) }
    val history: HistoryStore by lazy { HistoryStore(db.historyDao()) }
    val repository: MusicRepository by lazy { MusicRepository() }
    val downloader: Downloader by lazy { Downloader(app) }
}
