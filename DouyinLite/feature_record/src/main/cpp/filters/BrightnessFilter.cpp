#include "BrightnessFilter.h"

static const char* BRIGHTNESS_VERTEX_SHADER = R"(
    attribute vec4 aPosition;
    attribute vec2 aTextureCoord;
    varying vec2 vTextureCoord;
    void main() {
        gl_Position = aPosition;
        vTextureCoord = aTextureCoord;
    }
)";

static const char* BRIGHTNESS_FRAGMENT_SHADER = R"(
    precision mediump float;
    varying vec2 vTextureCoord;
    uniform sampler2D sTexture;
    uniform float uBrightness;
    void main() {
        vec4 color = texture2D(sTexture, vTextureCoord);
        gl_FragColor = vec4(color.rgb + uBrightness, color.a);
    }
)";

BrightnessFilter::BrightnessFilter(std::shared_ptr<rhi::RHIDevice> device) : BaseFilter(device) {
    initResources(BRIGHTNESS_VERTEX_SHADER, BRIGHTNESS_FRAGMENT_SHADER);
}

void BrightnessFilter::SetBrightness(float brightness) {
    mBrightness = brightness;
}

void BrightnessFilter::Draw(std::shared_ptr<rhi::RHITexture> inputTexture) {
    if (!mProgram) return;

    mDevice->BindProgram(mProgram);

    mProgram->SetUniformFloat("uBrightness", mBrightness);

    mDevice->BindTexture(0, inputTexture);
    mProgram->SetUniformInt("sTexture", 0);

    mProgram->BindVertexAttribute("aPosition", mVertexBuffer, 2, 0, 0);
    mProgram->BindVertexAttribute("aTextureCoord", mTexCoordBuffer, 2, 0, 0);

    mDevice->DrawArrays(rhi::PrimitiveTopology::TRIANGLE_STRIP, 0, 4);
}
