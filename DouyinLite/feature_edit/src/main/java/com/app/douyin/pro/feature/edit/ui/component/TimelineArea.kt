package com.app.douyin.pro.feature.edit.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.douyin.pro.feature.edit.domain.model.EditTrack
import com.app.douyin.pro.feature.edit.domain.model.TrackType

@Composable
fun TimelineArea(
    tracks: List<EditTrack>,
    currentTimeMs: Long,
    totalDurationMs: Long,
    selectedClipId: String?,
    onSeek: (Long) -> Unit,
    onSelectClip: (String) -> Unit,
    onUpdateClipBoundaries: (String, Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // scale factor: pixels per millisecond
    var scale by remember { mutableFloatStateOf(0.05f) }
    var viewWidth by remember { mutableIntStateOf(0) }

    // Scroll state for the tracks
    val scrollState = rememberScrollState()

    // Sync seeker visually using scroll offset + center
    LaunchedEffect(scrollState.value, scale, viewWidth) {
        if (viewWidth > 0 && scrollState.isScrollInProgress) {
            val centerPixel = scrollState.value + viewWidth / 2f
            val timeMs = (centerPixel / scale).toLong()
            onSeek(timeMs.coerceIn(0L, totalDurationMs))
        }
    }

    // Sync scroll position when time changes externally (e.g. playing)
    LaunchedEffect(currentTimeMs, scale, viewWidth) {
        if (viewWidth > 0 && !scrollState.isScrollInProgress) {
            val targetPixel = (currentTimeMs * scale) - viewWidth / 2f
            scrollState.scrollTo(targetPixel.toInt().coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFF1E202B))
            .padding(top = 16.dp, bottom = 12.dp)
            .onSizeChanged { viewWidth = it.width }
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
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        // Constrain zoom to prevent extreme scaling
                        scale = (scale * zoom).coerceIn(0.01f, 1f)

                        // We do not handle pan here since horizontalScroll handles it natively,
                        // but you could add manual panning if desired.
                    }
                }
        ) {
            // Tracks area
            // We use padding on start and end so the timeline starts/ends at the center seeker
            val halfViewWidthDp = with(LocalDensity.current) { (viewWidth / 2f).toDp() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = if (viewWidth > 0) halfViewWidthDp else 0.dp)
            ) {
                tracks.forEach { track ->
                    TrackRow(track, scale, selectedClipId, onSelectClip, onUpdateClipBoundaries)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Central White Seeker
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .align(Alignment.Center)
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun TrackRow(
    track: EditTrack,
    scale: Float,
    selectedClipId: String?,
    onSelectClip: (String) -> Unit,
    onUpdateClipBoundaries: (String, Long, Long) -> Unit
) {
    val bgColor = when (track.type) {
        TrackType.VIDEO, TrackType.PIP -> Color(0xFF2E303C)
        TrackType.AUDIO -> Color(0xFF00B3FF).copy(alpha = 0.15f)
        TrackType.TEXT, TrackType.STICKER -> Color(0xFFE2A500).copy(alpha = 0.15f)
    }

    Row(
        modifier = Modifier
            .height(if (track.type == TrackType.VIDEO || track.type == TrackType.PIP) 64.dp else 36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
    ) {
        track.clips.forEach { clip ->
            val durationMs = clip.getTimelineDurationMs().coerceAtLeast(1L)
            val pixelWidth = durationMs * scale
            val dpWidth = with(LocalDensity.current) { pixelWidth.toDp() }
            val isSelected = clip.id == selectedClipId

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(dpWidth)
                    .background(if (isSelected) Color(0xFFFF2C55).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.3f))
                    .border(
                        if (isSelected) 2.dp else 0.5.dp,
                        if (isSelected) Color(0xFFFF2C55) else Color.Black.copy(alpha = 0.3f)
                    )
                    .clickable { onSelectClip(clip.id) }
            ) {
                if (isSelected && (track.type == TrackType.VIDEO || track.type == TrackType.PIP)) {
                    // Left handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(12.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFFF2C55))
                            .pointerInput(clip.id) {
                                detectTransformGestures { _, pan, _, _ ->
                                    val deltaMs = (pan.x / scale).toLong()
                                    if (deltaMs != 0L) {
                                        onUpdateClipBoundaries(clip.id, clip.startInSourceMs + deltaMs, clip.endInSourceMs)
                                    }
                                }
                            }
                    )
                    // Right handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(12.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFFF2C55))
                            .pointerInput(clip.id) {
                                detectTransformGestures { _, pan, _, _ ->
                                    val deltaMs = (pan.x / scale).toLong()
                                    if (deltaMs != 0L) {
                                        onUpdateClipBoundaries(clip.id, clip.startInSourceMs, clip.endInSourceMs + deltaMs)
                                    }
                                }
                            }
                    )
                }

                if ((track.type == TrackType.VIDEO || track.type == TrackType.PIP) && dpWidth > 40.dp) {
                    Text(
                        "Clip",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
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
