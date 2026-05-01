#ifndef BASE_FILTER_H
#define BASE_FILTER_H

#include "../rhi/RHIDevice.h"
#include <memory>

class BaseFilter {
protected:
    std::shared_ptr<rhi::RHIDevice> mDevice;
    std::shared_ptr<rhi::RHIProgram> mProgram;
    std::shared_ptr<rhi::RHIBuffer> mVertexBuffer;
    std::shared_ptr<rhi::RHIBuffer> mTexCoordBuffer;

    static const float VERTICES[];
    static const float TEX_COORDS[];

    void initResources(const char* vertexSource, const char* fragmentSource);

public:
    BaseFilter(std::shared_ptr<rhi::RHIDevice> device);
    virtual ~BaseFilter();

    // Draw using RHI Texture abstraction
    virtual void Draw(std::shared_ptr<rhi::RHITexture> inputTexture);
};

#endif // BASE_FILTER_H
