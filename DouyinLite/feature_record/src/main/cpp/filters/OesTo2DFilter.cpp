#include "OesTo2DFilter.h"
#include <GLES2/gl2ext.h>

static const char* OES_VERTEX_SHADER = R"(
    attribute vec4 aPosition;
    attribute vec4 aTextureCoord;
    uniform mat4 uMVPMatrix;
    uniform mat4 uSTMatrix;
    varying vec2 vTextureCoord;
    void main() {
        gl_Position = uMVPMatrix * aPosition;
        vTextureCoord = (uSTMatrix * aTextureCoord).xy;
    }
)";

static const char* OES_FRAGMENT_SHADER = R"(
    #extension GL_OES_EGL_image_external : require
    precision mediump float;
    varying vec2 vTextureCoord;
    uniform samplerExternalOES sTexture;
    void main() {
        gl_FragColor = texture2D(sTexture, vTextureCoord);
    }
)";

OesTo2DFilter::OesTo2DFilter() {
    mProgramId = createProgram(OES_VERTEX_SHADER, OES_FRAGMENT_SHADER);
    mPositionHandle = glGetAttribLocation(mProgramId, "aPosition");
    mTextureCoordHandle = glGetAttribLocation(mProgramId, "aTextureCoord");
    muMVPMatrixHandle = glGetUniformLocation(mProgramId, "uMVPMatrix");
    muSTMatrixHandle = glGetUniformLocation(mProgramId, "uSTMatrix");
    mTextureSamplerHandle = glGetUniformLocation(mProgramId, "sTexture");

    // Initialize matrices to Identity
    for(int i=0; i<16; i++) {
        mvpMatrix[i] = (i % 5 == 0) ? 1.0f : 0.0f;
    }
}

void OesTo2DFilter::SetMatrix(float* matrix) {
    for (int i = 0; i < 16; i++) {
        stMatrix[i] = matrix[i];
    }
}

void OesTo2DFilter::Draw(GLuint inputTextureId) {
    glUseProgram(mProgramId);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, inputTextureId); // Bind OES!
    glUniform1i(mTextureSamplerHandle, 0);

    glUniformMatrix4fv(muMVPMatrixHandle, 1, GL_FALSE, mvpMatrix);
    glUniformMatrix4fv(muSTMatrixHandle, 1, GL_FALSE, stMatrix);

    glVertexAttribPointer(mPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, VERTICES);
    glEnableVertexAttribArray(mPositionHandle);

    glVertexAttribPointer(mTextureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0, TEX_COORDS);
    glEnableVertexAttribArray(mTextureCoordHandle);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glDisableVertexAttribArray(mPositionHandle);
    glDisableVertexAttribArray(mTextureCoordHandle);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
}
