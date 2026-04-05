package com.app.douyin.pro.feature.edit.data

import android.content.Context
import com.app.douyin.pro.lib.media.api.IVideoEditor
import com.app.douyin.pro.lib.media.model.EditingTimeline
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditorRepository @Inject constructor(
    private val videoEditor: IVideoEditor
) {
    fun export(timeline: EditingTimeline, outputPath: String, listener: IVideoEditor.ExportListener) {
        videoEditor.exportTimeline(timeline, outputPath, listener)
    }

    fun getEffectList(): Resource<List<String>> = Resource.Success(listOf("Beauty", "Filter", "Sticker"))
}
