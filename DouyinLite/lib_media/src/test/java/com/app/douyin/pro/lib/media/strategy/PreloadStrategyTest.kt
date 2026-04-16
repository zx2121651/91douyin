package com.app.douyin.pro.lib.media.strategy

import org.junit.Assert.assertEquals
import org.junit.Test

class PreloadStrategyTest {

    @Test
    fun testWifiConfig() {
        val strategy = PreloadStrategy()
        strategy.updateNetworkQuality(NetworkQuality.WIFI)
        val config = strategy.getConfig()
        assertEquals(3, config.preloadDepth)
        assertEquals(1024 * 1024 * 2L, config.bufferSize)
    }

    @Test
    fun testWeakConfig() {
        val strategy = PreloadStrategy()
        strategy.updateNetworkQuality(NetworkQuality.WEAK)
        val config = strategy.getConfig()
        assertEquals(1, config.preloadDepth)
        assertEquals(512 * 1024L, config.bufferSize)
    }

    @Test
    fun testNoNetworkConfig() {
        val strategy = PreloadStrategy()
        strategy.updateNetworkQuality(NetworkQuality.NO_NETWORK)
        val config = strategy.getConfig()
        assertEquals(0, config.preloadDepth)
        assertEquals(0L, config.bufferSize)
    }
}
