#ifndef VIDEO_ENGINE_H
#define VIDEO_ENGINE_H

#include "rhi/RHIDevice.h"
#include "filters/FilterGroup.h"
#include <memory>
#include <string>
#include "filters/OesTo2DFilter.h"

class VideoEngine {
public:
    VideoEngine(int width, int height);
    ~VideoEngine();

    // Returns a native handle (e.g. GLuint) for UI presentation
    int ProcessFrame(int inputOesTexture, float* matrix);

    // Legacy generic id
    void AddFilter(int filterId);

    // New rule-based dynamic filter
    void SetFilterWithRule(const std::string& ruleString);

private:
    int mWidth;
    int mHeight;

    std::shared_ptr<rhi::RHIDevice> mDevice;

    // Main OES converter
    std::shared_ptr<OesTo2DFilter> mOesConverter;

    // The main filter chain that executes after OES conversion
    std::shared_ptr<FilterGroup> mFilterChain;
};

#endif // VIDEO_ENGINE_H
