package com.app.douyin.pro.feature.profile.domain.usecase

import com.app.douyin.pro.feature.profile.data.ProfileRepository
import com.app.douyin.pro.feature.profile.data.source.ProfileInfo
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject

class GetProfileInfoUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Resource<ProfileInfo> = repository.getProfileInfo()
}
