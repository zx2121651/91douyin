#include "HardwareDecoder.h"
#include "../common/Log.h"
#include <media/NdkMediaFormat.h>

namespace douyin {
namespace player {

HardwareDecoder::HardwareDecoder() : mCodec(nullptr), mWindow(nullptr), mIsConfigured(false) {
    LOGD("HardwareDecoder: Constructor called");
}

HardwareDecoder::~HardwareDecoder() {
    LOGD("HardwareDecoder: Destructor called");
    Release();
}

bool HardwareDecoder::Init() {
    LOGD("HardwareDecoder: Init called");
    // Currently, Init is a placeholder for any pre-configuration logic
    return true;
}

bool HardwareDecoder::Configure(const char* mimeType, int width, int height, ANativeWindow* window) {
    LOGI("HardwareDecoder: Configure(mime=%s, width=%d, height=%d)", mimeType, width, height);

    if (mIsConfigured) {
        LOGW("HardwareDecoder: Already configured, releasing previous resources");
        Release();
    }

    if (!mimeType || width <= 0 || height <= 0) {
        LOGE("HardwareDecoder: Invalid parameters for configuration");
        return false;
    }

    mWindow = window;
    if (mWindow) {
        ANativeWindow_acquire(mWindow);
    }

    mCodec = AMediaCodec_createDecoderByType(mimeType);
    if (!mCodec) {
        LOGE("HardwareDecoder: Failed to create decoder for mime type: %s", mimeType);
        return false;
    }

    AMediaFormat* format = AMediaFormat_new();
    AMediaFormat_setString(format, AMEDIAFORMAT_KEY_MIME, mimeType);
    AMediaFormat_setInt32(format, AMEDIAFORMAT_KEY_WIDTH, width);
    AMediaFormat_setInt32(format, AMEDIAFORMAT_KEY_HEIGHT, height);

    media_status_t status = AMediaCodec_configure(mCodec, format, mWindow, nullptr, 0);
    AMediaFormat_delete(format);

    if (status != AMEDIA_OK) {
        LOGE("HardwareDecoder: AMediaCodec_configure failed with status: %d", status);
        Release();
        return false;
    }

    status = AMediaCodec_start(mCodec);
    if (status != AMEDIA_OK) {
        LOGE("HardwareDecoder: AMediaCodec_start failed with status: %d", status);
        Release();
        return false;
    }

    mIsConfigured = true;
    LOGI("HardwareDecoder: Configured successfully");
    return true;
}

void HardwareDecoder::RenderOutputBufferToSurface(int timeoutUs) {
    if (!mIsConfigured || !mCodec) {
        return;
    }

    AMediaCodecBufferInfo info;
    ssize_t outIndex = AMediaCodec_dequeueOutputBuffer(mCodec, &info, timeoutUs);
    if (outIndex >= 0) {
        // Render to surface if mWindow was provided during Configure
        AMediaCodec_releaseOutputBuffer(mCodec, outIndex, true);
    } else if (outIndex == AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED) {
        LOGD("HardwareDecoder: Output format changed");
    } else if (outIndex == AMEDIACODEC_INFO_OUTPUT_BUFFERS_CHANGED) {
        LOGD("HardwareDecoder: Output buffers changed");
    } else if (outIndex == AMEDIACODEC_INFO_TRY_AGAIN_LATER) {
        // No buffer available
    } else {
        LOGW("HardwareDecoder: Dequeue output buffer returned: %zd", outIndex);
    }
}

void HardwareDecoder::Release() {
    LOGD("HardwareDecoder: Release called");
    if (mCodec) {
        AMediaCodec_stop(mCodec);
        AMediaCodec_delete(mCodec);
        mCodec = nullptr;
    }
    if (mWindow) {
        ANativeWindow_release(mWindow);
        mWindow = nullptr;
    }
    mIsConfigured = false;
}

} // namespace player
} // namespace douyin
