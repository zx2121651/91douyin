package com.app.douyin.pro.feature.edit.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.douyin.pro.feature.edit.ui.component.TimelineArea
import com.app.douyin.pro.feature.edit.ui.vm.EditViewModel
import com.app.douyin.pro.lib.media.util.MediaMetadataUtils
import androidx.compose.ui.platform.LocalContext

@Composable
fun EditScreen(
    videoUri: String,
    segmentsJson: String = "",
    onClose: () -> Unit,
    onNext: (Uri) -> Unit,
    viewModel: EditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(videoUri, segmentsJson) {
        if (segmentsJson.isNotEmpty()) {
            viewModel.initProjectWithSegments(segmentsJson)
        } else if (videoUri.isNotEmpty()) {
            val uri = Uri.parse(videoUri)
            val duration = MediaMetadataUtils.getVideoDurationMs(context, uri)
            viewModel.initProject(uri, if (duration > 0) duration else 15000L)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                isExporting = uiState.isExporting,
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                onClose = onClose,
                onNext = { viewModel.exportProject { uri -> onNext(uri) } }
            )
        },
        bottomBar = {
            BottomBar(
                onSplit = viewModel::splitClip,
                onDelete = viewModel::deleteSelectedClip
            )
        },
        containerColor = Color(0xFF161823)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                PreviewPanel(
                    isPlaying = uiState.isPlaying,
                    onTogglePlay = viewModel::togglePlay,
                    modifier = Modifier.weight(1f)
                )

                TimelineArea(
                    tracks = uiState.tracks,
                    currentTimeMs = uiState.currentTimeMs,
                    totalDurationMs = uiState.totalDurationMs,
                    onSeek = viewModel::updateCurrentTime,
                    onSelectClip = viewModel::selectClip
                )
            }

            if (uiState.isExporting) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(progress = uiState.exportProgress / 100f, color = Color(0xFFFF2C55))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在导出 ${uiState.exportProgress}%", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(
    isExporting: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClose: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
        }

        Row {
            IconButton(onClick = onUndo, enabled = canUndo) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = if (canUndo) Color.White else Color.Gray)
            }
            IconButton(onClick = onRedo, enabled = canRedo) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = if (canRedo) Color.White else Color.Gray)
            }
        }

        Button(
            onClick = onNext,
            enabled = !isExporting,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2C55)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("下一步", fontSize = 14.sp)
        }
    }
}

@Composable
fun PreviewPanel(isPlaying: Boolean, onTogglePlay: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(16.dp).background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onTogglePlay) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun BottomBar(onSplit: () -> Unit, onDelete: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF161823)).padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EditActionItem(Icons.Filled.ContentCut, "分割", onSplit)
            EditActionItem(Icons.Filled.Delete, "删除", onDelete)
        }
        Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Color.Black))
    }
}

@Composable
fun EditActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.White, fontSize = 10.sp)
    }
}
