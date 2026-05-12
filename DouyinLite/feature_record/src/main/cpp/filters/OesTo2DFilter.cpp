#include "OesTo2DFilter.h"
#include <cstring>

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

OesTo2DFilter::OesTo2DFilter(std::shared_ptr<rhi::RHIDevice> device) : BaseFilter(device) {
    initResources(OES_VERTEX_SHADER, OES_FRAGMENT_SHADER);

    // Initialize matrices to Identity
    float identity[16] = {
        1,0,0,0,
        0,1,0,0,
        0,0,1,0,
        0,0,0,1
    };
    std::memcpy(mvpMatrix, identity, sizeof(identity));
    std::memcpy(stMatrix, identity, sizeof(identity));
}

void OesTo2DFilter::SetMatrix(float* matrix) {
    std::memcpy(stMatrix, matrix, 16 * sizeof(float));
}

void OesTo2DFilter::Draw(std::shared_ptr<rhi::RHITexture> inputTexture) {
    if (!mProgram) return;

    mDevice->BindProgram(mProgram);

    // Set Matrix Uniforms
    mProgram->SetUniformMatrix4fv("uMVPMatrix", mvpMatrix);
    mProgram->SetUniformMatrix4fv("uSTMatrix", stMatrix);

    mDevice->BindTexture(0, inputTexture);
    mProgram->SetUniformInt("sTexture", 0);

    mProgram->BindVertexAttribute("aPosition", mVertexBuffer, 2, 0, 0);
    mProgram->BindVertexAttribute("aTextureCoord", mTexCoordBuffer, 2, 0, 0);

    mDevice->DrawArrays(rhi::PrimitiveTopology::TRIANGLE_STRIP, 0, 4);
}
