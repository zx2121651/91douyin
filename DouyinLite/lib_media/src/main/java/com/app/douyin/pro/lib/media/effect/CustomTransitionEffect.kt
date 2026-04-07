package com.app.douyin.pro.lib.media.effect

import android.content.Context
import androidx.media3.common.VideoFrameProcessingException
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BaseGlShaderProgram
import androidx.media3.effect.GlEffect
import androidx.media3.effect.GlShaderProgram
import androidx.media3.common.util.GlUtil
import androidx.media3.common.util.Size

@UnstableApi
class CustomTransitionEffect(
    private val durationMs: Long,
    private val isFadeOut: Boolean
) : GlEffect {
    override fun toGlShaderProgram(context: Context, useHdr: Boolean): GlShaderProgram {
        return TransitionShaderProgram(context, durationMs, isFadeOut)
    }
}

@UnstableApi
private class TransitionShaderProgram(
    context: Context,
    private val durationMs: Long,
    private val isFadeOut: Boolean
) : BaseGlShaderProgram(false, 1) {

    private val glProgram: androidx.media3.common.util.GlProgram
    private var startTimeUs: Long = -1L

    init {
        val vertexShader = """
            attribute vec4 aFramePosition;
            varying vec2 vTexCoords;
            void main() {
              gl_Position = aFramePosition;
              vTexCoords = (aFramePosition.xy + vec2(1.0, 1.0)) / 2.0;
            }
        """.trimIndent()

        val fragmentShader = """
            precision mediump float;
            uniform sampler2D uTexSampler;
            uniform float uProgress;
            varying vec2 vTexCoords;

            void main() {
              vec4 color = texture2D(uTexSampler, vTexCoords);
              float alpha = ${if (isFadeOut) "1.0 - uProgress" else "uProgress"};
              gl_FragColor = vec4(color.rgb * alpha, color.a);
            }
        """.trimIndent()

        glProgram = androidx.media3.common.util.GlProgram(context, vertexShader, fragmentShader)
        glProgram.setBufferAttribute("aFramePosition", GlUtil.getNormalizedCoordinateBounds(), GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE)
    }

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        return Size(inputWidth, inputHeight)
    }

    override fun drawFrame(inputTexId: Int, presentationTimeUs: Long) {
        if (startTimeUs == -1L) {
            startTimeUs = presentationTimeUs
        }

        val elapsedMs = (presentationTimeUs - startTimeUs) / 1000L
        val progress = (elapsedMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

        glProgram.use()
        glProgram.setSamplerTexIdUniform("uTexSampler", inputTexId, 0)
        glProgram.setFloatUniform("uProgress", progress)
        glProgram.bindAttributesAndUniforms()

        android.opengl.GLES20.glDrawArrays(android.opengl.GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GlUtil.checkGlError()
    }

    override fun release() {
        super.release()
        glProgram.delete()
    }
}
