package com.app.douyin.pro.feature.edit.domain.command

import com.app.douyin.pro.feature.edit.domain.model.EditTrack

class DeleteClipCommand(private val clipId: String) : EditCommand {
    override fun execute(currentTracks: List<EditTrack>): List<EditTrack> {
        val newTracks = currentTracks.map { it.copy(clips = it.clips.toMutableList()) }
        newTracks.forEach { it.clips.removeIf { c -> c.id == clipId } }
        return newTracks
    }

    override fun undo(previousTracks: List<EditTrack>): List<EditTrack> = previousTracks
}
