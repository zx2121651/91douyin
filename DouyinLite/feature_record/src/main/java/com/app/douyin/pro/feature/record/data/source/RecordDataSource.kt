package com.app.douyin.pro.feature.record.data.source

import com.app.douyin.pro.feature.record.domain.model.FilterEffect
import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.auth.AuthManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

interface RecordDataSource {
    suspend fun getFilters(): List<FilterEffect>
    suspend fun publishVideo(videoFile: File, title: String)
}

class RemoteRecordDataSource @Inject constructor(
    private val apiService: DouyinApiService,
    private val authManager: AuthManager
) : RecordDataSource {

    override suspend fun getFilters(): List<FilterEffect> {
        try {
            val response = apiService.getEffectList()
            if (response.statusCode == 0) {
                response.effectList?.let { list ->
                    return list.map { dto ->
                        FilterEffect(
                            name = dto.name,
                            isDynamic = dto.isDynamic,
                            glslSource = dto.glslSource
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback to basic if network fails
        return listOf("原片", "黑白", "RGB色散", "二分屏").map {
            FilterEffect(name = it, isDynamic = false)
        }
    }

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
