#include "VideoEngine.h"
#include "filters/FilterFactory.h"
#include "filters/LUTFilter.h"
#include <android/log.h>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "VideoEngine", __VA_ARGS__)

VideoEngine::VideoEngine(int width, int height) : mWidth(width), mHeight(height) {
    mDevice = rhi::CreateRHIDevice(rhi::BackendType::GLES);
    rhi::RHITexturePool::GetInstance().Init(mDevice);

    mOesConverter = std::make_shared<OesTo2DFilter>(mDevice);

    // Initialize an empty filter chain
    mFilterChain = std::make_shared<FilterGroup>(mDevice);
}

VideoEngine::~VideoEngine() {
    // Release pool on destroy to clean up cached FBOs/Textures
    rhi::RHITexturePool::GetInstance().Clear();
}

int VideoEngine::ProcessFrame(int inputOesTexture, float* matrix) {
    // 1. Convert OES to 2D Texture
    auto baseInputTex = mDevice->CreateTextureFromNative((void*)(uintptr_t)inputOesTexture, mWidth, mHeight, rhi::TextureType::TEXTURE_OES);

    // We need an intermediate 2D texture to hold the result of OES conversion
    auto oesResultWrapped = std::make_shared<rhi::PooledTexture>(rhi::RHITexturePool::GetInstance().RequestTexture(mWidth, mHeight));
    auto oesResultTex = oesResultWrapped->Get();

    // Use an FBO to render OES to 2D
    auto fbo = mDevice->CreateFramebuffer();
    fbo->AttachColor(0, oesResultTex);

    mDevice->BindFramebuffer(fbo);
    rhi::Viewport vp = {0, 0, mWidth, mHeight};
    mDevice->SetViewport(vp);
    mDevice->ClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    mOesConverter->SetMatrix(matrix);
    mOesConverter->Draw(baseInputTex);

    // 2. Pass the 2D texture through the ping-pong filter chain
    // The filter chain handles FBO binding internally for intermediate steps.
    // Notice: if filter chain is empty, it returns the input directly.
    auto finalTex = mFilterChain->DrawPingPong(oesResultTex);

    // Unbind
    mDevice->BindFramebuffer(nullptr);

    // We return the raw GL uint.
    // The texture will be kept alive because finalTex holds a shared_ptr to it,
    // but in Android UI rendering loop, we typically do this synchronously.
    return (int)(uintptr_t)finalTex->GetNativeHandle();
}

void VideoEngine::AddFilter(int filterId) {
    // Legacy mapping or specific hardcoded filters
}

void VideoEngine::SetFilterWithRule(const std::string& ruleString) {
    mFilterChain = FilterFactory::ParseRuleString(mDevice, ruleString);
}

void VideoEngine::ApplyLUTFilter(int lutWidth, int lutHeight, const void* pixels, float intensity) {
    if (!pixels || !mDevice) return;

    auto lutTexture = mDevice->CreateTextureFromPixels(lutWidth, lutHeight, rhi::TextureFormat::RGBA8, pixels);

    // For simplicity, we just add it to the end of the current chain.
    // A robust system would replace existing LUT filters or allow inserting at specific nodes.
    auto lutFilter = std::make_shared<LUTFilter>(mDevice);
    lutFilter->SetLUTTexture(lutTexture);
    lutFilter->SetIntensity(intensity);

    mFilterChain->AddFilter(lutFilter);
}
