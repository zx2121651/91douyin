package com.app.douyin.pro.lib.media

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit test for NativeHardwareDecoder.
 * Note: Actual native method execution requires a device/emulator.
 * This test verifies class loading and basic signature visibility.
 */
class NativeHardwareDecoderTest {

    @Test
    fun testClassLoading() {
        try {
            val decoder = NativeHardwareDecoder()
            assertNotNull(decoder)
        } catch (e: UnsatisfiedLinkError) {
            // Expected when running on host without native library
            println("UnsatisfiedLinkError caught as expected on host: ${e.message}")
        }
    }

    @Test
    fun testLifecycleMethodsExist() {
        // This test mostly ensures the Kotlin code compiles and the class can be instantiated
        // (if the native library were present)
        val clazz = NativeHardwareDecoder::class.java
        val methods = clazz.declaredMethods
        val methodNames = methods.map { it.name }

        assertTrue(methodNames.contains("init"))
        assertTrue(methodNames.contains("configure"))
        assertTrue(methodNames.contains("render"))
        assertTrue(methodNames.contains("release"))
    }
}
