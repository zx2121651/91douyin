package com.app.douyin.pro.feature.record.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class CameraGLSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private lateinit var renderer: CameraRenderer
    var onSurfaceTextureReady: ((SurfaceTexture) -> Unit)? = null

    init {
        setEGLContextClientVersion(2)
    }

    fun initRenderer() {
        renderer = CameraRenderer(context) { st ->
            onSurfaceTextureReady?.invoke(st)
            // Trigger continuous rendering when frame arrives
            st.setOnFrameAvailableListener {
                requestRender()
            }
        }
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }
}
