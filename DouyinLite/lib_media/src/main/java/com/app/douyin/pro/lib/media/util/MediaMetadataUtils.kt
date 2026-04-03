package com.app.douyin.pro.lib.media.util

import android.content.Context
import android.net.Uri
import android.media.MediaMetadataRetriever

object MediaMetadataUtils {

    fun getVideoDurationMs(context: Context, uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration?.toLong() ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        } finally {
            retriever.release()
        }
    }
}
