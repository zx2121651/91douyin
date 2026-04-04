package com.app.douyin.pro.feature.mall.di

import com.app.douyin.pro.feature.mall.data.source.MallDataSource
import com.app.douyin.pro.feature.mall.data.source.MockMallDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MallModule {
    @Provides
    @Singleton
    fun provideMallDataSource(): MallDataSource = MockMallDataSource()
}
