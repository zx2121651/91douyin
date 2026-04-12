package com.app.douyin.pro.feature.profile.domain.usecase

import com.app.douyin.pro.feature.profile.data.ProfileRepository
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.network.model.VideoDto
import javax.inject.Inject

class GetPublishedVideosUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Resource<List<VideoDto>> = repository.getPublishedVideos()
}
