package com.app.douyin.pro.lib.media.interaction

import com.app.douyin.pro.lib.media.network.DouyinApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class InteractionEvent {
    data class LikeChanged(val videoId: Long, val isLiked: Boolean, val newLikeCount: Long) : InteractionEvent()
    data class FollowChanged(val authorId: Long, val isFollowed: Boolean) : InteractionEvent()
    data class CommentAdded(val videoId: Long, val newCommentCount: Long) : InteractionEvent()
}

@Singleton
class VideoInteractionManager @Inject constructor(
    private val apiService: DouyinApiService
) {
    private val _interactionEvents = MutableSharedFlow<InteractionEvent>(extraBufferCapacity = 64)
    val interactionEvents = _interactionEvents.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun toggleLike(videoId: Long, currentIsLiked: Boolean, currentLikeCount: Long) {
        val newIsLiked = !currentIsLiked
        val newLikeCount = if (newIsLiked) currentLikeCount + 1 else (currentLikeCount - 1).coerceAtLeast(0)

        // Optimistic update
        scope.launch {
            _interactionEvents.emit(InteractionEvent.LikeChanged(videoId, newIsLiked, newLikeCount))
        }

        // Backend call
        scope.launch {
            try {
                val actionType = if (newIsLiked) 1 else 2
                val response = apiService.favoriteAction(videoId, actionType)
                if (response.statusCode != 0) {
                    // Rollback
                    _interactionEvents.emit(InteractionEvent.LikeChanged(videoId, currentIsLiked, currentLikeCount))
                }
            } catch (e: Exception) {
                // Rollback
                _interactionEvents.emit(InteractionEvent.LikeChanged(videoId, currentIsLiked, currentLikeCount))
            }
        }
    }

    fun toggleFollow(authorId: Long, currentIsFollowed: Boolean) {
        val newIsFollowed = !currentIsFollowed

        // Optimistic update
        scope.launch {
            _interactionEvents.emit(InteractionEvent.FollowChanged(authorId, newIsFollowed))
        }

        // Backend call
        scope.launch {
            try {
                val actionType = if (newIsFollowed) 1 else 2
                val response = apiService.relationAction(authorId, actionType)
                if (response.statusCode != 0) {
                    // Rollback
                    _interactionEvents.emit(InteractionEvent.FollowChanged(authorId, currentIsFollowed))
                }
            } catch (e: Exception) {
                // Rollback
                _interactionEvents.emit(InteractionEvent.FollowChanged(authorId, currentIsFollowed))
            }
        }
    }

    fun notifyCommentAdded(videoId: Long, currentCommentCount: Long) {
        scope.launch {
            _interactionEvents.emit(InteractionEvent.CommentAdded(videoId, currentCommentCount + 1))
        }
    }
}
