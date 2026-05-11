#ifndef BEAUTY_FILTER_H
#define BEAUTY_FILTER_H

#include "BaseFilter.h"

// A simplified skin smoothing filter mimicking basic beauty cameras.
class BeautyFilter : public BaseFilter {
public:
    BeautyFilter(std::shared_ptr<rhi::RHIDevice> device);

    // Sets the smoothing intensity [0.0, 1.0]
    void SetSmoothing(float level);

    void Draw(std::shared_ptr<rhi::RHITexture> inputTexture) override;

private:
    float mSmoothLevel = 0.5f;
};

#endif // BEAUTY_FILTER_H
