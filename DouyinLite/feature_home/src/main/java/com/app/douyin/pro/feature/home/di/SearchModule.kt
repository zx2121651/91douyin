package com.app.douyin.pro.feature.home.di

import com.app.douyin.pro.feature.home.data.SearchRepository
import com.app.douyin.pro.feature.home.data.source.SearchDataSource
import com.app.douyin.pro.lib.media.network.DouyinApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchModule {

    @Provides
    @Singleton
    fun provideSearchDataSource(apiService: DouyinApiService): SearchDataSource {
        return SearchDataSource(apiService)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(dataSource: SearchDataSource): SearchRepository {
        return SearchRepository(dataSource)
    }
}
