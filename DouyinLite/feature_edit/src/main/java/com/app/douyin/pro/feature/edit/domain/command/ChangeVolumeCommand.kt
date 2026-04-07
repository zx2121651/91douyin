package com.app.douyin.pro.feature.edit.domain.command

import com.app.douyin.pro.feature.edit.domain.model.EditTrack
import com.app.douyin.pro.feature.edit.domain.model.TrackType

class ChangeVolumeCommand(
    private val clipId: String,
    private val newVolume: Float
) : EditCommand {
    override fun execute(tracks: List<EditTrack>): List<EditTrack> {
        return tracks.map { track ->
            if (track.type == TrackType.VIDEO) {
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == clipId) {
                        clip.copy(volume = newVolume)
                    } else {
                        clip
                    }
                }.toMutableList())
            } else {
                track
            }
        }
    }

    override fun undo(previousTracks: List<EditTrack>): List<EditTrack> {
        return previousTracks
    }
}
