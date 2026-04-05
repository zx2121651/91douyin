package com.app.douyin.pro.feature.edit.domain.usecase

import android.net.Uri
import com.app.douyin.pro.feature.edit.data.EditorRepository
import com.app.douyin.pro.lib.media.api.IVideoEditor
import com.app.douyin.pro.lib.media.model.EditingTimeline
import javax.inject.Inject

class ExportVideoUseCase @Inject constructor(
    private val repository: EditorRepository
) {
    operator fun invoke(timeline: EditingTimeline, outputPath: String, listener: IVideoEditor.ExportListener) {
        repository.export(timeline, outputPath, listener)
    }
}
