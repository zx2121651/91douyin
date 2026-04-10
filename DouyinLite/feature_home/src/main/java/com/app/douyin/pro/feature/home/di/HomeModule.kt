package com.app.douyin.pro.feature.home.di

import com.app.douyin.pro.feature.home.data.source.HomeDataSource
import com.app.douyin.pro.feature.home.data.source.RemoteHomeDataSource
import com.app.douyin.pro.lib.media.network.DouyinApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {
    @Provides
    @Singleton
    fun provideCommentDataSource(
        apiService: com.app.douyin.pro.lib.media.network.DouyinApiService,
        authManager: com.app.douyin.pro.lib.media.auth.AuthManager
    ): com.app.douyin.pro.feature.home.data.source.CommentDataSource {
        return com.app.douyin.pro.feature.home.data.source.RemoteCommentDataSource(apiService, authManager)
    }

    @Provides
    @Singleton
    fun provideHomeDataSource(apiService: DouyinApiService): HomeDataSource {
        return RemoteHomeDataSource(apiService)
    }
}
