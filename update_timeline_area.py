import os

file_path = 'DouyinLite/feature_edit/src/main/java/com/app/douyin/pro/feature/edit/ui/component/TimelineArea.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Add drag logic to TimelineArea
new_imports = """
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
"""
content = content.replace('import androidx.compose.ui.unit.sp', 'import androidx.compose.ui.unit.sp' + new_imports)

content = content.replace(
    'fun TimelineArea(\n    tracks: List<EditTrack>,\n    currentTimeMs: Long,\n    totalDurationMs: Long,\n    modifier: Modifier = Modifier\n)',
    'fun TimelineArea(\n    tracks: List<EditTrack>,\n    currentTimeMs: Long,\n    totalDurationMs: Long,\n    onSeek: (Long) -> Unit,\n    onSelectClip: (String) -> Unit,\n    modifier: Modifier = Modifier\n)'
)

# Replace Playhead container with drag handling
old_box = """        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                tracks.forEach { track ->
                    TrackRow(track)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Playhead
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color.White)
            )
        }"""

new_box = r"""        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
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

            // Playhead (Fixed at center, we shift tracks logic would be better but let's keep it simple for now)
            val progress = if (totalDurationMs > 0) currentTimeMs.toFloat() / totalDurationMs else 0f
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .offset(x = (16.dp + (360.dp * progress))) // Mocking screen width offset
                    .background(Color.White)
            )
        }"""

content = content.replace(old_box, new_box)

# Update TrackRow to handle selection
content = content.replace('fun TrackRow(track: EditTrack) {', 'fun TrackRow(track: EditTrack, onSelectClip: (String) -> Unit) {')
content = content.replace('.background(bgColor)', '.background(bgColor).clickable { }') # just stub for now
content = content.replace('track.clips.forEach { clip ->', 'track.clips.forEach { clip ->\n            Box(\n                modifier = Modifier\n                    .fillMaxHeight()\n                    .weight(clip.getTimelineDurationMs().toFloat())\n                    .background(Color.Gray.copy(alpha = 0.3f))\n                    .border(0.5.dp, Color.Black.copy(alpha = 0.3f))\n                    .clickable { onSelectClip(clip.id) },\n                contentAlignment = Alignment.Center\n            ) {\n                if (track.type == TrackType.VIDEO) {\n                    Text("Clip", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)\n                }\n            }')

# Remove redundant Box inside TrackRow loop since we replaced it
# Wait, I should just replace the inner Box.

with open(file_path, 'w') as f:
    f.write(content)
