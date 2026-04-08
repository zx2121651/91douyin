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
    fun getFilters(): List<FilterEffect>
    suspend fun publishVideo(videoFile: File, title: String)
}

class RemoteRecordDataSource @Inject constructor(
    private val apiService: DouyinApiService,
    private val authManager: AuthManager
) : RecordDataSource {
    override fun getFilters(): List<FilterEffect> {
        val staticFilters = listOf("原片", "黑白", "RGB色散", "二分屏").map {
            FilterEffect(name = it, isDynamic = false)
        }

        // Mock a dynamically downloaded shader from the server
        val dynamicWavyShader = """
            #version 310 es
            #extension GL_OES_EGL_image_external_essl3 : require
            precision mediump float;

            in vec2 vTextureCoord;
            uniform samplerExternalOES sTexture;

            out vec4 fragColor;

            void main() {
                vec2 uv = vTextureCoord;
                // Add a dynamic wave effect based on the y coordinate
                uv.x += sin(uv.y * 10.0) * 0.05;

                fragColor = texture(sTexture, uv);
            }
        """.trimIndent()

        val dynamicFilter = FilterEffect(
            name = "动态波浪 (云端)",
            isDynamic = true,
            glslSource = dynamicWavyShader
        )

        return staticFilters + dynamicFilter
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
