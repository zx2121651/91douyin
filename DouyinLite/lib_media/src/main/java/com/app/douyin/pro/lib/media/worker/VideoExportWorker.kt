package com.app.douyin.pro.lib.media.worker

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.app.douyin.pro.lib.media.VideoEditorHelper
import com.app.douyin.pro.lib.media.api.IVideoEditor
import com.app.douyin.pro.lib.media.model.EditingTimeline
import com.app.douyin.pro.lib.media.model.VideoClip
import com.app.douyin.pro.lib.media.model.AudioTrack
import com.app.douyin.pro.lib.media.util.MediaMetadataUtils
import kotlinx.coroutines.CompletableDeferred
import com.google.gson.Gson
import com.app.douyin.pro.lib.media.model.EditingTimelineDto

class VideoExportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val outputPath = inputData.getString("output_path") ?: return Result.failure()
        val timelineJson = inputData.getString("timeline_json") ?: return Result.failure()

        val timelineDto = Gson().fromJson(timelineJson, EditingTimelineDto::class.java)

        val timeline = EditingTimeline().apply {
            timelineDto.videoMainTrack.forEach { clipDto ->
                videoMainTrack.add(VideoClip(
                    id = clipDto.id,
                    uri = Uri.parse(clipDto.uriString),
                    startInSourceMs = clipDto.startInSourceMs,
                    endInSourceMs = clipDto.endInSourceMs,
                    sourceDurationMs = clipDto.sourceDurationMs,
                    speed = clipDto.speed,
                    volume = clipDto.volume
                ))
            }

            timelineDto.pipTracks.forEach { clipDto ->
                pipTracks.add(VideoClip(
                    id = clipDto.id,
                    uri = Uri.parse(clipDto.uriString),
                    startInSourceMs = clipDto.startInSourceMs,
                    endInSourceMs = clipDto.endInSourceMs,
                    sourceDurationMs = clipDto.sourceDurationMs,
                    speed = clipDto.speed,
                    volume = clipDto.volume
                ))
            }

            timelineDto.audioTracks.forEach { trackDto ->
                audioTracks.add(AudioTrack(
                    id = trackDto.id,
                    uri = Uri.parse(trackDto.uriString),
                    timelineStartMs = trackDto.timelineStartMs,
                    startInSourceMs = trackDto.startInSourceMs,
                    endInSourceMs = trackDto.endInSourceMs,
                    sourceDurationMs = trackDto.sourceDurationMs,
                    volume = trackDto.volume,
                    speed = trackDto.speed,
                    isMainTrack = trackDto.isMainTrack
                ))
            }

            // Note: Text and Sticker overlays need specialized handling in VideoEditorHelper
            // for them to be actually rendered.
        }

        val deferred = CompletableDeferred<Result>()
        val editorHelper: IVideoEditor = VideoEditorHelper(applicationContext)

        editorHelper.exportTimeline(timeline, outputPath, object : IVideoEditor.ExportListener {
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
