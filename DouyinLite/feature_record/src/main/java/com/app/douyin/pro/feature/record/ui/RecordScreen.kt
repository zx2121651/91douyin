package com.app.douyin.pro.feature.record.ui

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Face

import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.app.douyin.pro.feature.record.gl.CameraGLSurfaceView
import java.io.File

@SuppressLint("RestrictedApi")
@Composable
fun RecordScreen(onNavigateToEdit: (String) -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var isRecording by remember { mutableStateOf(false) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var previewTexture by remember { mutableStateOf<SurfaceTexture?>(null) }
    var countdownTime by remember { mutableIntStateOf(0) }
    var showFilters by remember { mutableStateOf(false) }

    fun bindCamera(surfaceTexture: SurfaceTexture) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider { request: SurfaceRequest ->
                val resolution = request.resolution
                surfaceTexture.setDefaultBufferSize(resolution.width, resolution.height)
                val surface = Surface(surfaceTexture)
                request.provideSurface(surface, ContextCompat.getMainExecutor(context)) {
                    surface.release()
                }
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(
                        Quality.HIGHEST,
                        FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                    )
                )
                .build()
            val capture = VideoCapture.withOutput(recorder)
            videoCapture = capture

            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(lensFacing, previewTexture) {
        previewTexture?.let { bindCamera(it) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        if (countdownTime > 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = countdownTime.toString(),
                    color = Color.White,
                    fontSize = 120.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }

        AndroidView(
            factory = { ctx ->
                CameraGLSurfaceView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    onSurfaceTextureReady = { surfaceTexture ->
                        previewTexture = surfaceTexture
                        bindCamera(surfaceTexture)
                    }
                    initRenderer()
                }
            },
            update = { _ -> },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }
            Text("拍摄", color = Color.White)
            IconButton(onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Flip", tint = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = { }) { Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White) }

            IconButton(onClick = { showFilters = true }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Face, contentDescription = "Filters", tint = Color.White)
                    Text("滤镜", color = Color.White, fontSize = 10.sp)
                }
            }


            IconButton(onClick = { countdownTime = 3 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                    Text("3s", color = Color.White, fontSize = 10.sp)
                }
            }

            IconButton(onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
            }) { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White) }
        }


        if (showFilters) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(180.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { showFilters = false }
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("选择滤镜", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val filters = listOf("原图", "磨皮", "冷白", "复古", "胶片", "黑白")
                        items(filters) { filter ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (filter == "磨皮") Color(0xFFFF2C55) else Color.DarkGray)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(filter, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }


        FloatingActionButton(
            onClick = {
                if (isRecording) {
                    activeRecording?.stop()
                    isRecording = false
                } else {
                    val videoFile = File(context.cacheDir, "recorded_video_${System.currentTimeMillis()}.mp4")
                    val outputOptions = FileOutputOptions.Builder(videoFile).build()
                    activeRecording = videoCapture?.output
                        ?.prepareRecording(context, outputOptions)
                        ?.start(ContextCompat.getMainExecutor(context)) { event ->
                            when (event) {
                                is VideoRecordEvent.Start -> isRecording = true
                                is VideoRecordEvent.Finalize -> {
                                    isRecording = false
                                    if (!event.hasError()) {
                                        onNavigateToEdit(videoFile.absolutePath)
                                    } else {
                                        videoFile.delete()
                                    }
                                }
                            }
                        }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .size(84.dp)
                .border(3.dp, Color.White, CircleShape),
            containerColor = if (isRecording) Color(0xFFFF0050) else Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Record",
                tint = if (isRecording) Color.White else Color(0xFFFF0050),
                modifier = Modifier.size(38.dp)
            )
        }

        Text(
            text = if (isRecording) "录制中... 点击停止" else "点击开始录制",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                .clickable(enabled = false) {}
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}
