package com.app.douyin.pro.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.app.douyin.pro.feature.home.domain.model.CommentModel
import com.app.douyin.pro.feature.home.domain.model.CommentStatus
import com.app.douyin.pro.feature.home.viewmodel.CommentUiState
import com.app.douyin.pro.feature.home.viewmodel.CommentViewModel
import com.app.douyin.pro.lib.media.util.CountFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    videoId: Long,
    commentCount: Long,
    onDismiss: () -> Unit,
    viewModel: CommentViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val comments by viewModel.comments.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var replyToComment by remember { mutableStateOf<CommentModel?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF161823),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.Gray.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(24.dp))
                    Text("全部评论 (${CountFormatter.format(commentCount)})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.7f).fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val state = uiState) {
                    is CommentUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFFF2C55))
                        }
                    }
                    is CommentUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.message, color = Color.Gray)
                            Button(
                                onClick = { viewModel.retryLoadComments() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2C55)),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("重试")
                            }
                        }
                    }
                    is CommentUiState.Success -> {
                        if (comments.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("暂无评论，快来抢沙发吧~", color = Color.Gray, fontSize = 14.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                items(comments, key = { it.tempId ?: it.id }) { comment ->
                                    CommentRow(
                                        comment = comment,
                                        onReply = { replyToComment = it },
                                        onRetrySend = { viewModel.retryPostComment(it, videoId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Input Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF252632))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                replyToComment?.let { replyTarget ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("回复 @${replyTarget.authorName}:", color = Color.Gray, fontSize = 12.sp)
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cancel reply",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp).clickable { replyToComment = null }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("善语结善缘，恶言伤人心", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF383A4A),
                            unfocusedContainerColor = Color(0xFF383A4A),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    val text = inputText
                                    viewModel.postComment(videoId, text, replyToComment?.id) {
                                        // only clear if it was actually this text
                                    }
                                    inputText = ""
                                    replyToComment = null
                                }
                            }
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "发送",
                        color = if (inputText.isNotBlank()) Color(0xFFFF2C55) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = inputText.isNotBlank()) {
                            val text = inputText
                            viewModel.postComment(videoId, text, replyToComment?.id)
                            inputText = ""
                            replyToComment = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CommentRow(
    comment: CommentModel,
    onReply: (CommentModel) -> Unit,
    onRetrySend: (CommentModel) -> Unit = {},
    isSubComment: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .alpha(if (comment.status == CommentStatus.SENDING) 0.5f else 1f)
    ) {
        AsyncImage(
            model = comment.authorAvatar,
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(if (isSubComment) 24.dp else 36.dp).clip(CircleShape).background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(comment.authorName, color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.clickable { if (comment.status == CommentStatus.SUCCESS) onReply(comment) }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.createDate, color = Color.Gray, fontSize = 12.sp)
                if (comment.status == CommentStatus.SUCCESS) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("回复", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.clickable { onReply(comment) })
                }

                if (comment.status == CommentStatus.FAILED) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        modifier = Modifier.clickable { onRetrySend(comment) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Retry", tint = Color(0xFFFF2C55), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("发送失败，点击重试", color = Color(0xFFFF2C55), fontSize = 12.sp)
                    }
                }

                if (comment.status == CommentStatus.SENDING) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("发送中...", color = Color.Gray, fontSize = 12.sp)
                }
            }

            // Render Sub-comments (Replies)
            if (comment.replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    comment.replies.forEach { subComment ->
                        CommentRow(comment = subComment, onReply = onReply, onRetrySend = onRetrySend, isSubComment = true)
                    }
                    if (comment.replyCount > comment.replies.size) {
                        Text(
                            "展开更多回复",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp).clickable { /* Load more logic */ }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.FavoriteBorder, contentDescription = "Like", tint = Color.Gray, modifier = Modifier.size(20.dp))
            Text("0", color = Color.Gray, fontSize = 12.sp) // Mock like count for comment
        }
    }
}
