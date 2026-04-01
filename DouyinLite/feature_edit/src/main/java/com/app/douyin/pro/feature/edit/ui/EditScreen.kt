package com.app.douyin.pro.feature.edit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF161823)) // Douyin dark background
            .systemBarsPadding()
    ) {
        TopBar()
        VideoPreviewArea(modifier = Modifier.weight(1f))
        TimelineArea()
        BottomToolbar()
    }
}

@Composable
fun TopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Close",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(28.dp)
                .clickable { /* Handle close */ }
        )

        // Resolution and Frame Rate Settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color(0xFF2E303C), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable { /* Handle settings */ }
        ) {
            Text("1080P", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.size(3.dp).background(Color.Gray, RoundedCornerShape(1.5.dp)))
            Spacer(modifier = Modifier.width(4.dp))
            Text("30", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Settings", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }

        // Export/Next Button
        Button(
            onClick = { /* Handle Export */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2C55)), // Douyin Red
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(32.dp)
        ) {
            Text("下一步", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VideoPreviewArea(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder background mock to simulate video
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E202B).copy(alpha = 0.5f)))

        // Placeholder for the actual video player
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("预览区域", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
        }

        // Timeline indicator (current time)
        Text(
            text = "00:03 / 00:15",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun TimelineArea() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFF1E202B))
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        // Time Ruler
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("00:00", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Medium)

            // Subtle ruler marks
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { Box(modifier = Modifier.width(2.dp).height(4.dp).background(Color.White.copy(alpha = 0.2f))) }
            }
            Text("00:05", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Medium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { Box(modifier = Modifier.width(2.dp).height(4.dp).background(Color.White.copy(alpha = 0.2f))) }
            }
            Text("00:10", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Medium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { Box(modifier = Modifier.width(2.dp).height(4.dp).background(Color.White.copy(alpha = 0.2f))) }
            }
            Text("00:15", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            // Video Track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(64.dp)
                    .background(Color(0xFF2E303C), RoundedCornerShape(6.dp))
                    .border(2.dp, Color(0xFF35FBF5), RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
            ) {
                // Mock Thumbnails with some content
                for (i in 0..4) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (i % 2 == 0) Color(0xFF454752) else Color(0xFF3E404C))
                            .border(0.5.dp, Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(24.dp))
                    }
                }
            }

            // Audio Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 76.dp)
                    .height(36.dp)
                    .background(Color(0xFF00B3FF).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF00B3FF).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .clickable { /* Add Audio */ },
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Audio", tint = Color(0xFF00B3FF), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("点击添加音频", color = Color(0xFF00B3FF), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Playhead (Vertical White Line with a custom handle)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-40).dp)
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-6).dp)
                        .size(10.dp)
                        .background(Color.White, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

data class EditTool(val title: String, val icon: ImageVector)

@Composable
fun BottomToolbar() {
    val tools = listOf(
        EditTool("剪辑", Icons.Filled.Edit),
        EditTool("音频", Icons.Filled.PlayArrow),
        EditTool("文字", Icons.Filled.Create),
        EditTool("贴纸", Icons.Filled.Face),
        EditTool("特效", Icons.Filled.Star),
        EditTool("画中画", Icons.Filled.AddCircle),
        EditTool("滤镜", Icons.Filled.ThumbUp),
        EditTool("比例", Icons.Filled.Settings),
        EditTool("背景", Icons.Filled.Menu)
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(Color(0xFF161823))
            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(tools) { tool ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { /* Tool clicked */ }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tool.title,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
