package com.app.douyin.pro.feature.record.gl

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import androidx.camera.core.SurfaceOutput
import androidx.camera.core.SurfaceProcessor
import androidx.camera.core.SurfaceRequest
import java.util.concurrent.Executor

/**
 * A [SurfaceProcessor] that applies effects using OpenGL and [NativeVideoEngine].
 */
class CameraSurfaceProcessor : SurfaceProcessor, SurfaceTexture.OnFrameAvailableListener {
    private val glThread = HandlerThread("CameraSurfaceProcessor").apply { start() }
    private val glHandler = Handler(glThread.looper)
    private val glExecutor = Executor { command -> glHandler.post(command) }

    private var eglCore: EglCore? = null
    private var inputSurfaceTexture: SurfaceTexture? = null
    private var inputSurface: Surface? = null
    private var inputTextureId: Int = -1
    private val nativeEngine = NativeVideoEngine()

    private val outputSurfaces = mutableMapOf<SurfaceOutput, WindowSurface>()
    private val textureMatrix = FloatArray(16)

    init {
        glHandler.post {
            eglCore = EglCore(null, EglCore.FLAG_RECORDABLE)
        }
    }

    override fun onInputSurface(request: SurfaceRequest) {
        glHandler.post {
            inputTextureId = OpenGLUtils.createOESTextureObject()
            inputSurfaceTexture = SurfaceTexture(inputTextureId)
            inputSurfaceTexture?.setDefaultBufferSize(request.resolution.width, request.resolution.height)
            inputSurfaceTexture?.setOnFrameAvailableListener(this)
            inputSurface = Surface(inputSurfaceTexture)

            request.provideSurface(inputSurface!!, glExecutor) {
                releaseInput()
            }

            nativeEngine.initEngine(request.resolution.width, request.resolution.height)
        }
    }

    override fun onOutputSurface(surfaceOutput: SurfaceOutput) {
        glHandler.post {
            val windowSurface = WindowSurface(eglCore!!, surfaceOutput.getSurface(glExecutor) {
                // it is OutputSurfaceResult
                glHandler.post {
                    outputSurfaces.remove(surfaceOutput)?.release()
                }
            }, false)
            outputSurfaces[surfaceOutput] = windowSurface
        }
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        glHandler.post {
            if (eglCore == null || inputSurfaceTexture == null) return@post

            inputSurfaceTexture?.updateTexImage()
            inputSurfaceTexture?.getTransformMatrix(textureMatrix)

            val finalTextureId = nativeEngine.processFrame(inputTextureId, textureMatrix)

            outputSurfaces.forEach { (output, windowSurface) ->
                windowSurface.makeCurrent()

                val resolution = output.getSize()
                GLES30.glViewport(0, 0, resolution.width, resolution.height)
                GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
                GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

                // We should use the output.updateTransformMatrix() if needed,
                // but since we are doing a simple 1:1 blit of the processed texture
                // which already handled orientation via nativeEngine (if implemented there)
                // or we handle it here using a simple shader.

                // For now, let's assume we need a simple blit shader to render finalTextureId (2D) to output.
                renderTextureToSurface(finalTextureId)

                windowSurface.setPresentationTime(inputSurfaceTexture!!.timestamp)
                windowSurface.swapBuffers()
            }
        }
    }

    private var blitProgram = -1
    private var blitVao = -1

    private fun renderTextureToSurface(textureId: Int) {
        if (blitProgram == -1) {
            setupBlitProgram()
        }
        GLES30.glUseProgram(blitProgram)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glBindVertexArray(blitVao)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glBindVertexArray(0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }

    private fun setupBlitProgram() {
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

        blitProgram = OpenGLUtils.createProgram(vShader, fShader)

        val vaos = IntArray(1)
        GLES30.glGenVertexArrays(1, vaos, 0)
        blitVao = vaos[0]

        val vbos = IntArray(2)
        GLES30.glGenBuffers(2, vbos, 0)

        val vertexData = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)
        val texData = floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f, 1f, 1f)

        GLES30.glBindVertexArray(blitVao)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[0])
        val vBuf = java.nio.ByteBuffer.allocateDirect(vertexData.size * 4).order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData)
        vBuf.position(0)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexData.size * 4, vBuf, GLES30.GL_STATIC_DRAW)
        val posHandle = GLES30.glGetAttribLocation(blitProgram, "aPosition")
        GLES30.glEnableVertexAttribArray(posHandle)
        GLES30.glVertexAttribPointer(posHandle, 2, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[1])
        val tBuf = java.nio.ByteBuffer.allocateDirect(texData.size * 4).order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer().put(texData)
        tBuf.position(0)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, texData.size * 4, tBuf, GLES30.GL_STATIC_DRAW)
        val texHandle = GLES30.glGetAttribLocation(blitProgram, "aTextureCoord")
        GLES30.glEnableVertexAttribArray(texHandle)
        GLES30.glVertexAttribPointer(texHandle, 2, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glBindVertexArray(0)
    }

    fun release() {
        glHandler.post {
            outputSurfaces.values.forEach { it.release() }
            outputSurfaces.clear()
            releaseInput()
            nativeEngine.release()
            eglCore?.release()
            eglCore = null
            glThread.quitSafely()
        }
    }

    private fun releaseInput() {
        inputSurface?.release()
        inputSurface = null
        inputSurfaceTexture?.release()
        inputSurfaceTexture = null
        if (inputTextureId != -1) {
            GLES30.glDeleteTextures(1, intArrayOf(inputTextureId), 0)
            inputTextureId = -1
        }
    }

    fun setFilter(filterName: String) {
        // Map filter names to IDs if needed, or pass strings to native
        // For now, stub as per NativeVideoEngine capability
    }
}
