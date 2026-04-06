package com.app.douyin.pro.feature.home.domain.usecase

import com.app.douyin.pro.feature.home.data.HomeRepository
import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject

class LoadMoreVideosUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(page: Int): Resource<List<VideoModel>> = repository.loadMoreVideos(page)
}
