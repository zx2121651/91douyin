package com.app.douyin.pro.feature.record.gl

class NativeVideoEngine {
    companion object {
        init {
            System.loadLibrary("video_engine")
        }
    }

    external fun initEngine(width: Int, height: Int)

    external fun processFrame(oesTextureId: Int, matrix: FloatArray): Int

    external fun addFilter(filterId: Int)

    external fun setFilterWithRule(rule: String)

    external fun release()
}
