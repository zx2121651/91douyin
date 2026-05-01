#ifndef VIDEO_ENGINE_H
#define VIDEO_ENGINE_H

#include "rhi/RHIDevice.h"
#include <vector>
#include <memory>
#include "filters/OesTo2DFilter.h"

class VideoEngine {
public:
    VideoEngine(int width, int height);
    ~VideoEngine();

    // Returns a native handle (e.g. GLuint) for UI presentation if needed
    int ProcessFrame(int inputOesTexture, float* matrix);
    void AddFilter(int filterId);

private:
    int mWidth;
    int mHeight;

    std::shared_ptr<rhi::RHIDevice> mDevice;
    std::shared_ptr<rhi::RHIFramebuffer> mFbo;
    std::shared_ptr<rhi::RHITexture> mTexOut;

    OesTo2DFilter* mOesConverter;

    void initFBO();
};

#endif // VIDEO_ENGINE_H
