package com.app.douyin.pro.lib.media.di

import com.app.douyin.pro.lib.media.network.ApiClient
import com.app.douyin.pro.lib.media.network.DouyinApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideDouyinApiService(): DouyinApiService {
        return ApiClient.retrofit.create(DouyinApiService::class.java)
    }
}
