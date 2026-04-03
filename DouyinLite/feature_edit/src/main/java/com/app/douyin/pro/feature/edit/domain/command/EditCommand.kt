package com.app.douyin.pro.feature.edit.domain.command

import com.app.douyin.pro.feature.edit.domain.model.EditTrack

interface EditCommand {
    fun execute(currentTracks: List<EditTrack>): List<EditTrack>
    fun undo(previousTracks: List<EditTrack>): List<EditTrack>
}
