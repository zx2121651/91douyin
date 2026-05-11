#include "LUTFilter.h"
#include <android/log.h>

static const char* LUT_VERTEX_SHADER = R"(
    attribute vec4 aPosition;
    attribute vec2 aTextureCoord;
    varying vec2 vTextureCoord;
    void main() {
        gl_Position = aPosition;
        vTextureCoord = aTextureCoord;
    }
)";

// Standard 512x512 LUT shader.
// It maps colors to a 64x64 grid layout inside a 512x512 texture.
static const char* LUT_FRAGMENT_SHADER = R"(
    precision mediump float;
    varying highp vec2 vTextureCoord;

    uniform sampler2D sTexture;      // Input image
    uniform sampler2D sLUTTexture;   // 512x512 LUT
    uniform float uIntensity;        // Effect strength [0, 1]

    void main() {
        vec4 textureColor = texture2D(sTexture, vTextureCoord);

        float blueColor = textureColor.b * 63.0;

        vec2 quad1;
        quad1.y = floor(floor(blueColor) / 8.0);
        quad1.x = floor(blueColor) - (quad1.y * 8.0);

        vec2 quad2;
        quad2.y = floor(ceil(blueColor) / 8.0);
        quad2.x = ceil(blueColor) - (quad2.y * 8.0);

        vec2 texPos1;
        texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);
        texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);

        vec2 texPos2;
        texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);
        texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);

        vec4 newColor1 = texture2D(sLUTTexture, texPos1);
        vec4 newColor2 = texture2D(sLUTTexture, texPos2);

        vec4 newColor = mix(newColor1, newColor2, fract(blueColor));
        gl_FragColor = mix(textureColor, vec4(newColor.rgb, textureColor.a), uIntensity);
    }
)";

LUTFilter::LUTFilter(std::shared_ptr<rhi::RHIDevice> device) : BaseFilter(device) {
    initResources(LUT_VERTEX_SHADER, LUT_FRAGMENT_SHADER);
}

LUTFilter::~LUTFilter() {
    mLUTTexture.reset();
}

void LUTFilter::SetLUTTexture(std::shared_ptr<rhi::RHITexture> lutTexture) {
    mLUTTexture = lutTexture;
}

void LUTFilter::SetIntensity(float intensity) {
    mIntensity = intensity;
}

void LUTFilter::Draw(std::shared_ptr<rhi::RHITexture> inputTexture) {
    if (!mProgram || !mLUTTexture) {
        // If LUT isn't loaded, don't break the chain.
        // We shouldn't really draw without it, but for a solid pipeline,
        // we'd probably just skip drawing, or draw a pass-through.
        // For simplicity, we just won't render anything, which means the output FBO keeps its old content.
        // In a true graph, we'd copy input to output.
        return;
    }

    mDevice->BindProgram(mProgram);

    // Bind main texture to slot 0
    mDevice->BindTexture(0, inputTexture);
    mProgram->SetUniformInt("sTexture", 0);

    // Bind LUT texture to slot 1
    mDevice->BindTexture(1, mLUTTexture);
    mProgram->SetUniformInt("sLUTTexture", 1);

    mProgram->SetUniformFloat("uIntensity", mIntensity);

    mProgram->BindVertexAttribute("aPosition", mVertexBuffer, 2, 0, 0);
    mProgram->BindVertexAttribute("aTextureCoord", mTexCoordBuffer, 2, 0, 0);

    mDevice->DrawArrays(rhi::PrimitiveTopology::TRIANGLE_STRIP, 0, 4);
}
