package com.app.douyin.pro.feature.profile.domain.usecase

import com.app.douyin.pro.feature.profile.data.ProfileRepository
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.VideoModel
import javax.inject.Inject

class GetFavoriteVideosUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: Long? = null, latestTime: Long? = null): Resource<Pair<List<VideoModel>, Long>> {
        return repository.getFavoriteVideos(userId, latestTime)
    }
}
