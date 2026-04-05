package com.app.douyin.pro.lib.media.di

import android.content.Context
import com.app.douyin.pro.lib.media.VideoPlayerManager
import com.app.douyin.pro.lib.media.VideoCacheManager
import com.app.douyin.pro.lib.media.VideoEditorHelper
import com.app.douyin.pro.lib.media.api.IVideoEditor
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

    @Provides
    @Singleton
    fun provideVideoEditor(@ApplicationContext context: Context): IVideoEditor {
        return VideoEditorHelper(context)
    }
}
