#include "BeautyFilter.h"

// A very fast, simplified high-pass/bilateral skin smoothing approach
// commonly found in basic beauty shaders.

static const char* BEAUTY_VERTEX_SHADER = R"(
    attribute vec4 aPosition;
    attribute vec2 aTextureCoord;
    varying vec2 vTextureCoord;
    void main() {
        gl_Position = aPosition;
        vTextureCoord = aTextureCoord;
    }
)";

static const char* BEAUTY_FRAGMENT_SHADER = R"(
    precision mediump float;
    varying vec2 vTextureCoord;
    uniform sampler2D sTexture;
    uniform float uSmoothLevel;

    // Quick approximation for a 5x5 blur
    void main() {
        vec3 centralColor = texture2D(sTexture, vTextureCoord).rgb;

        // Very rudimentary blur approximation for demonstration.
        // A real beauty filter would do a two-pass separated Gaussian or Bilateral.
        vec2 offset = vec2(0.003, 0.003); // Approximate for 720p/1080p

        vec3 sampleColor = centralColor;
        sampleColor += texture2D(sTexture, vTextureCoord + vec2(offset.x, offset.y)).rgb;
        sampleColor += texture2D(sTexture, vTextureCoord + vec2(-offset.x, offset.y)).rgb;
        sampleColor += texture2D(sTexture, vTextureCoord + vec2(offset.x, -offset.y)).rgb;
        sampleColor += texture2D(sTexture, vTextureCoord + vec2(-offset.x, -offset.y)).rgb;

        sampleColor /= 5.0;

        // Edge preserving (High-pass approximation)
        float diff = distance(centralColor, sampleColor);
        float edgeWeight = clamp(1.0 - (diff * 10.0), 0.0, 1.0);

        vec3 finalColor = mix(centralColor, sampleColor, edgeWeight * uSmoothLevel);

        gl_FragColor = vec4(finalColor, 1.0);
    }
)";

BeautyFilter::BeautyFilter(std::shared_ptr<rhi::RHIDevice> device) : BaseFilter(device) {
    initResources(BEAUTY_VERTEX_SHADER, BEAUTY_FRAGMENT_SHADER);
}

void BeautyFilter::SetSmoothing(float level) {
    mSmoothLevel = level;
}

void BeautyFilter::Draw(std::shared_ptr<rhi::RHITexture> inputTexture) {
    if (!mProgram) return;

    mDevice->BindProgram(mProgram);

    mProgram->SetUniformFloat("uSmoothLevel", mSmoothLevel);

    mDevice->BindTexture(0, inputTexture);
    mProgram->SetUniformInt("sTexture", 0);

    mProgram->BindVertexAttribute("aPosition", mVertexBuffer, 2, 0, 0);
    mProgram->BindVertexAttribute("aTextureCoord", mTexCoordBuffer, 2, 0, 0);

    mDevice->DrawArrays(rhi::PrimitiveTopology::TRIANGLE_STRIP, 0, 4);
}
