#ifndef OES_TO_2D_FILTER_H
#define OES_TO_2D_FILTER_H

#include "BaseFilter.h"

class OesTo2DFilter : public BaseFilter {
private:
    float mvpMatrix[16];
    float stMatrix[16];

public:
    OesTo2DFilter(std::shared_ptr<rhi::RHIDevice> device);
    void SetMatrix(float* matrix);
    void Draw(std::shared_ptr<rhi::RHITexture> inputTexture) override;
};

#endif // OES_TO_2D_FILTER_H
