#include "VideoEngine.h"
#include <android/log.h>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "VideoEngine", __VA_ARGS__)

VideoEngine::VideoEngine(int width, int height) : mWidth(width), mHeight(height) {
    // Instantiate RHI Device. Currently defaulting to GLES.
    // In a full implementation, this could be configured via JNI param.
    mDevice = rhi::CreateRHIDevice(rhi::BackendType::GLES);

    // We pass device to filters so they can allocate RHI resources natively
    mOesConverter = new OesTo2DFilter(mDevice);

    initFBO();
}

VideoEngine::~VideoEngine() {
    delete mOesConverter;
    // RHI resources clean themselves up via shared_ptr and destructors
}

void VideoEngine::initFBO() {
    // Create output texture via RHI
    mTexOut = mDevice->CreateTexture(mWidth, mHeight, rhi::TextureFormat::RGBA8, rhi::TextureUsage::COLOR_ATTACHMENT);

    // Create FBO via RHI
    mFbo = mDevice->CreateFramebuffer();
    mFbo->AttachColor(0, mTexOut);

    if (!mFbo->IsValid()) {
        LOGE("FBO initialization failed in RHI.");
    }
}

int VideoEngine::ProcessFrame(int inputOesTexture, float* matrix) {
    mDevice->BindFramebuffer(mFbo);

    rhi::Viewport vp = {0, 0, mWidth, mHeight};
    mDevice->SetViewport(vp);

    mDevice->ClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    // Create a temporary RHI texture wrapper for the incoming GL OES texture
    auto rhiInputTex = mDevice->CreateTextureFromNative((void*)(uintptr_t)inputOesTexture, mWidth, mHeight, rhi::TextureType::TEXTURE_OES);

    mOesConverter->SetMatrix(matrix);
    mOesConverter->Draw(rhiInputTex);

    mDevice->BindFramebuffer(nullptr); // Unbind

    // Return the underlying native GLuint so Android's Java CameraRenderer can render to screen
    return (int)(uintptr_t)mTexOut->GetNativeHandle();
}

void VideoEngine::AddFilter(int filterId) {
    // Dynamic filter management
}
