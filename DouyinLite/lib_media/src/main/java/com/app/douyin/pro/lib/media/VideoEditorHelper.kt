package com.app.douyin.pro.lib.media

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.SpeedChangeEffect
import androidx.media3.transformer.*
import com.app.douyin.pro.lib.media.api.IVideoEditor
import com.app.douyin.pro.lib.media.model.EditingTimeline
import java.io.File
import kotlinx.coroutines.*

@OptIn(UnstableApi::class)
class VideoEditorHelper(private val context: Context) : IVideoEditor {

    private var transformer: Transformer? = null

    override fun exportTimeline(
        timeline: EditingTimeline,
        outputPath: String,
        listener: IVideoEditor.ExportListener
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

        val editedMediaItems = timeline.videoMainTrack.map { clip ->
            // Enforce precise frame-exact clipping by ensuring startsAtKeyFrame is false
            val clippingConfig = MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(clip.startMs)
                .setEndPositionMs(clip.endMs)
                .setStartsAtKeyFrame(false) // Force exact trimming, regardless of I-Frame
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(clip.uri)
                .setClippingConfiguration(clippingConfig)
                .build()

            val audioProcessors = mutableListOf<AudioProcessor>()
            if (clip.speed != 1.0f) {
                audioProcessors.add(SonicAudioProcessor().apply {
                    setSpeed(clip.speed)
                    setPitch(1.0f)
                })
            }

            val videoEffects = mutableListOf<androidx.media3.common.Effect>()
            if (clip.speed != 1.0f) {
                videoEffects.add(SpeedChangeEffect(clip.speed))
            }

            val effects = Effects(audioProcessors, videoEffects)

            EditedMediaItem.Builder(mediaItem)
                .setRemoveAudio(clip.volume == 0f)
                .setEffects(effects)
                .build()
        }

        // To safely stitch multiple clips with precise cuts, we use an EditedMediaItemSequence
        val videoSequence = EditedMediaItemSequence(editedMediaItems)

        val audioSequences = timeline.audioTracks.map { track ->
            val mediaItem = MediaItem.Builder()
                .setUri(track.uri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(track.clipStartMs)
                        .setEndPositionMs(track.clipEndMs)
                        .setStartsAtKeyFrame(false)
                        .build()
                )
                .build()

            EditedMediaItemSequence(EditedMediaItem.Builder(mediaItem).build())
        }

        val sequences = mutableListOf<EditedMediaItemSequence>()
        sequences.add(videoSequence)
        sequences.addAll(audioSequences)

        // Force synchronous video/audio composition
        val composition = Composition.Builder(sequences)
            .build()

        transformer.start(composition, outputPath)

        CoroutineScope(Dispatchers.Main).launch {
            while (transformer.getProgress(ProgressHolder()) != Transformer.PROGRESS_STATE_NOT_STARTED) {
                val progressHolder = ProgressHolder()
                val state = transformer.getProgress(progressHolder)
                if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                    listener.onProgress(progressHolder.progress)
                }
                delay(200)
            }
        }
    }

    override fun getProgress(): Int {
        val progressHolder = ProgressHolder()
        val state = transformer?.getProgress(progressHolder)
        return if (state == Transformer.PROGRESS_STATE_AVAILABLE) progressHolder.progress else -1
    }

    override fun cancel() {
        transformer?.cancel()
    }
}
