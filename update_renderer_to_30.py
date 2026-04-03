import os

file_path = 'DouyinLite/feature_record/src/main/java/com/app/douyin/pro/feature/record/gl/CameraRenderer.kt'
with open(file_path, 'r') as f:
    content = f.read()

# 1. Update imports
content = content.replace('import android.opengl.GLES20', 'import android.opengl.GLES30')

# 2. Introduce VAO logic
# Add vaoId variable
content = content.replace('private var oesTextureId = -1', 'private var oesTextureId = -1\n    private var vaoId = -1')

# Update onSurfaceCreated
old_init = """        programId = OpenGLUtils.createProgram(vertexShaderSource, fragmentShaderSource)
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition")
        aTextureCoordHandle = GLES20.glGetAttribLocation(programId, "aTextureCoord")
        uMVPMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        uSTMatrixHandle = GLES20.glGetUniformLocation(programId, "uSTMatrix")"""

new_init = """        programId = OpenGLUtils.createProgram(vertexShaderSource, fragmentShaderSource)
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
        GLES30.glBindVertexArray(0)"""

content = content.replace(old_init, new_init)

# Update onDrawFrame to use VAO
old_draw = """        GLES20.glUseProgram(programId)

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
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)"""

new_draw = """        GLES30.glUseProgram(programId)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)

        GLES30.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES30.glUniformMatrix4fv(uSTMatrixHandle, 1, false, stMatrix, 0)

        GLES30.glBindVertexArray(vaoId)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glBindVertexArray(0)

        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)"""

# Global replacement of GLES20 with GLES30 for ClearColor etc
content = content.replace('GLES20', 'GLES30')
content = content.replace(old_draw.replace('GLES20', 'GLES30'), new_draw)

with open(file_path, 'w') as f:
    f.write(content)
