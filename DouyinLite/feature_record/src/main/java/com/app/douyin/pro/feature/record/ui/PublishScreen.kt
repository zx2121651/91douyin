package com.app.douyin.pro.feature.record.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.douyin.pro.feature.record.ui.component.VideoPlayerComponent
import com.app.douyin.pro.feature.record.ui.vm.PublishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    videoUri: String,
    onBack: () -> Unit,
    onPublishSuccess: () -> Unit,
    viewModel: PublishViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(videoUri) {
        viewModel.setVideoUri(videoUri)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onPublishSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF161823),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF161823)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    // Video Preview
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                    ) {
                        if (uiState.videoUri.isNotEmpty()) {
                            VideoPlayerComponent(
                                url = uiState.videoUri,
                                isVisible = true,
                                isPaused = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Title Input
                    TextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChanged,
                        placeholder = { Text("添加作品描述...", color = Color.Gray) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.publish() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2C55)),
                    shape = RoundedCornerShape(4.dp),
                    enabled = !uiState.isPublishing
                ) {
                    if (uiState.isPublishing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("发布", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("确定", color = Color(0xFFFF2C55))
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}
