package com.app.douyin.pro.feature.record.ui

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.douyin.pro.feature.record.gl.CameraGLSurfaceView
import com.app.douyin.pro.feature.record.ui.vm.RecordViewModel
import java.io.File

@SuppressLint("RestrictedApi")
@Composable
fun RecordScreen(
    onNavigateToEdit: (String) -> Unit = {},
    viewModel: RecordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val availableFilters by viewModel.availableFilters.collectAsState()

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var previewTexture by remember { mutableStateOf<SurfaceTexture?>(null) }

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
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.Builder().requireLensFacing(uiState.lensFacing).build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
            } catch (e: Exception) { e.printStackTrace() }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(uiState.lensFacing, previewTexture) {
        previewTexture?.let { bindCamera(it) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        var glSurfaceView by remember { mutableStateOf<CameraGLSurfaceView?>(null) }

        LaunchedEffect(uiState.selectedFilter) {
            glSurfaceView?.setFilter(uiState.selectedFilter)
        }

        AndroidView(
            factory = { ctx ->
                CameraGLSurfaceView(ctx).apply {
                    onSurfaceTextureReady = { st ->
                        previewTexture = st
                        bindCamera(st)
                    }
                    initRenderer()
                    glSurfaceView = this
                }
            },

            modifier = Modifier.fillMaxSize()
        )

        // UI Components
        TopControls(onClose = { }, onToggleLens = viewModel::toggleLens)

        SideControls(
            onShowFilters = { viewModel.setShowFilters(true) },
            onStartCountdown = { viewModel.startCountdown(3) }
        )

        if (uiState.countdownTime > 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.countdownTime.toString(), color = Color.White, fontSize = 120.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }

        if (uiState.showFilters) {
            FilterPanel(
                filters = availableFilters,
                selectedFilter = uiState.selectedFilter,
                onSelectFilter = viewModel::selectFilter,
                onDismiss = { viewModel.setShowFilters(false) }
            )
        }

        RecordButton(
            isRecording = uiState.isRecording,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
            onClick = {
                if (uiState.isRecording) {
                    activeRecording?.stop()
                    viewModel.setRecording(false)
                } else {
                    val videoFile = File(context.cacheDir, "recorded_${System.currentTimeMillis()}.mp4")
                    activeRecording = videoCapture?.output
                        ?.prepareRecording(context, FileOutputOptions.Builder(videoFile).build())
                        ?.start(ContextCompat.getMainExecutor(context)) { event ->
                            if (event is VideoRecordEvent.Start) viewModel.setRecording(true)
                            if (event is VideoRecordEvent.Finalize) {
                                viewModel.setRecording(false)
                                if (!event.hasError()) onNavigateToEdit(videoFile.absolutePath)
                            }
                        }
                }
            }
        )
    }
}

@Composable
fun TopControls(onClose: () -> Unit, onToggleLens: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "Close", tint = Color.White) }
        Text("拍摄", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        IconButton(onClick = onToggleLens) { Icon(Icons.Default.Refresh, "Flip", tint = Color.White) }
    }
}

@Composable
fun SideControls(onShowFilters: () -> Unit, onStartCountdown: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxHeight().padding(end = 16.dp).wrapContentWidth().padding(top = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ControlItem(Icons.Default.Face, "滤镜", onShowFilters)
        ControlItem(Icons.Default.Notifications, "倒计时", onStartCountdown)
    }
}

@Composable
fun ControlItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
        Text(label, color = Color.White, fontSize = 10.sp)
    }
}

@Composable
fun FilterPanel(filters: List<String>, selectedFilter: String, onSelectFilter: (String) -> Unit, onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().clickable { onDismiss() }) {
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.8f)).padding(16.dp)
        ) {
            Text("选择滤镜", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (filters.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF2C55))
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filters) { filter ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelectFilter(filter) }) {
                            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)).background(if (filter == selectedFilter) Color(0xFFFF2C55) else Color.DarkGray))
                            Text(filter, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordButton(isRecording: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(84.dp)
            .border(4.dp, Color.White, CircleShape)
            .padding(6.dp)
            .clip(CircleShape)
            .background(if (isRecording) Color.White else Color(0xFFFF2C55))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isRecording) {
            Box(modifier = Modifier.size(32.dp).background(Color(0xFFFF2C55), RoundedCornerShape(4.dp)))
        }
    }
}
