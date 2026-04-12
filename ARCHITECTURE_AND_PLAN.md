# DouyinLite & DouyinBackend 架构分析与重构计划书

## 1. 架构现状分析（对标真实抖音）

本项目致力于实现高仿抖音 (TikTok) 的音视频生态，涵盖了从 Android 客户端音视频采集、编辑、流媒体播放，到 Go 后端基于高并发的微服务雏形设计。

### 1.1 Android 客户端 (DouyinLite)
基于现代化的 **Kotlin + Jetpack Compose + Media3** 技术栈：
*   **信息流 (Video Feed)**：采用 `VerticalPager` 实现全屏上下滑动，辅以 `VideoPlayerManager` 维护 ExoPlayer 的实例池（大小为 3），并在后台引入 `VideoCacheManager` 的 LRU 缓存，实现了零首帧延迟的边下边播机制。
*   **音视频处理 (Record & Edit)**：
    *   采集端利用 `CameraX` 结合 `CameraGLSurfaceView` 实现 OpenGL ES 级别的实时美颜滤镜。
    *   编辑端采用 AndroidX Media3 Transformer API 进行非破坏性编辑，编辑轨道序列化为 `EditingTimelineDto` 后，交由 WorkManager 在后台执行无损导出。

**⚠️ 痛点与不足：**
1.  **预加载过于激进**：`VideoFeed` 页面在滑动时立即预加载后续 3 个视频，这会与当前正在播放的视频争抢网络带宽，易造成主视频卡顿。
2.  **生命周期感知薄弱**：多个 Compose 的重组与 AndroidView (ExoPlayer PlayerView) 结合不够紧密，若无严格的后台暂停释放策略，极易导致内存泄漏 (OOM) 或底层解码器资源枯竭。

### 1.2 Go 后端 (DouyinBackend)
基于字节跳动开源的 **Hertz** 框架，采用 `handler -> service -> dal` 经典三层架构。
*   **推荐系统雏形**：依靠 SQL 实现了包含视频热度、时间衰减与用户标签（User Tag Affinity）匹配的轻量级多路召回混合排序。
*   **组件生态**：通过 GORM+SQLite 充当持久化层，并利用 JWT 进行无状态鉴权，文件直接存储于本地文件系统（Public 目录）。

**⚠️ 痛点与不足（高并发隐患）：**
1.  **SQLite 锁竞争**：在高并发（尤其是疯狂点赞、疯狂评论场景下），SQLite 默认的 `DELETE` 日志模式会导致严重的读写冲突（`database is locked` 异常），因为写操作会锁死整个数据库文件。
2.  **同步多媒体处理**：在 `PublishAction` 发布视频时，使用了同步调用 `exec.Command("ffmpeg", ...)` 截取视频首帧封面。由于 FFmpeg 处理极为耗时，且极度消耗 CPU，在上传并发量大的情况下会导致接口大面积超时，且拖垮整个机器的算力。

---

## 2. 详细开发与重构计划（Step by Step）

为了贴近抖音级的高可用和高性能，制定以下三个阶段的重构计划：

### 阶段一：后端数据库并发优化（解决 DB Locked）
**目标**：提升 SQLite 在高并发读写场景下的吞吐量。
*   **操作指引**：修改 `DouyinBackend/biz/dal/db/init.go` 文件。
*   **代码修改**：在 GORM 初始化数据库连接之后，立刻执行两条 PRAGMA 原生 SQL 语句：
    1.  `PRAGMA journal_mode=WAL;` （开启预写式日志，实现读写并发不互斥）。
    2.  `PRAGMA busy_timeout=5000;` （当发生行级竞争时，设置 5 秒自旋等待，而不是直接抛出 locked 错误）。

### 阶段二：后端视频处理异步化（解决发布超时）
**目标**：将繁重的 CPU 密集型任务从主协程中剥离，提升 API 吞吐效率。
*   **操作指引**：修改 `DouyinBackend/biz/handler/video/publish_handler.go` 和 `DouyinBackend/biz/service/video_service.go`。
*   **代码修改**：在保存视频文件后，立即返回响应，并将 FFmpeg 获取封面图的逻辑放入单独的 `goroutine` (如 `go func()`) 中执行；先使用一个“正在生成”的占位图存入数据库，等异步截帧成功后再 `UPDATE` 数据库中的 CoverURL。

### 阶段三：客户端信息流预加载调优（解决带宽抢占）
**目标**：平衡预加载数量与当前视频的流畅度。
*   **操作指引**：修改 `DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/components/VideoFeed.kt`。
*   **代码修改**：将 `LaunchedEffect(pagerState.currentPage)` 中的预加载循环上限，由 `nextIndex + 2` (即预加载3个) 降低至 `nextIndex` (仅预加载1个)。在有限带宽下保当前视频流畅优先。
