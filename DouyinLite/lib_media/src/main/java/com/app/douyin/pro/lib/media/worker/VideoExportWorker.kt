package com.app.douyin.pro.lib.media.worker

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.app.douyin.pro.lib.media.VideoEditorHelper
import com.app.douyin.pro.lib.media.model.EditingTimeline
import com.app.douyin.pro.lib.media.model.VideoClip
import kotlinx.coroutines.CompletableDeferred

class VideoExportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val outputPath = inputData.getString("output_path") ?: return Result.failure()
        val videoUri = inputData.getString("video_uri") ?: return Result.failure()

        // 简化的 Timeline 重构（实际应从数据库或持久化 JSON 中读取）
        val timeline = EditingTimeline().apply {
            videoMainTrack.add(VideoClip(
                id = "main",
                uri = Uri.parse(videoUri),
                startMs = 0L,
                endMs = 5000L, // Mock 5s
                durationMs = 5000L
            ))
        }

        val deferred = CompletableDeferred<Result>()
        val editorHelper = VideoEditorHelper(applicationContext)

        editorHelper.exportTimeline(timeline, outputPath, object : VideoEditorHelper.ExportListener {
            override fun onProgress(progress: Int) {
                setProgressAsync(workDataOf("progress" to progress))
            }

            override fun onCompleted(outputUri: Uri) {
                deferred.complete(Result.success(workDataOf("output_uri" to outputUri.toString())))
            }

            override fun onError(exception: Exception) {
                deferred.complete(Result.retry())
            }
        })

        return deferred.await()
    }
}
