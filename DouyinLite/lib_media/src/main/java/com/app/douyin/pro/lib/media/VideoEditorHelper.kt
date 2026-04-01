package com.app.douyin.pro.lib.media

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer

class VideoEditorHelper(private val context: Context) {

    private var transformer: Transformer? = null

    interface ExportListener {
        fun onProgress(progress: Int)
        fun onCompleted(outputUri: Uri)
        fun onError(exception: Exception)
    }

    /**
     * Initializes the Transformer.
     */
    fun setupTransformer(listener: ExportListener) {
        transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    // Handled below, since there are two overrides (one deprecated in some versions, but we use the new one)
                }

                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    listener.onError(exportException)
                }
            })
            // Use the standard start method to supply an output path, so we don't need all onCompleted overrides to carry output uri.
            .build()
    }

    /**
     * Trims a video and removes audio, then exports it to the specified output path.
     */
    fun exportTrimmedAndMutedVideo(
        inputUri: Uri,
        outputPath: String,
        startMs: Long,
        endMs: Long,
        listener: ExportListener
    ) {
        val transformerBuilder = Transformer.Builder(context)

        val newTransformer = transformerBuilder.addListener(object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                listener.onCompleted(Uri.parse("file://$outputPath"))
            }

            override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                listener.onError(exportException)
            }
        }).build()

        this.transformer = newTransformer

        // 1. Configure trimming
        val mediaItem = MediaItem.Builder()
            .setUri(inputUri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(startMs)
                    .setEndPositionMs(endMs)
                    .build()
            )
            .build()

        // 2. Remove audio
        val editedMediaItem = EditedMediaItem.Builder(mediaItem)
            .setRemoveAudio(true)
            .build()

        // 3. Start export
        newTransformer.start(editedMediaItem, outputPath)
    }

    /**
     * Replaces the audio of a video by stripping the original and mixing in a new audio file.
     */
    fun exportVideoWithReplacedAudio(
        videoUri: Uri,
        audioUri: Uri,
        outputPath: String,
        listener: ExportListener
    ) {
        val newTransformer = Transformer.Builder(context).addListener(object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                listener.onCompleted(Uri.parse("file://$outputPath"))
            }

            override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                listener.onError(exportException)
            }
        }).build()

        this.transformer = newTransformer

        // Mute original video
        val videoItem = MediaItem.fromUri(videoUri)
        val mutedVideoEditedItem = EditedMediaItem.Builder(videoItem)
            .setRemoveAudio(true)
            .build()
        val videoSequence = EditedMediaItemSequence(mutedVideoEditedItem)

        // New audio track
        val audioItem = MediaItem.fromUri(audioUri)
        val audioEditedItem = EditedMediaItem.Builder(audioItem)
            .setRemoveVideo(true)
            .build()
        val audioSequence = EditedMediaItemSequence(audioEditedItem)

        // Combine
        val composition = Composition.Builder(listOf(videoSequence, audioSequence))
            // .experimentalSetForceAudioTrack(true) // Sometimes necessary if audio track is missing
            .build()

        newTransformer.start(composition, outputPath)
    }

    fun getProgress(): Int {
        val progressHolder = androidx.media3.transformer.ProgressHolder()
        val state = transformer?.getProgress(progressHolder)
        return if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
            progressHolder.progress
        } else {
            -1
        }
    }

    fun cancel() {
        transformer?.cancel()
    }
}
