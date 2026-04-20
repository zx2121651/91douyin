package com.app.douyin.pro.feature.record.ui

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
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
import androidx.compose.material.icons.filled.CloudDownload
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
import com.app.douyin.pro.feature.record.gl.CameraSurfaceProcessor
import com.app.douyin.pro.feature.record.ui.vm.RecordViewModel
import com.app.douyin.pro.feature.record.ui.state.PermissionStatus
import com.app.douyin.pro.feature.record.ui.state.RecordState
import com.app.douyin.pro.feature.record.util.RecordGuard
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.app.Activity
import androidx.core.app.ActivityCompat

import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import java.util.concurrent.Executors
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import androidx.compose.ui.graphics.drawscope.Stroke

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

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.updatePermissionStatus(PermissionStatus.GRANTED)
        } else {
            val activity = context as? Activity
            val showRationale = activity?.let { act ->
                RecordGuard.getRequiredPermissions().any { perm ->
                    ActivityCompat.shouldShowRequestPermissionRationale(act, perm)
                }
            } ?: true

            if (!showRationale) {
                viewModel.updatePermissionStatus(PermissionStatus.PERMANENTLY_DENIED)
            } else {
                viewModel.updatePermissionStatus(PermissionStatus.DENIED)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkDeviceCapabilities(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (!RecordGuard.isAllPermissionsGranted(context)) {
            permissionLauncher.launch(RecordGuard.getRequiredPermissions())
        }
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    val surfaceProcessor = remember { CameraSurfaceProcessor() }

    DisposableEffect(Unit) {
        onDispose {
            surfaceProcessor.release()
        }
    }

    // Initialize FaceTracker (it will fail silently if the model asset isn't bundled,
    // but provides the architecture for MediaPipe AI processing on camera frames)
    val faceTracker = remember { FaceTracker(context) }
    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    val nosePosition by faceTracker.nosePosition.collectAsState()

    fun bindCamera(previewView: PreviewView) {
        if (uiState.permissionStatus != PermissionStatus.GRANTED) return

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(analyzerExecutor, faceTracker)
                }

            val cameraSelector = CameraSelector.Builder().requireLensFacing(uiState.lensFacing).build()

            val effect = object : CameraEffect(
                PREVIEW or VIDEO_CAPTURE,
                ContextCompat.getMainExecutor(context),
                surfaceProcessor,
                { /* error handler */ }
            ) {}

            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(videoCapture!!)
                .addUseCase(imageAnalyzer)
                .addEffect(effect)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
            } catch (e: Exception) { e.printStackTrace() }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (uiState.permissionStatus == PermissionStatus.GRANTED) {
            LaunchedEffect(uiState.selectedFilter) {
                uiState.selectedFilter?.let { filter ->
                    surfaceProcessor.setFilter(filter.name)
                } ?: run {
                    surfaceProcessor.setFilter("原片")
                }
            }

            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        bindCamera(this)
                    }
                },
                update = {
                   // Optional updates to PreviewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // UI Components
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(16.dp))
                RecordProgressBar(
                    segments = uiState.segments,
                    totalDurationMs = uiState.totalDurationMs,
                    maxDurationMs = 60000L, // 60s limit
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(4.dp)
                )
                TopControls(onClose = { }, onToggleLens = viewModel::toggleLens)
            }

            SideControls(
                onShowFilters = { viewModel.setShowFilters(true) },
                onStartCountdown = { viewModel.startCountdown(3) }
            )

            if (!uiState.capabilities.hasMic) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("麦克风不可用，将录制无声视频", color = Color.Yellow, fontSize = 12.sp)
                }
            }

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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
            ) {
                // Delete button (only show when not recording and has segments)
                if (!uiState.isRecording && uiState.segments.isNotEmpty()) {
                    IconButton(
                        onClick = viewModel::deleteLastSegment,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 48.dp)
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Backspace, contentDescription = "Delete Last", tint = Color.White)
                    }
                }

                RecordButton(
                    isRecording = uiState.isRecording,
                    modifier = Modifier.align(Alignment.Center),
                    onClick = {
                        if (uiState.isRecording) {
                            activeRecording?.stop()
                        } else {
                            val videoFile = File(context.cacheDir, "recorded_${System.currentTimeMillis()}.mp4")
                            activeRecording = videoCapture?.output
                                ?.prepareRecording(context, FileOutputOptions.Builder(videoFile).build())
                                ?.apply { if (uiState.capabilities.hasMic) withAudioEnabled() }
                                ?.start(ContextCompat.getMainExecutor(context)) { event ->
                                    if (event is VideoRecordEvent.Start) {
                                        viewModel.startRecording()
                                    }
                                    if (event is VideoRecordEvent.Finalize) {
                                        if (!event.hasError()) {
                                            val duration = event.recordingStats.recordedDurationNanos / 1_000_000
                                            viewModel.pauseRecording(videoFile.absolutePath, duration)
                                        } else {
                                            viewModel.setRecording(false)
                                        }
                                    }
                                }
                        }
                    }
                )

                // Done button (show when has segments)
                if (!uiState.isRecording && uiState.segments.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            viewModel.completeRecording()
                            val segmentsJson = com.google.gson.Gson().toJson(uiState.segments)
                            onNavigateToEdit(segmentsJson)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 48.dp)
                            .size(48.dp)
                            .background(Color(0xFFFF2C55), CircleShape)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White)
                    }
                }
            }
        } else {
            PermissionGuardView(
                status = uiState.permissionStatus,
                onRequestPermissions = {
                    permissionLauncher.launch(RecordGuard.getRequiredPermissions())
                },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun PermissionGuardView(
    status: PermissionStatus,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Camera,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "需要权限才能开始拍摄",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "请授予相机、麦克风和存储权限，以使用拍摄功能。",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = if (status == PermissionStatus.PERMANENTLY_DENIED) onOpenSettings else onRequestPermissions,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2C55))
        ) {
            Text(if (status == PermissionStatus.PERMANENTLY_DENIED) "去设置" else "去授权")
        }
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
fun FilterPanel(filters: List<com.app.douyin.pro.feature.record.domain.model.FilterEffect>, selectedFilter: com.app.douyin.pro.feature.record.domain.model.FilterEffect?, onSelectFilter: (com.app.douyin.pro.feature.record.domain.model.FilterEffect) -> Unit, onDismiss: () -> Unit) {
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
                        val isSelected = selectedFilter?.name == filter.name
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelectFilter(filter) }) {
                            Box(
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color(0xFFFF2C55) else Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                if (filter.isDynamic) {
                                    Icon(Icons.Filled.CloudDownload, "Cloud", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
                                }
                            }
                            Text(filter.name, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordProgressBar(
    segments: List<com.app.douyin.pro.feature.record.domain.model.RecordSegment>,
    totalDurationMs: Long,
    maxDurationMs: Long,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Background
        drawRect(color = Color.White.copy(alpha = 0.3f), size = size)

        var currentX = 0f
        segments.forEach { segment ->
            val segmentWidth = (segment.durationMs.toFloat() / maxDurationMs) * width
            drawRect(
                color = Color(0xFFFF2C55),
                topLeft = Offset(currentX, 0f),
                size = androidx.compose.ui.geometry.Size(segmentWidth - 2.dp.toPx(), height)
            )
            currentX += segmentWidth
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
