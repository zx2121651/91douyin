package com.app.douyin.pro.feature.record.ui
import java.io.File

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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.app.douyin.pro.feature.record.gl.CameraGLSurfaceView
import java.util.concurrent.Executors
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

    // Hold reference to VideoCapture and Recording
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. Camera View
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
            update = { _ ->
                // Do not rebind unnecessarily on view updates
            }
        )

        // 2. Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // 3. Top Navigation Layer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* close */ }) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }

            // Music Selector
            Row(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { /* select music */ },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Music", tint = Color(0xFFffb3b6), modifier = Modifier.size(14.dp))
                Text("选择音乐", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            IconButton(onClick = { /* help */ }) {
                Icon(Icons.Filled.Info, contentDescription = "Help", tint = Color.White)
            }
        }

        // 4. Right Side Vertical Toolbar
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 96.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ToolbarItem(icon = Icons.Filled.Refresh, text = "翻转", onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
            })
            ToolbarItem(icon = Icons.Filled.PlayArrow, text = "快慢速")
            ToolbarItem(icon = Icons.Filled.Star, text = "滤镜")
            ToolbarItem(icon = Icons.Filled.Face, text = "美化")
            ToolbarItem(icon = Icons.Filled.Build, text = "计时器")
            ToolbarItem(icon = Icons.Filled.Share, text = "回复")

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.2f), shape = CircleShape)
                    .clickable { /* expand */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Expand", tint = Color.White)
            }
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
        // 5. Bottom Controls Container
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Center Interaction Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Effects Thumbnail
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { /* open effects */ }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .background(Color(0xFF323440), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuD7B52Q9MVFL7lnSKWQ-WJZzo2FwMRItL_ToXH1oYcn6tJLsalgf3xO2zI6p1HWabdE6v_jhix516EPLhmbbLn-5PxAYT2QfzTR9pcbBLOupIUU3pz7R0g---3-6aG-Pbrbny_0aAKGILj3vAye_dzSVYGZpU8Bj3mia2PruXl7UKiFMDo_zYFTqOLNeNG56zkSgwW4N-7e7g08q78e1KmyFSJU82nbDZyEsunNKnAbSQMFOG0AQx4rF_T1Yd4qWwDvr1aRs-3QUXY",
                            contentDescription = "Effects",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text("特效", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Record Button
                Box(
                    modifier = Modifier
                        .size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(6.dp, Color(0xFFffb3b6).copy(alpha = 0.3f), CircleShape)
                    )

                    // Inner Button
                    Box(
                        modifier = Modifier
                            .size(if (isRecording) 40.dp else 80.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFffb3b6), Color(0xFFff5168))
                                ),
                                shape = if (isRecording) RoundedCornerShape(8.dp) else CircleShape
                            )
                            .clickable {
                                if (!isRecording) {
                                    // Start Recording
                                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                                    val outputPath = File(context.getExternalFilesDir(null), "VID_${timestamp}.mp4").absolutePath
                                    cameraGLSurfaceView?.startRecording(outputPath)
                                    isRecording = true
                                } else {
                                    // Stop Recording
                                    cameraGLSurfaceView?.stopRecording()
                                    isRecording = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isRecording) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(4.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                            )
                        }
                    }
                }

                // Album Thumbnail
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { /* open album */ }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .background(Color(0xFF323440), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDVG4oJ8Zc7gBazCKApxyRvkwM1H751UtX8ANXH7r0pIGz0YTWcCJT-4KqEa-w-JfLWmbBreZkbLfT9Mx-SC8n07eHzMiNVeEbedMMDYbjqrM7kzdqm6M9WJnxMisux0jvAOEoDpMiyb1FD-bpAUOGAgfXCSBrCXpou2gbe3hRmmq4LtXKYbDMGbEGjgf1qIpt6109IVFcsWQNToHxB5r-PbdzIy6a_kpOgQCukDI23_w38D4is1xFN4y-_kTH5fa9VGSsWWrRQ1KU",
                            contentDescription = "Album",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text("相册", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Mode Selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("文字", color = Color.Gray.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("分段拍", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("快拍", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.width(20.dp).height(2.dp).background(Color(0xFFffb3b6)))
                }

                Text("影集", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("开直播", color = Color.Gray.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Re-bind when lensFacing changes
    LaunchedEffect(lensFacing) {
        val cameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()
        // Wait for onSurfaceTextureReady to be called to re-bind preview in the full pipeline.
    }
}

@Composable
fun ToolbarItem(icon: ImageVector, text: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.2f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}
