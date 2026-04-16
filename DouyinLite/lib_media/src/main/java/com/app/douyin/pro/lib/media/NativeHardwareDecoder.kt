package com.app.douyin.pro.lib.media

import android.view.Surface

/**
 * Kotlin interface for the native hardware decoder.
 * Manages the lifecycle of a native HardwareDecoder instance.
 */
class NativeHardwareDecoder {
    // Pointer to the native HardwareDecoder object
    private var nativePtr: Long = 0

    companion object {
        init {
            System.loadLibrary("douyin_core")
        }
    }

    /**
     * Creates and initializes the native decoder instance.
     */
    fun init(): Boolean {
        if (nativePtr != 0L) return true
        nativePtr = nativeCreate()
        return if (nativePtr != 0L) {
            nativeInit(nativePtr)
        } else {
            false
        }
    }

    /**
     * Configures the decoder with the given parameters and target surface.
     */
    fun configure(mimeType: String, width: Int, height: Int, surface: Surface?): Boolean {
        if (nativePtr == 0L) return false
        return nativeConfigure(nativePtr, mimeType, width, height, surface)
    }

    /**
     * Triggers rendering of the next available output buffer to the surface.
     */
    fun render(timeoutUs: Int) {
        if (nativePtr != 0L) {
            nativeRender(nativePtr, timeoutUs)
        }
    }

    /**
     * Releases the native decoder and its resources.
     */
    fun release() {
        if (nativePtr != 0L) {
            nativeRelease(nativePtr)
            nativePtr = 0
        }
    }

    protected fun finalize() {
        release()
    }

    private external fun nativeCreate(): Long
    private external fun nativeInit(ptr: Long): Boolean
    private external fun nativeConfigure(ptr: Long, mimeType: String, width: Int, height: Int, surface: Surface?): Boolean
    private external fun nativeRender(ptr: Long, timeoutUs: Int)
    private external fun nativeRelease(ptr: Long)
}
