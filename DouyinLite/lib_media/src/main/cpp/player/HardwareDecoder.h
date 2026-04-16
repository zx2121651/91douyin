#pragma once
#include <media/NdkMediaCodec.h>
#include <android/native_window_jni.h>

namespace douyin {
namespace player {

class HardwareDecoder {
private:
    AMediaCodec* mCodec;
    ANativeWindow* mWindow;
    bool mIsConfigured;

public:
    HardwareDecoder();
    ~HardwareDecoder();

    bool Init();
    bool Configure(const char* mimeType, int width, int height, ANativeWindow* window);
    void RenderOutputBufferToSurface(int timeoutUs);
    void Release();

    // Helper to get AMediaCodec pointer if needed for future extensions
    AMediaCodec* GetCodec() const { return mCodec; }
};

} // namespace player
} // namespace douyin
