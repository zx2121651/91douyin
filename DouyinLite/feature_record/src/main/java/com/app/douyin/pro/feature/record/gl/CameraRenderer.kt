package com.app.douyin.pro.feature.record.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES30
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
    private var vaoId = -1
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
        uMVPMatrixHandle = GLES30.glGetUniformLocation(programId, "uMVPMatrix")
        uSTMatrixHandle = GLES30.glGetUniformLocation(programId, "uSTMatrix")

        // Create VAO
        val vaos = IntArray(1)
        GLES30.glGenVertexArrays(1, vaos, 0)
        vaoId = vaos[0]

        // Create VBOs
        val vbos = IntArray(2)
        GLES30.glGenBuffers(2, vbos, 0)

        GLES30.glBindVertexArray(vaoId)

        // Position Buffer
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexData.size * 4, vertexBuffer, GLES30.GL_STATIC_DRAW)
        GLES30.glEnableVertexAttribArray(0) // layout location 0
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 0, 0)

        // Texture Buffer
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[1])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, textureData.size * 4, textureBuffer, GLES30.GL_STATIC_DRAW)
        GLES30.glEnableVertexAttribArray(1) // layout location 1
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindVertexArray(0)

        oesTextureId = OpenGLUtils.createOESTextureObject()
        surfaceTexture = SurfaceTexture(oesTextureId)
        surfaceTexture?.setOnFrameAvailableListener(this)

        // Callback to UI to bind CameraX
        onSurfaceTextureCreated(surfaceTexture!!)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        Matrix.setIdentityM(mvpMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        if (updateSurface) {
            surfaceTexture?.updateTexImage()
            surfaceTexture?.getTransformMatrix(stMatrix)
            updateSurface = false
        }

        GLES30.glUseProgram(programId)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)

        GLES30.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES30.glUniformMatrix4fv(uSTMatrixHandle, 1, false, stMatrix, 0)

        GLES30.glBindVertexArray(vaoId)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glBindVertexArray(0)

        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        updateSurface = true
    }
}
