package com.app.douyin.pro.lib.media

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

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
         // Simple preload: just cache the video, don't necessarily need a player yet.
         // VideoCacheManager will handle the downloading part when media source is created.
         // In a real scenario, we might use DownloadManager.
    }

    private fun createMediaSource(url: String): MediaSource {
        val dataSourceFactory = VideoCacheManager.getInstance(context).getCacheDataSourceFactory()
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(url))
    }
}
