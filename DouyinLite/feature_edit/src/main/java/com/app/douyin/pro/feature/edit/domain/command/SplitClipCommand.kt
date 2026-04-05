package com.app.douyin.pro.feature.edit.domain.command

import com.app.douyin.pro.feature.edit.domain.model.EditTrack
import com.app.douyin.pro.feature.edit.domain.model.TrackType
import java.util.*

class SplitClipCommand(private val currentTimeMs: Long) : EditCommand {
    override fun execute(currentTracks: List<EditTrack>): List<EditTrack> {
        val newTracks = currentTracks.map { it.copy(clips = it.clips.toMutableList()) }
        val videoTrack = newTracks.find { it.type == TrackType.VIDEO } ?: return currentTracks
        val clips = videoTrack.clips

        var accumulatedTime = 0L
        var targetIdx = -1
        for (i in clips.indices) {
            val dur = clips[i].getTimelineDurationMs()
            if (currentTimeMs > accumulatedTime && currentTimeMs < accumulatedTime + dur) {
                targetIdx = i
                break
            }
            accumulatedTime += dur
        }

        if (targetIdx != -1) {
            val target = clips[targetIdx]
            val offset = (currentTimeMs - accumulatedTime) * target.speed
            val c1 = target.copy(id = UUID.randomUUID().toString(), endInSourceMs = target.startInSourceMs + offset.toLong())
            val c2 = target.copy(id = UUID.randomUUID().toString(), startInSourceMs = target.startInSourceMs + offset.toLong())
            clips.removeAt(targetIdx)
            clips.add(targetIdx, c1)
            clips.add(targetIdx + 1, c2)
            return newTracks
        }
        return currentTracks
    }

    override fun undo(previousTracks: List<EditTrack>): List<EditTrack> = previousTracks
}
