package com.app.douyin.pro.feature.home.domain.usecase

import com.app.douyin.pro.feature.home.data.HomeRepository
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject

class FavoriteActionUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(videoId: Long, isFavorite: Boolean): Resource<Boolean> {
        val actionType = if (isFavorite) 1 else 2
        return repository.favoriteAction(videoId, actionType)
    }
}
