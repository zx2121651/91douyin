package com.app.douyin.pro.feature.profile.data

import com.app.douyin.pro.feature.profile.data.source.ProfileDataSource
import com.app.douyin.pro.feature.profile.data.source.ProfileInfo
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.AppError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val dataSource: ProfileDataSource
) {
    fun getProfileInfo(): Resource<ProfileInfo> = try {
        Resource.Success(dataSource.getUserInfo())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.UnknownError)
    }
}
