package com.app.douyin.pro.lib.media.api

import android.net.Uri
import com.app.douyin.pro.lib.media.model.EditingTimeline

interface IVideoEditor {
    interface ExportListener {
        fun onProgress(progress: Int)
        fun onCompleted(outputUri: Uri)
        fun onError(exception: Exception)
    }

    fun exportTimeline(
        timeline: EditingTimeline,
        outputPath: String,
        listener: ExportListener
    )

    fun getProgress(): Int
    fun cancel()
}
