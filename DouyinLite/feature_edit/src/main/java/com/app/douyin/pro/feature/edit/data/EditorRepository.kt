package com.app.douyin.pro.feature.edit.data

import android.content.Context
import android.net.Uri
import com.app.douyin.pro.lib.media.VideoEditorHelper
import com.app.douyin.pro.lib.media.model.EditingTimeline
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditorRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val editorHelper = VideoEditorHelper(context)

    fun export(timeline: EditingTimeline, outputPath: String, listener: VideoEditorHelper.ExportListener) {
        editorHelper.exportTimeline(timeline, outputPath, listener)
    }
}
