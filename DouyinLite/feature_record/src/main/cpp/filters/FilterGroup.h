#ifndef FILTER_GROUP_H
#define FILTER_GROUP_H

#include "BaseFilter.h"
#include <vector>
#include "../rhi/RHITexturePool.h"

class FilterGroup : public BaseFilter {
public:
    FilterGroup(std::shared_ptr<rhi::RHIDevice> device);
    ~FilterGroup() override;

    void AddFilter(std::shared_ptr<BaseFilter> filter);
    void ClearFilters();
    bool IsEmpty() const;

    // Overrides standard draw. It returns the final texture.
    // Notice: The returned texture might be wrapped in a PooledTexture,
    // so caller needs to handle it. For this interface, we return raw shared_ptr,
    // assuming the final consumer (screen) will eventually release it back to pool.
    std::shared_ptr<rhi::RHITexture> DrawPingPong(std::shared_ptr<rhi::RHITexture> inputTexture);

    // Legacy override (Not fully utilized in ping-pong, but required by interface)
    void Draw(std::shared_ptr<rhi::RHITexture> inputTexture) override;

private:
    std::vector<std::shared_ptr<BaseFilter>> mFilters;
    std::shared_ptr<rhi::RHIFramebuffer> mFbo; // Shared FBO for ping-pong swapping
};

#endif // FILTER_GROUP_H
