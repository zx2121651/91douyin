package com.app.douyin.pro.feature.record.ui

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("RestrictedApi")
@Composable
fun RecordScreen(onNavigateToEdit: (String) -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var isRecording by remember { mutableStateOf(false) }

    var cameraGLSurfaceView by remember { mutableStateOf<CameraGLSurfaceView?>(null) }
    var currentSurfaceTexture by remember { mutableStateOf<SurfaceTexture?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                CameraGLSurfaceView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    cameraGLSurfaceView = this

                    onSurfaceTextureReady = { surfaceTexture ->
                        currentSurfaceTexture = surfaceTexture
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

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview
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
                // Do not rebind unnecessarily on view updates
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
                    isRecording = false
                    // Mock: Assuming video was saved to cache dir
                    val dummyVideoPath = File(context.cacheDir, "mock_recorded_video.mp4").absolutePath
                    // In a real app, video encoding stops and we await the actual file URI.
                    onNavigateToEdit(dummyVideoPath)
                } else {
                    // Start recording
                    isRecording = true
                    // Trigger encoding start here in a real scenario via view model calling GL thread
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
        if (currentSurfaceTexture != null) {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()

            preview.setSurfaceProvider { request: SurfaceRequest ->
                val surface = Surface(currentSurfaceTexture)
                request.provideSurface(surface, ContextCompat.getMainExecutor(context)) {
                    surface.release()
                }
            }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
