package com.app.douyin.pro.feature.edit.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.app.douyin.pro.feature.edit.domain.model.EditTrack
import com.app.douyin.pro.feature.edit.domain.model.TrackType

@Composable
fun TimelineArea(
    tracks: List<EditTrack>,
    currentTimeMs: Long,
    totalDurationMs: Long,
    onSeek: (Long) -> Unit,
    onSelectClip: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFF1E202B))
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
             Text(
                "${formatTime(currentTimeMs)} / ${formatTime(totalDurationMs)}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(totalDurationMs) {
                    detectDragGestures { _, dragAmount ->
                        val ratio = dragAmount.x / size.width
                        val seekDelta = (ratio * totalDurationMs).toLong()
                        onSeek((currentTimeMs + seekDelta).coerceIn(0, totalDurationMs))
                    }
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                tracks.forEach { track ->
                    TrackRow(track, onSelectClip)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            val progress = if (totalDurationMs > 0) currentTimeMs.toFloat() / totalDurationMs else 0f
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .offset(x = (16.dp + (320.dp * progress))) // Approximate track width
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun TrackRow(track: EditTrack, onSelectClip: (String) -> Unit) {
    val bgColor = when(track.type) {
        TrackType.VIDEO -> Color(0xFF2E303C)
        TrackType.AUDIO -> Color(0xFF00B3FF).copy(alpha = 0.15f)
        TrackType.TEXT -> Color(0xFFE2A500).copy(alpha = 0.15f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(if (track.type == TrackType.VIDEO) 64.dp else 36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
    ) {
        track.clips.forEach { clip ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(clip.getTimelineDurationMs().coerceAtLeast(1L).toFloat())
                    .background(Color.Gray.copy(alpha = 0.3f))
                    .border(0.5.dp, Color.Black.copy(alpha = 0.3f))
                    .clickable { onSelectClip(clip.id) },
                contentAlignment = Alignment.Center
            ) {
                if (track.type == TrackType.VIDEO) {
                    Text("Clip", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    return "%02d:%02d".format(minutes, seconds)
}
