package com.app.douyin.pro.feature.record.gl

class NativeVideoEngine {
    companion object {
        init {
            System.loadLibrary("video_engine")
        }
    }

    private var nativeHandle: Long = 0

    external fun initEngine(width: Int, height: Int)

    external fun processFrame(oesTextureId: Int, matrix: FloatArray): Int

    external fun addFilter(filterId: Int)

    external fun release()
}
