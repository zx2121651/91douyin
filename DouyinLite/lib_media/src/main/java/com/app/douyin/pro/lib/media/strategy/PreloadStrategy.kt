package com.app.douyin.pro.lib.media.strategy

class PreloadStrategy {
    private var currentQuality: NetworkQuality = NetworkQuality.WIFI

    fun updateNetworkQuality(quality: NetworkQuality) {
        if (currentQuality != quality) {
            currentQuality = quality
        }
    }

    fun getConfig(): PreloadConfig {
        return when (currentQuality) {
            NetworkQuality.WIFI -> PreloadConfig.WIFI_CONFIG
            NetworkQuality.GOOD -> PreloadConfig.GOOD_CONFIG
            NetworkQuality.WEAK -> PreloadConfig.WEAK_CONFIG
            NetworkQuality.NO_NETWORK -> PreloadConfig.NONE_CONFIG
        }
    }
}
