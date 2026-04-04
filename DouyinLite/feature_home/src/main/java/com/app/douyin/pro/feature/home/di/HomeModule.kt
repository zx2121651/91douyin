package com.app.douyin.pro.feature.home.di

import com.app.douyin.pro.feature.home.data.source.HomeDataSource
import com.app.douyin.pro.feature.home.data.source.MockHomeDataSource
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
    fun provideHomeDataSource(): HomeDataSource = MockHomeDataSource()
}
