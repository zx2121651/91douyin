package com.app.douyin.pro.lib.media.state

sealed class VideoPlayerState {
    object Idle : VideoPlayerState()
    object Preparing : VideoPlayerState()
    object Ready : VideoPlayerState()
    object Playing : VideoPlayerState()
    object Paused : VideoPlayerState()
    object Buffering : VideoPlayerState()
    object Ended : VideoPlayerState()
    data class Error(val message: String?, val errorCode: Int? = null) : VideoPlayerState()

    override fun toString(): String {
        return when (this) {
            is Idle -> "Idle"
            is Preparing -> "Preparing"
            is Ready -> "Ready"
            is Playing -> "Playing"
            is Paused -> "Paused"
            is Buffering -> "Buffering"
            is Ended -> "Ended"
            is Error -> "Error(message=$message, errorCode=$errorCode)"
        }
    }
}
