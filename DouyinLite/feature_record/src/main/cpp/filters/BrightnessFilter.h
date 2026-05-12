#ifndef BRIGHTNESS_FILTER_H
#define BRIGHTNESS_FILTER_H

#include "BaseFilter.h"

class BrightnessFilter : public BaseFilter {
public:
    BrightnessFilter(std::shared_ptr<rhi::RHIDevice> device);
    void SetBrightness(float brightness);
    void Draw(std::shared_ptr<rhi::RHITexture> inputTexture) override;

private:
    float mBrightness = 0.0f;
};

#endif // BRIGHTNESS_FILTER_H
