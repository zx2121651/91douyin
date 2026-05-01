#include "BaseFilter.h"
#include <android/log.h>
#include <cstdlib>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "BaseFilter", __VA_ARGS__)

const float BaseFilter::VERTICES[] = {
    -1.0f, -1.0f,
     1.0f, -1.0f,
    -1.0f,  1.0f,
     1.0f,  1.0f
};

const float BaseFilter::TEX_COORDS[] = {
    0.0f, 0.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f
};

BaseFilter::BaseFilter(std::shared_ptr<rhi::RHIDevice> device) : mDevice(device) {
    mVertexBuffer = mDevice->CreateBuffer(sizeof(VERTICES), rhi::BufferUsage::VERTEX_BUFFER, VERTICES);
    mTexCoordBuffer = mDevice->CreateBuffer(sizeof(TEX_COORDS), rhi::BufferUsage::VERTEX_BUFFER, TEX_COORDS);
}

BaseFilter::~BaseFilter() {
}

void BaseFilter::initResources(const char* vertexSource, const char* fragmentSource) {
    auto vs = mDevice->CreateShader(rhi::ShaderStage::VERTEX, vertexSource);
    auto fs = mDevice->CreateShader(rhi::ShaderStage::FRAGMENT, fragmentSource);
    mProgram = mDevice->CreateProgram(vs, fs);
}

void BaseFilter::Draw(std::shared_ptr<rhi::RHITexture> inputTexture) {
    if (!mProgram) return;

    mDevice->BindProgram(mProgram);

    mDevice->BindTexture(0, inputTexture);
    mProgram->SetUniformInt("sTexture", 0);

    mProgram->BindVertexAttribute("aPosition", mVertexBuffer, 2, 0, 0);
    mProgram->BindVertexAttribute("aTextureCoord", mTexCoordBuffer, 2, 0, 0);

    mDevice->DrawArrays(rhi::PrimitiveTopology::TRIANGLE_STRIP, 0, 4);
}
