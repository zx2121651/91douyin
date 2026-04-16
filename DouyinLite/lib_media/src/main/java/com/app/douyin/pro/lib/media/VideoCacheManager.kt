package com.app.douyin.pro.lib.media

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import android.util.Log
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheKeyFactory
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@OptIn(UnstableApi::class)
class VideoCacheManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: VideoCacheManager? = null

        fun getInstance(context: Context): VideoCacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VideoCacheManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val MAX_CACHE_SIZE: Long = 500 * 1024 * 1024 // 500MB
    }

    private val cache: SimpleCache
    private val cacheDataSourceFactory: DataSource.Factory

    init {
        val cacheDir = File(context.cacheDir, "media_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE)
        val databaseProvider = StandaloneDatabaseProvider(context)

        cache = SimpleCache(cacheDir, evictor, databaseProvider)

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        val defaultDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val cacheKeyFactory = CacheKeyFactory { dataSpec ->
            dataSpec.key ?: dataSpec.uri.buildUpon().clearQuery().build().toString()
        }

        val eventListener = object : CacheDataSource.EventListener {
            override fun onCachedBytesRead(cacheSize: Long, cachedBytesRead: Long) {
                if (cachedBytesRead > 0) {
                    Log.v("VideoCache", "Cache Hit: read $cachedBytesRead bytes from cache.")
                }
            }

            override fun onCacheIgnored(reason: Int) {
                Log.w("VideoCache", "Cache Ignored: reason=$reason")
            }
        }

        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
            .setCacheKeyFactory(cacheKeyFactory)
            .setEventListener(eventListener)
            // 边播边存，且缓存出错时不影响正常播放
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun getCacheDataSourceFactory(): DataSource.Factory {
        return cacheDataSourceFactory
    }

    fun release() {
        cache.release()
    }
}
