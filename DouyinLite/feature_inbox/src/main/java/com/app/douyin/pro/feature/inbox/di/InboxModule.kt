package com.app.douyin.pro.feature.inbox.di

import com.app.douyin.pro.feature.inbox.data.source.InboxDataSource
import com.app.douyin.pro.feature.inbox.data.source.MockInboxDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InboxModule {
    @Provides
    @Singleton
    fun provideInboxDataSource(): InboxDataSource = MockInboxDataSource()
}
