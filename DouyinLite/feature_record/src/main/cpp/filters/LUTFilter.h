#ifndef LUT_FILTER_H
#define LUT_FILTER_H

#include "BaseFilter.h"
#include <memory>

class LUTFilter : public BaseFilter {
public:
    LUTFilter(std::shared_ptr<rhi::RHIDevice> device);
    ~LUTFilter() override;

    // Set the LUT texture to be used for color mapping.
    // The texture should be a standard 512x512 LUT image.
    void SetLUTTexture(std::shared_ptr<rhi::RHITexture> lutTexture);

    // Set the intensity of the LUT effect [0.0, 1.0]
    void SetIntensity(float intensity);

    void Draw(std::shared_ptr<rhi::RHITexture> inputTexture) override;

private:
    std::shared_ptr<rhi::RHITexture> mLUTTexture;
    float mIntensity = 1.0f;
};

#endif // LUT_FILTER_H
