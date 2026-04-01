package com.app.douyin.pro.feature.record.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLExt
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.EGLDisplay
import android.opengl.EGLSurface

class CameraRenderer(
    private val context: Context,
    private val onSurfaceTextureCreated: (SurfaceTexture) -> Unit
) : GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private var programId = -1
    private var oesTextureId = -1
    private var surfaceTexture: SurfaceTexture? = null

    private var aPositionHandle = -1
    private var aTextureCoordHandle = -1
    private var uMVPMatrixHandle = -1
    private var uSTMatrixHandle = -1

    private val mvpMatrix = FloatArray(16)
    private val stMatrix = FloatArray(16)

    @Volatile
    private var updateSurface = false

    private val vertexData = floatArrayOf(
        -1.0f, -1.0f, // Bottom Left
        1.0f, -1.0f,  // Bottom Right
        -1.0f, 1.0f,  // Top Left
        1.0f, 1.0f    // Top Right
    )

    private val textureData = floatArrayOf(
        0.0f, 0.0f, // Bottom Left
        1.0f, 0.0f, // Bottom Right
        0.0f, 1.0f, // Top Left
        1.0f, 1.0f  // Top Right
    )

    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData).apply { position(0) }

    private val textureBuffer: FloatBuffer = ByteBuffer.allocateDirect(textureData.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(textureData).apply { position(0) }

    // 录制相关的对象
    private var videoRecorder: VideoRecorder? = null
    private var isRecording = false
    private var outputPathToRecord: String? = null

    // EGL 相关
    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: android.opengl.EGLContext = EGL14.EGL_NO_CONTEXT
    private var windowSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var recordSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var viewWidth = 0
    private var viewHeight = 0
    private var eglConfig: android.opengl.EGLConfig? = null

    fun startRecording(outputPath: String) {
        if (!isRecording) {
            outputPathToRecord = outputPath
            isRecording = true
        }
    }

    fun stopRecording() {
        if (isRecording) {
            isRecording = false
            videoRecorder?.stop()
            if (recordSurface != EGL14.EGL_NO_SURFACE) {
                EGL14.eglMakeCurrent(eglDisplay, windowSurface, windowSurface, eglContext)
                EGL14.eglDestroySurface(eglDisplay, recordSurface)
                recordSurface = EGL14.EGL_NO_SURFACE
            }
            videoRecorder = null
            outputPathToRecord = null
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val vertexShaderSource = OpenGLUtils.readShaderFromAssets(context, "shaders/vertex_shader.glsl")
        val fragmentShaderSource = OpenGLUtils.readShaderFromAssets(context, "shaders/fragment_shader.glsl")

        programId = OpenGLUtils.createProgram(vertexShaderSource, fragmentShaderSource)
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition")
        aTextureCoordHandle = GLES20.glGetAttribLocation(programId, "aTextureCoord")
        uMVPMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        uSTMatrixHandle = GLES20.glGetUniformLocation(programId, "uSTMatrix")

        oesTextureId = OpenGLUtils.createOESTextureObject()
        surfaceTexture = SurfaceTexture(oesTextureId)
        surfaceTexture?.setOnFrameAvailableListener(this)

        // Callback to UI to bind CameraX
        onSurfaceTextureCreated(surfaceTexture!!)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        GLES20.glViewport(0, 0, width, height)
        Matrix.setIdentityM(mvpMatrix, 0)

        // 我们可以在这里或者绘制第一帧时捕获 EGL Context (需要在 GL 线程)
        eglDisplay = EGL14.eglGetCurrentDisplay()
        eglContext = EGL14.eglGetCurrentContext()
        windowSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW)

        // 提取 EGLConfig
        val configs = arrayOfNulls<android.opengl.EGLConfig>(1)
        val numConfigs = IntArray(1)
        val EGL_RECORDABLE_ANDROID = 0x3142
        val configAttribs = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_NONE
        )
        EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, 1, numConfigs, 0)
        eglConfig = configs[0]
    }

    override fun onDrawFrame(gl: GL10?) {
        if (updateSurface) {
            surfaceTexture?.updateTexImage()
            surfaceTexture?.getTransformMatrix(stMatrix)
            updateSurface = false
        }

        // 1. 渲染到屏幕 (GLSurfaceView 默认提供的 EGLSurface)
        drawFrameToCurrentSurface()

        // 2. 如果正在录制，则渲染到 VideoRecorder 的 Surface (需要通过 EGL14 建立 EGLSurface)
        if (isRecording) {
            if (videoRecorder == null && outputPathToRecord != null) {
                videoRecorder = VideoRecorder(outputPathToRecord!!)
                videoRecorder?.prepare(viewWidth, viewHeight)
                videoRecorder?.start()

                val recorderSurfaceObj = videoRecorder?.getInputSurface()
                if (recorderSurfaceObj != null && eglConfig != null) {
                    val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
                    recordSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, recorderSurfaceObj, surfaceAttribs, 0)
                }
            }

            if (recordSurface != EGL14.EGL_NO_SURFACE) {
                EGL14.eglMakeCurrent(eglDisplay, recordSurface, recordSurface, eglContext)

                // 设置视口并清屏
                GLES20.glViewport(0, 0, viewWidth, viewHeight)

                // 绘制一次
                drawFrameToCurrentSurface()

                // eglPresentationTimeANDROID 期望的是纳秒
                val timestampNs = surfaceTexture?.timestamp ?: System.nanoTime()
                EGLExt.eglPresentationTimeANDROID(eglDisplay, recordSurface, timestampNs)

                // 交换缓冲区，推入编码器
                EGL14.eglSwapBuffers(eglDisplay, recordSurface)

                // 恢复回屏幕的 EGL 环境，让 GLSurfaceView 也能正常工作 (GLSurfaceView会在onDrawFrame之后自动swap windowSurface)
                EGL14.eglMakeCurrent(eglDisplay, windowSurface, windowSurface, eglContext)
                GLES20.glViewport(0, 0, viewWidth, viewHeight) // 恢复视口
            }

            // 抽取编码器数据
            videoRecorder?.drainEncoder(false)
        }
    }

    private fun drawFrameToCurrentSurface() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(programId)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)

        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionHandle)

        textureBuffer.position(0)
        GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle)

        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uSTMatrixHandle, 1, false, stMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aTextureCoordHandle)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        updateSurface = true
    }
}
