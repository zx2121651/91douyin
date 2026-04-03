package com.app.douyin.pro.lib.media.di

import android.content.Context
import com.app.douyin.pro.lib.media.VideoPlayerManager
import com.app.douyin.pro.lib.media.VideoCacheManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideVideoPlayerManager(@ApplicationContext context: Context): VideoPlayerManager {
        return VideoPlayerManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideVideoCacheManager(@ApplicationContext context: Context): VideoCacheManager {
        return VideoCacheManager.getInstance(context)
    }
}
