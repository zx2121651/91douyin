package com.app.douyin.pro.feature.home.domain.usecase

import com.app.douyin.pro.feature.home.data.HomeRepository
import com.app.douyin.pro.feature.home.domain.model.VideoPage
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject

class GetVideosUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(): Resource<VideoPage> = repository.getInitialVideos()
}
