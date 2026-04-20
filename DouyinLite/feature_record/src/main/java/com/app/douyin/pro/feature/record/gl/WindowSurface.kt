package com.app.douyin.pro.feature.record.gl

import android.opengl.EGL14
import android.opengl.EGLSurface

/**
 * Recordable EGL surface associated with a window.
 */
class WindowSurface(
    private val eglCore: EglCore,
    private val surface: Any,
    private val releaseSurface: Boolean = false
) {
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE

    init {
        eglSurface = eglCore.createWindowSurface(surface)
    }

    fun release() {
        eglCore.destroySurface(eglSurface)
        eglSurface = EGL14.EGL_NO_SURFACE
        if (releaseSurface) {
            if (surface is android.view.Surface) {
                surface.release()
            }
        }
    }

    fun makeCurrent() {
        eglCore.makeCurrent(eglSurface)
    }

    fun swapBuffers(): Boolean {
        return eglCore.swapBuffers(eglSurface)
    }

    fun setPresentationTime(nsecs: Long) {
        eglCore.setPresentationTime(eglSurface, nsecs)
    }
}
