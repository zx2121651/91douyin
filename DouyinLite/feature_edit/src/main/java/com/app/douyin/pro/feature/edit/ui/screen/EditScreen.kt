package com.app.douyin.pro.feature.edit.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.douyin.pro.feature.edit.ui.component.TimelineArea
import com.app.douyin.pro.feature.edit.ui.vm.EditViewModel

@Composable
fun EditScreen(
    videoUri: String,
    onClose: () -> Unit,
    onNext: () -> Unit,
    viewModel: EditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(videoUri) {
        if (videoUri.isNotEmpty()) {
            // In real app, we would get duration from MediaMetadataRetriever
            viewModel.initProject(Uri.parse(videoUri), 15000L)
        }
    }

    Scaffold(
        topBar = {
            // Simplified TopBar
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onClose) { Text("取消", color = Color.White) }
                Button(onClick = onNext) { Text("下一步") }
            }
        },
        bottomBar = {
            Column {
                // Quick Actions (Split)
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF161823)).padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { viewModel.splitClip() }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.ContentCut, contentDescription = "分割", tint = Color.White)
                            Text("分割", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
                // Placeholder for Toolbar
                Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Color.Black))
            }
        },
        containerColor = Color(0xFF161823)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Preview Placeholder
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp).background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text("Video Preview", color = Color.White)
            }

            TimelineArea(
                tracks = uiState.tracks,
                currentTimeMs = uiState.currentTimeMs,
                totalDurationMs = uiState.totalDurationMs
            )
        }
    }
}
