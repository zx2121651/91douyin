package com.app.douyin.pro.lib.media

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

@OptIn(UnstableApi::class)
class VideoPlayerManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: VideoPlayerManager? = null

        fun getInstance(context: Context): VideoPlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VideoPlayerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val playerPoolSize = 3
    private val idlePlayers = mutableListOf<ExoPlayer>()
    private val activePlayers = mutableMapOf<String, ExoPlayer>()
    private val preLoadScope = CoroutineScope(Dispatchers.IO)

    init {
        // Initialize pool
        for (i in 0 until playerPoolSize) {
            idlePlayers.add(createPlayer())
        }
    }

    private fun createPlayer(): ExoPlayer {
        return ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = false
        }
    }

    fun getPlayer(url: String): ExoPlayer {
        // If already active, return it
        if (activePlayers.containsKey(url)) {
            return activePlayers[url]!!
        }

        // Get from idle pool or create new if empty
        val player = if (idlePlayers.isNotEmpty()) {
            idlePlayers.removeAt(0)
        } else {
            // Pool exhausted, recycle the oldest active player (LRU logic could be better, simple for now)
            val oldestUrl = activePlayers.keys.firstOrNull()
            if (oldestUrl != null) {
                val recycledPlayer = activePlayers.remove(oldestUrl)!!
                recycledPlayer.stop()
                recycledPlayer.clearMediaItems()
                recycledPlayer
            } else {
                createPlayer() // Fallback
            }
        }

        activePlayers[url] = player

        // Prepare media
        val mediaSource = createMediaSource(url)
        player.setMediaSource(mediaSource)
        player.prepare()

        return player
    }

    fun releasePlayer(url: String) {
        val player = activePlayers.remove(url)
        if (player != null) {
            player.stop()
            player.clearMediaItems()
            if (idlePlayers.size < playerPoolSize) {
                idlePlayers.add(player)
            } else {
                player.release()
            }
        }
    }

    fun preLoad(url: String) {
        // Execute preload in a background coroutine
        preLoadScope.launch {
            var cacheDataSource: androidx.media3.datasource.DataSource? = null
            try {
                val dataSpec = DataSpec.Builder().setUri(url).build()
                cacheDataSource = VideoCacheManager.getInstance(context).getCacheDataSourceFactory().createDataSource()
                // Download the first 1MB of the video
                val buffer = ByteArray(1024 * 1024)
                var bytesRead = 0
                val lengthToRead = buffer.size

                cacheDataSource.open(dataSpec)
                while (bytesRead < lengthToRead) {
                    val read = cacheDataSource.read(buffer, bytesRead, lengthToRead - bytesRead)
                    if (read == -1) break
                    bytesRead += read
                }

            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    cacheDataSource?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
    }

    private fun createMediaSource(url: String): MediaSource {
        val dataSourceFactory = VideoCacheManager.getInstance(context).getCacheDataSourceFactory()
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(url))
    }
}
