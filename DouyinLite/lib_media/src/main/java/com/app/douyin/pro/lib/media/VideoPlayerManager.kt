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
import android.util.Log
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.app.douyin.pro.lib.media.state.VideoPlayerState
import com.app.douyin.pro.lib.media.strategy.NetworkQuality
import com.app.douyin.pro.lib.media.strategy.PreloadStrategy
import com.app.douyin.pro.lib.media.util.NetworkMonitor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@OptIn(UnstableApi::class)
class VideoPlayerManager private constructor(private val context: Context) {

    private val TAG = "VideoPlayerManager"

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
    private val playerStates = mutableMapOf<String, MutableStateFlow<VideoPlayerState>>()
    private val playerListeners = mutableMapOf<String, Player.Listener>()
    private val preLoadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val preloadTasks = ConcurrentHashMap<String, Job>()
    private val preloadStrategy = PreloadStrategy()
    private val networkMonitor = NetworkMonitor(context)

    init {
        // Initialize pool
        for (i in 0 until playerPoolSize) {
            idlePlayers.add(createPlayer())
        }

        // Observe network quality
        preLoadScope.launch {
            networkMonitor.networkQuality.collect { quality ->
                updateNetworkQuality(quality)
            }
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
        activePlayers[url]?.let { return it }

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
                // Clean up state and listener for the recycled player
                playerStates.remove(oldestUrl)
                playerListeners.remove(oldestUrl)?.let { recycledPlayer.removeListener(it) }
                recycledPlayer
            } else {
                createPlayer() // Fallback
            }
        }

        activePlayers[url] = player
        val stateFlow = MutableStateFlow<VideoPlayerState>(VideoPlayerState.Idle)
        playerStates[url] = stateFlow

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val newState = when (playbackState) {
                    Player.STATE_IDLE -> VideoPlayerState.Idle
                    Player.STATE_BUFFERING -> VideoPlayerState.Buffering
                    Player.STATE_READY -> {
                        if (player.playWhenReady) VideoPlayerState.Playing else VideoPlayerState.Paused
                    }
                    Player.STATE_ENDED -> VideoPlayerState.Ended
                    else -> VideoPlayerState.Idle
                }
                updateState(url, newState)
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (player.playbackState == Player.STATE_READY) {
                    val newState = if (playWhenReady) VideoPlayerState.Playing else VideoPlayerState.Paused
                    updateState(url, newState)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                updateState(url, VideoPlayerState.Error(error.message, error.errorCode))
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                if (isLoading && player.playbackState == Player.STATE_BUFFERING) {
                    updateState(url, VideoPlayerState.Buffering)
                }
            }
        }

        player.addListener(listener)
        playerListeners[url] = listener

        // Prepare media
        updateState(url, VideoPlayerState.Preparing)
        Log.d(TAG, "Preparing player for: $url (potential cache use enabled)")
        val mediaSource = createMediaSource(url)
        player.setMediaSource(mediaSource)
        player.prepare()

        return player
    }

    private fun updateState(url: String, newState: VideoPlayerState) {
        val stateFlow = playerStates[url]
        if (stateFlow != null && stateFlow.value != newState) {
            Log.d(TAG, "Video [$url] state transition: ${stateFlow.value} -> $newState")
            stateFlow.value = newState
        }
    }

    fun getState(url: String): StateFlow<VideoPlayerState> {
        return playerStates[url]?.asStateFlow() ?: MutableStateFlow(VideoPlayerState.Idle).asStateFlow()
    }

    fun releasePlayer(url: String) {
        val player = activePlayers.remove(url)
        if (player != null) {
            player.stop()
            player.clearMediaItems()

            playerListeners.remove(url)?.let { player.removeListener(it) }
            playerStates.remove(url)

            if (idlePlayers.size < playerPoolSize) {
                idlePlayers.add(player)
            } else {
                player.release()
            }
        }
    }

    fun updateNetworkQuality(quality: NetworkQuality) {
        preloadStrategy.updateNetworkQuality(quality)
    }

    fun updatePreloadList(urls: List<String>) {
        val config = preloadStrategy.getConfig()
        val targetPreloadUrls = urls.take(config.preloadDepth)

        // Cancel tasks that are no longer in the preload list
        val iterator = preloadTasks.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!targetPreloadUrls.contains(entry.key) && !activePlayers.containsKey(entry.key)) {
                Log.d(TAG, "Cancelling preload for: ${entry.key}")
                entry.value.cancel()
                iterator.remove()
            }
        }

        // Start new preload tasks
        targetPreloadUrls.forEach { url ->
            if (!preloadTasks.containsKey(url) && !activePlayers.containsKey(url)) {
                preLoad(url, config.bufferSize)
            }
        }
    }

    private fun preLoad(url: String, bufferSize: Long) {
        if (bufferSize <= 0) return

        // Concurrent request de-duplication is already partially handled by the caller (updatePreloadList),
        // but we double check here to ensure we don't start duplicate jobs.
        if (preloadTasks.containsKey(url)) return

        val job = preLoadScope.launch {
            var cacheDataSource: androidx.media3.datasource.DataSource? = null
            try {
                Log.d(TAG, "Starting preload for: $url with size: $bufferSize")
                // Use the same CacheDataSourceFactory to ensure stable cache key logic is applied
                val dataSourceFactory = VideoCacheManager.getInstance(context).getCacheDataSourceFactory()
                cacheDataSource = dataSourceFactory.createDataSource()

                val dataSpec = DataSpec.Builder()
                    .setUri(url)
                    .setLength(bufferSize)
                    .build()

                val buffer = ByteArray(64 * 1024) // 64KB chunks
                var totalBytesRead = 0L

                cacheDataSource.open(dataSpec)
                while (totalBytesRead < bufferSize && isActive) {
                    val remaining = bufferSize - totalBytesRead
                    val toRead = if (remaining < buffer.size) remaining.toInt() else buffer.size
                    val read = cacheDataSource.read(buffer, 0, toRead)
                    if (read == -1) break
                    totalBytesRead += read
                }
                Log.d(TAG, "Completed preload for: $url, read: $totalBytesRead bytes")
            } catch (e: CancellationException) {
                Log.d(TAG, "Preload cancelled for: $url")
            } catch (e: IOException) {
                Log.e(TAG, "Preload failed for: $url (will retry on playback if needed)", e)
            } finally {
                try {
                    cacheDataSource?.close()
                } catch (e: Exception) {
                    // Ignore
                }
                preloadTasks.remove(url)
            }
        }
        preloadTasks[url] = job
    }

    private fun createMediaSource(url: String): MediaSource {
        val dataSourceFactory = VideoCacheManager.getInstance(context).getCacheDataSourceFactory()
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(url))
    }
}
