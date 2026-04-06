package com.app.douyin.pro.feature.record.data.source

import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.auth.AuthManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

interface RecordDataSource {
    fun getFilters(): List<String>
    suspend fun publishVideo(videoFile: File, title: String)
}

class RemoteRecordDataSource @Inject constructor(
    private val apiService: DouyinApiService,
    private val authManager: AuthManager
) : RecordDataSource {
    override fun getFilters(): List<String> = listOf("原片", "冷白", "柔光", "复古", "黑白", "清新", "夏日")

    override suspend fun publishVideo(videoFile: File, title: String) {
        val token = authManager.getToken() ?: throw Exception("Not logged in")

        val tokenBody = token.toRequestBody("text/plain".toMediaTypeOrNull())
        val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFile = videoFile.asRequestBody("video/mp4".toMediaTypeOrNull())
        val videoPart = MultipartBody.Part.createFormData("data", videoFile.name, requestFile)

        val response = apiService.publishVideo(tokenBody, titleBody, videoPart)
        if (response.statusCode != 0) {
            throw Exception(response.statusMsg ?: "Publish failed")
        }
    }
}
