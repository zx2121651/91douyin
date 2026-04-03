import os

file_path = 'DouyinLite/lib_media/src/main/java/com/app/douyin/pro/lib/media/VideoEditorHelper.kt'
with open(file_path, 'r') as f:
    content = f.read()

# 1. Add Coroutines imports
new_imports = """
import kotlinx.coroutines.*
"""
content = content.replace('import java.io.File', 'import java.io.File' + new_imports)

# 2. Add polling logic to exportTimeline
old_start = '        transformer.start(composition, outputPath)'
new_start = """        transformer.start(composition, outputPath)

        // 启动协程轮询进度
        CoroutineScope(Dispatchers.Main).launch {
            while (transformer.getProgress(ProgressHolder()) != Transformer.PROGRESS_STATE_NOT_STARTED) {
                val progressHolder = ProgressHolder()
                val state = transformer.getProgress(progressHolder)
                if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                    listener.onProgress(progressHolder.progress)
                } else if (state == Transformer.PROGRESS_STATE_WAITING_FOR_AVAILABILITY) {
                    // Do nothing
                } else {
                    break
                }
                delay(200)
            }
        }"""

content = content.replace(old_start, new_start)

with open(file_path, 'w') as f:
    f.write(content)
