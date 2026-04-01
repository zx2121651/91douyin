package com.app.douyin.pro.feature.record.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

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
        GLES20.glViewport(0, 0, width, height)
        Matrix.setIdentityM(mvpMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (updateSurface) {
            surfaceTexture?.updateTexImage()
            surfaceTexture?.getTransformMatrix(stMatrix)
            updateSurface = false
        }

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
