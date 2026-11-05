# MusicFlow 🎵

> 原生 Android 音乐搜索 / 在线试听 / 无损下载 / 本地曲库 App
> Kotlin + Jetpack Compose + Media3 · minSdk 27 (Android 8.1) · Material 3 深空主题

## ✨ 功能全清单

### 在线
- 🔍 多源搜索（网易云曲库聚合 API）
- ▶️ 在线试听：ExoPlayer，**整列表自动入队**，自动连播下一首
- ⏮⏭ 上一曲 / 下一曲
- 🎤 **歌词显示**：独立歌词接口，LRC 解析，底部弹层滚动查看
- ⬇️ 下载：优先无损 FLAC(999)，失败自动回落 320k MP3
- 🎚 **均衡器**：系统 AudioEffect 开关（零第三方依赖）

### 本地
- 📂 **全盘音频扫描**：MediaStore 索引，扫描手机所有音乐（含下载目录/其他 App 下载的音乐）
- ▶️ 本地播放：整列表入队，自动连播
- 🔄 一键重新扫描

### 管理
- ❤️ 收藏（Room 持久化）
- 🕘 搜索历史（Room 持久化）
- 📋 **歌单**：新建 / 添加歌曲 / 删除歌单 / 展开查看 / 点击播放

### 体验
- 🎨 Material 3 深空蓝紫渐变主题，磨砂卡片，微动效
- 📌 MiniPlayer 悬浮播放条 + 进度条 + 三键控制
- 🍞 Snackbar 操作反馈
- 📱 兼容 Android 8.1 ~ 14
- 🌗 **动态取色**（Android 12+，Material You）

## 📥 下载文件去哪了

| 系统版本 | 保存位置 | 能否被其他音乐 App 扫到 |
|---|---|---|
| Android 10+ | 公共 `音乐/MusicFlow/`（MediaStore 登记） | ✅ 是 |
| Android 8.1~9 | 公共 `/sdcard/Music/MusicFlow/` | ✅ 是 |

不是 App 私有目录，文件管理器和其他播放器都能看到。

## 🚀 云编译（GitHub Actions）

推送到 GitHub 自动编译，无需本地 Android SDK：

```bash
git remote add origin https://github.com/<用户名>/MusicFlow.git
git push -u origin master
```

- 打开仓库 → **Actions** → 「Android CI - Build APK」
- 跑完在该次运行底部 **Artifacts** 下载 `MusicFlow-debug-apk`
- 打 tag（`git tag v1.0.0 && git push --tags`）自动建 Release 附 APK

## 🧱 技术栈（全部零第三方音频/UI 依赖）

| 层 | 选型 | 理由 |
|---|---|---|
| UI | Jetpack Compose + Material 3 | 声明式，动效流畅 |
| 架构 | MVVM + StateFlow | 单向数据流，状态清晰 |
| 网络 | OkHttp + kotlinx.serialization | 依赖最少，云编译最稳 |
| 播放 | AndroidX Media3 ExoPlayer | 官方标准，队列/后台播放原生支持 |
| 均衡器 | android.media.audiofx.Equalizer | 系统类，零风险 |
| 本地扫描 | MediaStore.Audio | 分区存储下唯一可靠全盘索引 |
| 存储 | Room + KSP | 历史/收藏/歌单 |
| DI | 手写 AppContainer | 避开 Hilt 的 KSP 云编译风险 |

## ⚠️ 声明

仅供个人学习与技术研究，音乐版权归原平台及版权方所有。

## License
MIT
