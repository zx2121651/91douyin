package com.app.douyin.pro.feature.home.domain.usecase

import com.app.douyin.pro.feature.home.data.SearchRepository
import com.app.douyin.pro.lib.media.model.UserModel
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(keyword: String, cursor: Long): Resource<Pair<List<UserModel>, Long>> {
        return repository.searchUsers(keyword, cursor)
    }
}
