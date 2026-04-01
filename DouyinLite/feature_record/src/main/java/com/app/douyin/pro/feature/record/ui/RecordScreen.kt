package com.app.douyin.pro.feature.record.ui

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.video.FileOutputOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.app.douyin.pro.feature.record.gl.CameraGLSurfaceView
import java.util.concurrent.Executors

import java.io.File

@SuppressLint("RestrictedApi")
@Composable
fun RecordScreen(onNavigateToEdit: (String) -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var isRecording by remember { mutableStateOf(false) }

    // Hold reference to VideoCapture and Recording
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                CameraGLSurfaceView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    onSurfaceTextureReady = { surfaceTexture ->
                        // Bind CameraX
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build()

                            // Feed SurfaceTexture to CameraX
                            preview.setSurfaceProvider { request: SurfaceRequest ->
                                val resolution = request.resolution
                                surfaceTexture.setDefaultBufferSize(resolution.width, resolution.height)
                                val surface = Surface(surfaceTexture)
                                request.provideSurface(surface, ContextCompat.getMainExecutor(ctx)) {
                                    // Surface is no longer used by CameraX
                                    surface.release()
                                }
                            }

                            val recorder = Recorder.Builder()
                                .setQualitySelector(QualitySelector.from(Quality.HIGHEST, FallbackStrategy.higherQualityOrLowerThan(Quality.SD)))
                                .build()
                            val capture = VideoCapture.withOutput(recorder)
                            videoCapture = capture

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    capture
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                    }

                    initRenderer()
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                // Handle updates if lensFacing changes, rebinding is done in another LaunchedEffect
                // for simplicity here we assume re-bind logic is handled.
            }
        )

        // Switch Camera Button
        IconButton(
            onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Switch Camera", tint = Color.White)
        }

        // Record Button
        Button(
            onClick = {
                if (isRecording) {
                    // Stop recording
                    activeRecording?.stop()
                    isRecording = false
                } else {
                    // Start recording
                    val videoFile = File(context.cacheDir, "recorded_video_${System.currentTimeMillis()}.mp4")
                    val outputOptions = FileOutputOptions.Builder(videoFile).build()

                    val recording = videoCapture?.output
                        ?.prepareRecording(context, outputOptions)
                        ?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                            when(recordEvent) {
                                is VideoRecordEvent.Start -> {
                                    isRecording = true
                                }
                                is VideoRecordEvent.Finalize -> {
                                    if (!recordEvent.hasError()) {
                                        onNavigateToEdit(videoFile.absolutePath)
                                    } else {
                                        // Handle recording error (e.g., delete file)
                                        videoFile.delete()
                                        isRecording = false
                                    }
                                }
                            }
                        }
                    activeRecording = recording
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color.DarkGray else Color.Red)
        ) {}
    }

    // Re-bind when lensFacing changes
    LaunchedEffect(lensFacing) {
        val cameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()
        // Wait for onSurfaceTextureReady to be called to re-bind preview in the full pipeline.
    }
}
