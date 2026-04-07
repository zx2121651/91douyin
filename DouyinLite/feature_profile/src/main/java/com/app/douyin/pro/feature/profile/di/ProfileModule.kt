package com.app.douyin.pro.feature.profile.di

import com.app.douyin.pro.feature.profile.data.source.ProfileDataSource
import com.app.douyin.pro.feature.profile.data.source.RemoteProfileDataSource
import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.auth.AuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {
    @Provides
    @Singleton
    fun provideProfileDataSource(apiService: DouyinApiService, authManager: AuthManager): ProfileDataSource {
        return RemoteProfileDataSource(apiService, authManager)
    }
}
