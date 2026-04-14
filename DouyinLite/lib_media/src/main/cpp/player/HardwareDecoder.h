#pragma once
#include <media/NdkMediaCodec.h>
#include <android/native_window_jni.h>

namespace douyin {
namespace player {

class HardwareDecoder {
private:
    AMediaCodec* mCodec;
    ANativeWindow* mWindow;
    bool isConfigured;

public:
    HardwareDecoder() : mCodec(nullptr), mWindow(nullptr), isConfigured(false) {}
    ~HardwareDecoder() {
        if (mCodec) {
            AMediaCodec_stop(mCodec);
            AMediaCodec_delete(mCodec);
        }
        if (mWindow) {
            ANativeWindow_release(mWindow);
        }
    }

    bool AttachWindow(JNIEnv* env, jobject surface) {
        if (mWindow) ANativeWindow_release(mWindow);
        mWindow = ANativeWindow_fromSurface(env, surface);
        return mWindow != nullptr;
    }

    bool ConfigureDecoder(const char* mimeType, int width, int height) {
        mCodec = AMediaCodec_createDecoderByType(mimeType);
        if (!mCodec) return false;

        AMediaFormat* format = AMediaFormat_new();
        AMediaFormat_setString(format, AMEDIAFORMAT_KEY_MIME, mimeType);
        AMediaFormat_setInt32(format, AMEDIAFORMAT_KEY_WIDTH, width);
        AMediaFormat_setInt32(format, AMEDIAFORMAT_KEY_HEIGHT, height);

        media_status_t status = AMediaCodec_configure(mCodec, format, mWindow, nullptr, 0);
        AMediaFormat_delete(format);

        if (status == AMEDIA_OK) {
            AMediaCodec_start(mCodec);
            isConfigured = true;
            return true;
        }
        return false;
    }

    void RenderOutputBufferToSurface(int timeoutUs) {
        if (!isConfigured) return;
        AMediaCodecBufferInfo info;
        ssize_t outIndex = AMediaCodec_dequeueOutputBuffer(mCodec, &info, timeoutUs);
        if (outIndex >= 0) {
            // 硬核零拷贝：将画面直接渲染到 EGL Surface
            AMediaCodec_releaseOutputBuffer(mCodec, outIndex, true);
        }
    }
};

} // namespace player
} // namespace douyin
