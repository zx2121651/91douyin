package com.app.douyin.pro.lib.media

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.*
import com.app.douyin.pro.lib.media.model.EditingTimeline
import com.app.douyin.pro.lib.media.model.VideoClip
import java.io.File
import kotlinx.coroutines.*


@OptIn(UnstableApi::class)
class VideoEditorHelper(private val context: Context) {

    private var transformer: Transformer? = null

    interface ExportListener {
        fun onProgress(progress: Int)
        fun onCompleted(outputUri: Uri)
        fun onError(exception: Exception)
    }

    /**
     * 将真实的 Timeline 模型转换为 Media3 Composition 并开始导出
     */
    fun exportTimeline(
        timeline: EditingTimeline,
        outputPath: String,
        listener: ExportListener
    ) {
        val transformerBuilder = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    listener.onCompleted(Uri.fromFile(File(outputPath)))
                }

                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    listener.onError(exportException)
                }
            })

        val transformer = transformerBuilder.build()
        this.transformer = transformer

        // 构建视频序列 (Video Sequence)
        val editedMediaItems = timeline.videoMainTrack.map { clip ->
            val mediaItem = MediaItem.Builder()
                .setUri(clip.uri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(clip.startMs)
                        .setEndPositionMs(clip.endMs)
                        .build()
                )
                .build()

            EditedMediaItem.Builder(mediaItem)
                .setRemoveAudio(clip.volume == 0f)
                .build()
        }

        val videoSequence = EditedMediaItemSequence(editedMediaItems)

        // 构建音频序列 (Audio Tracks)
        val audioSequences = timeline.audioTracks.map { track ->
            val mediaItem = MediaItem.Builder()
                .setUri(track.uri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(track.clipStartMs)
                        .setEndPositionMs(track.clipEndMs)
                        .build()
                )
                .build()

            EditedMediaItemSequence(EditedMediaItem.Builder(mediaItem).build())
        }

        val sequences = mutableListOf<EditedMediaItemSequence>()
        sequences.add(videoSequence)
        sequences.addAll(audioSequences)

        val composition = Composition.Builder(sequences).build()

        transformer.start(composition, outputPath)

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
        }
    }

    fun getProgress(): Int {
        val progressHolder = ProgressHolder()
        val state = transformer?.getProgress(progressHolder)
        return if (state == Transformer.PROGRESS_STATE_AVAILABLE) progressHolder.progress else -1
    }

    fun cancel() {
        transformer?.cancel()
    }
}
