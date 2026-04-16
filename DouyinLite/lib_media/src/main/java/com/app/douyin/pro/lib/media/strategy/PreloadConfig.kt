package com.app.douyin.pro.lib.media.strategy

data class PreloadConfig(
    val preloadDepth: Int,
    val bufferSize: Long,
    val maxConcurrentTasks: Int = 3
) {
    companion object {
        val WIFI_CONFIG = PreloadConfig(
            preloadDepth = 3,
            bufferSize = 1024 * 1024 * 2, // 2MB
            maxConcurrentTasks = 3
        )

        val GOOD_CONFIG = PreloadConfig(
            preloadDepth = 2,
            bufferSize = 1024 * 1024 * 1, // 1MB
            maxConcurrentTasks = 2
        )

        val WEAK_CONFIG = PreloadConfig(
            preloadDepth = 1,
            bufferSize = 512 * 1024, // 512KB
            maxConcurrentTasks = 1
        )

        val NONE_CONFIG = PreloadConfig(
            preloadDepth = 0,
            bufferSize = 0,
            maxConcurrentTasks = 0
        )
    }
}
