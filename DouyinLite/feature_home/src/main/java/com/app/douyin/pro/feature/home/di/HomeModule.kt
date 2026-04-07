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
    fun provideHomeDataSource(apiService: DouyinApiService): HomeDataSource {
        return RemoteHomeDataSource(apiService)
    }
}
