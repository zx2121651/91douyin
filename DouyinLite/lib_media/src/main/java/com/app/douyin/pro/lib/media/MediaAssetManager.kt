package com.app.douyin.pro.lib.media

import android.content.Context
import android.util.Log
import java.io.File
import java.util.UUID

/**
 * Manages the lifecycle of local media assets created during recording and editing.
 * Provides unified naming, directory structure, and cleanup mechanisms.
 */
open class MediaAssetManager(private val context: Context) {

    companion object {
        private const val TAG = "MediaAssetManager"
        private const val BASE_DIR = "media_assets"
        private const val DIR_SEGMENTS = "segments"
        private const val DIR_COVERS = "covers"
        private const val DIR_EXPORTS = "exports"

        // Files older than 24 hours are considered eligible for cleanup
        private const val CLEANUP_THRESHOLD_MS = 24 * 60 * 60 * 1000L
    }

    private val baseDir: File by lazy {
        File(context.cacheDir, BASE_DIR).apply {
            if (!exists()) mkdirs()
        }
    }

    private fun getDir(subDir: String): File {
        return File(baseDir, subDir).apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Generates a new path for a recorded video segment.
     */
    open fun getNewSegmentPath(): String {
        val fileName = "SEG_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.mp4"
        return File(getDir(DIR_SEGMENTS), fileName).absolutePath
    }

    /**
     * Generates a new path for a video cover/thumbnail.
     */
    fun getNewCoverPath(): String {
        val fileName = "COVER_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
        return File(getDir(DIR_COVERS), fileName).absolutePath
    }

    /**
     * Generates a new path for an exported video.
     */
    fun getNewExportPath(): String {
        val fileName = "EXPORT_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.mp4"
        return File(getDir(DIR_EXPORTS), fileName).absolutePath
    }

    /**
     * Deletes a specific file.
     */
    open fun deleteFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "Deleted file: $path")
                }
                deleted
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete file: $path", e)
            false
        }
    }

    /**
     * Cleans up all temporary files in the media_assets directory that are older than the threshold.
     */
    open fun cleanupAllTempFiles() {
        val now = System.currentTimeMillis()
        try {
            if (!baseDir.exists()) return

            baseDir.walkTopDown().forEach { file ->
                if (file.isFile && (now - file.lastModified() > CLEANUP_THRESHOLD_MS)) {
                    Log.d(TAG, "Cleaning up old temp file: ${file.absolutePath}")
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}
