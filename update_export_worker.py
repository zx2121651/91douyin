import os

file_path = 'DouyinLite/lib_media/src/main/java/com/app/douyin/pro/lib/media/worker/VideoExportWorker.kt'
with open(file_path, 'r') as f:
    content = f.read()

# 1. Use MediaMetadataUtils to get real duration
new_imports = """
import com.app.douyin.pro.lib.media.util.MediaMetadataUtils
"""
content = content.replace('import com.app.douyin.pro.lib.media.model.VideoClip', 'import com.app.douyin.pro.lib.media.model.VideoClip' + new_imports)

old_timeline = """        // 简化的 Timeline 重构（实际应从数据库或持久化 JSON 中读取）
        val timeline = EditingTimeline().apply {
            videoMainTrack.add(VideoClip(
                id = "main",
                uri = Uri.parse(videoUri),
                startMs = 0L,
                endMs = 5000L, // Mock 5s
                durationMs = 5000L
            ))
        }"""

new_timeline = """        // 使用真实元数据重构 Timeline
        val uri = Uri.parse(videoUri)
        val duration = MediaMetadataUtils.getVideoDurationMs(applicationContext, uri)
        val timeline = EditingTimeline().apply {
            videoMainTrack.add(VideoClip(
                id = "main_export",
                uri = uri,
                startMs = 0L,
                endMs = if (duration > 0) duration else 5000L,
                durationMs = if (duration > 0) duration else 5000L
            ))
        }"""

content = content.replace(old_timeline, new_timeline)

with open(file_path, 'w') as f:
    f.write(content)
