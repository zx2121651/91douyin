package com.app.douyin.pro.feature.record.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class CameraRenderer(
    private val context: Context,
    private val onSurfaceTextureCreated: (SurfaceTexture) -> Unit
) : GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private var oesTextureId = -1
    private var surfaceTexture: SurfaceTexture? = null
    private val stMatrix = FloatArray(16)

    @Volatile
    private var updateSurface = false

    // Core engine from C++
    private val nativeEngine = NativeVideoEngine()

    // For rendering the final 2D texture to screen
    private var screenProgramId = -1
    private var screenVaoId = -1
    private var uScreenTextureHandle = -1

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

    // Dummy methods to keep compatibility with UI caller
    fun setFilter(filterName: String) {
        // Handle filter changes via nativeEngine.addFilter() in future
    }
    fun setDynamicFilter(glsl: String) {}

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        oesTextureId = OpenGLUtils.createOESTextureObject()
        surfaceTexture = SurfaceTexture(oesTextureId)
        surfaceTexture?.setOnFrameAvailableListener(this)

        // Pass surface to CameraX
        onSurfaceTextureCreated(surfaceTexture!!)

        setupScreenProgram()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        nativeEngine.initEngine(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (updateSurface) {
            surfaceTexture?.updateTexImage()
            surfaceTexture?.getTransformMatrix(stMatrix)
            updateSurface = false
        }

        // 1. Process via C++ Engine (OES -> Filter1 -> Filter2 -> FBO Output)
        val finalTextureId = nativeEngine.processFrame(oesTextureId, stMatrix)

        // 2. Render final FBO 2D Texture to Default Screen Framebuffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUseProgram(screenProgramId)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, finalTextureId)
        GLES30.glUniform1i(uScreenTextureHandle, 0)

        GLES30.glBindVertexArray(screenVaoId)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glBindVertexArray(0)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        updateSurface = true
    }

    fun release() {
        nativeEngine.release()
    }

    private fun setupScreenProgram() {
        val vShader = """
            attribute vec4 aPosition;
            attribute vec2 aTextureCoord;
            varying vec2 vTextureCoord;
            void main() {
                gl_Position = aPosition;
                vTextureCoord = aTextureCoord;
            }
        """.trimIndent()

        val fShader = """
            precision mediump float;
            varying vec2 vTextureCoord;
            uniform sampler2D sTexture;
            void main() {
                gl_FragColor = texture2D(sTexture, vTextureCoord);
            }
        """.trimIndent()

        screenProgramId = OpenGLUtils.createProgram(vShader, fShader)
        uScreenTextureHandle = GLES30.glGetUniformLocation(screenProgramId, "sTexture")

        // Create VAO and VBOs for screen rendering...
        val vaos = IntArray(1)
        GLES30.glGenVertexArrays(1, vaos, 0)
        screenVaoId = vaos[0]

        val vbos = IntArray(2)
        GLES30.glGenBuffers(2, vbos, 0)


        val vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData).apply { position(0) }
        val textureBuffer = ByteBuffer.allocateDirect(textureData.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(textureData).apply { position(0) }

        GLES30.glBindVertexArray(screenVaoId)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexData.size * 4, vertexBuffer, GLES30.GL_STATIC_DRAW)
        val aPositionHandle = GLES30.glGetAttribLocation(screenProgramId, "aPosition")
        GLES30.glEnableVertexAttribArray(aPositionHandle)
        GLES30.glVertexAttribPointer(aPositionHandle, 2, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[1])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, textureData.size * 4, textureBuffer, GLES30.GL_STATIC_DRAW)
        val aTextureCoordHandle = GLES30.glGetAttribLocation(screenProgramId, "aTextureCoord")
        GLES30.glEnableVertexAttribArray(aTextureCoordHandle)
        GLES30.glVertexAttribPointer(aTextureCoordHandle, 2, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindVertexArray(0)
    }
}
