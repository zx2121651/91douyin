package com.app.douyin.pro.feature.record.di

import com.app.douyin.pro.feature.record.data.source.RecordDataSource
import com.app.douyin.pro.feature.record.data.source.RemoteRecordDataSource
import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.auth.AuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecordModule {
    @Provides
    @Singleton
    fun provideRecordDataSource(apiService: DouyinApiService, authManager: AuthManager): RecordDataSource {
        return RemoteRecordDataSource(apiService, authManager)
    }
}
