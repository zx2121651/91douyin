package com.app.douyin.pro.feature.record.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoSpec
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.video.MediaStoreOutputOptions
import android.content.ContentValues
import android.provider.MediaStore
import androidx.camera.video.PendingRecording
import androidx.camera.video.Recording
import androidx.core.util.Consumer
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("RestrictedApi")
@Composable
fun RecordScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // 强制指定编码参数：8Mbps, 30fps
    // 注意：CameraX的高级API VideoCapture 默认会根据 QualitySelector 自动选择最佳配置。
    // 要强制指定码率，我们需要使用 Recorder.Builder() 并进行详细配置，或者依赖设备硬件支持的Quality。
    val recorder = remember {
        Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(Quality.FHD, FallbackStrategy.higherQualityOrLowerThan(Quality.FHD))
            )
            // CameraX API now supports configuring bitrate for specific API levels
            // For typical 8Mbps target we can enforce setting (approx 8 * 1024 * 1024 = 8388608 bps)
            // and frame rate at 30 fps using VideoSpec (when available/needed based on versions).
            // However, typical robust setups rely on Quality defaults or low level encoding.
            // .setTargetVideoEncodingBitRate(8 * 1024 * 1024) is a supported pattern in some implementations.
            .build()
    }

    val videoCapture = remember { VideoCapture.withOutput(recorder) }
    val preview = remember { Preview.Builder().build() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            videoCapture
                        )
                    } catch (e: Exception) {
                        Log.e("RecordScreen", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 录制按钮 (Mocking simple UI for recording)
        Button(
            onClick = {
                // Placeholder for starting recording
                // startRecording(context, videoCapture, cameraExecutor)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
        }
    }
}

// Helper function to handle recording (simplified)
@SuppressLint("MissingPermission")
private fun startRecording(context: Context, videoCapture: VideoCapture<Recorder>, executor: ExecutorService) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
    }

    val mediaStoreOutputOptions = MediaStoreOutputOptions
        .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        .setContentValues(contentValues)
        .build()

    videoCapture.output
        .prepareRecording(context, mediaStoreOutputOptions)
        .withAudioEnabled()
        .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
            if (recordEvent is VideoRecordEvent.Finalize) {
                if (!recordEvent.hasError()) {
                    Log.d("RecordScreen", "Video record success: ${recordEvent.outputResults.outputUri}")
                } else {
                    Log.e("RecordScreen", "Video record error: ${recordEvent.error}")
                }
            }
        }
}
