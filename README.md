# MusicFlow 🎵

> 原生 Android 音乐搜索 / 在线试听 / 无损下载 App
> Kotlin + Jetpack Compose + Media3 · minSdk 27 (Android 8.1) · Material 3 深空主题

## ✨ 特性

- 🔍 **多源搜索**：聚合音乐 API（网易云曲库）
- ▶️ **在线试听**：ExoPlayer 后台播放，通知栏/锁屏可控
- ⬇️ **下载到手机**：优先无损 FLAC(999)，失败自动回落 320k MP3；Android 10+ 自动登记媒体库，所有播放器可见
- ❤️ **收藏 / 历史**：Room 本地持久化
- 🎨 **精美 UI**：Material 3 深空渐变主题、磨砂卡片、微动效、MiniPlayer 悬浮播放条
- 📱 **兼容 Android 8.1 及以上**

## 🚀 云编译（GitHub Actions）

推送到 GitHub 后自动编译，无需本地安装 Android SDK：

```bash
git remote add origin https://github.com/<你的用户名>/MusicFlow.git
git push -u origin master
```

- 打开仓库 → **Actions** → **Android CI - Build APK**
- 编译完成后在该次运行底部 **Artifacts** 下载 `MusicFlow-debug-apk`
- 打 tag（`git tag v1.0.0 && git push --tags`）会自动创建 GitHub Release 并附上 APK

## 🛠 本地构建

需 JDK 17 + Android SDK (compileSdk 34)：

```bash
gradle :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## 📁 项目结构

```
MusicFlow/
├── app/src/main/java/com/musicflow/app/
│   ├── data/          # 网络 API + 模型 + Room (歌曲/历史/收藏)
│   ├── download/      # 流式下载器 + MediaStore 登记
│   ├── player/        # Media3 ExoPlayer + 后台播放服务
│   ├── ui/            # Compose 页面 / 组件 / 主题 / ViewModel
│   ├── AppContainer.kt      # 手写依赖注入(替代 Hilt，编译更稳)
│   ├── MusicFlowApp.kt
│   └── MainActivity.kt
├── app/src/main/res/        # 图标 / 主题 / Manifest 资源
├── .github/workflows/       # GitHub Actions 云编译
├── build.gradle.kts
└── gradle/libs.versions.toml
```

## 🧱 技术栈

| 层 | 选型 | 理由 |
|---|---|---|
| UI | Jetpack Compose + Material 3 | 声明式，动效流畅，代码量少 |
| 架构 | MVVM + StateFlow | 单向数据流，状态清晰 |
| 网络 | OkHttp + kotlinx.serialization | 依赖最少，云编译最稳 |
| 播放 | AndroidX Media3 ExoPlayer | 官方标准，后台/通知栏支持 |
| 存储 | Room + KSP | 本地历史/收藏 |
| DI | 手写 AppContainer | 避开 Hilt 的 KSP 云编译风险 |

## ⚠️ 声明

本 App 仅供个人学习与技术研究，所有音乐版权归原平台及版权方所有。请勿用于商业用途。

## License
MIT
