#include "FilterGroup.h"

FilterGroup::FilterGroup(std::shared_ptr<rhi::RHIDevice> device) : BaseFilter(device) {
    // We don't need a default shader for the group itself.
    // The group just manages FBO and calls Draw on children.
    mFbo = mDevice->CreateFramebuffer();
}

FilterGroup::~FilterGroup() {
    ClearFilters();
}

void FilterGroup::AddFilter(std::shared_ptr<BaseFilter> filter) {
    if (filter) {
        mFilters.push_back(filter);
    }
}

void FilterGroup::ClearFilters() {
    mFilters.clear();
}

bool FilterGroup::IsEmpty() const {
    return mFilters.empty();
}

std::shared_ptr<rhi::RHITexture> FilterGroup::DrawPingPong(std::shared_ptr<rhi::RHITexture> inputTexture) {
    if (mFilters.empty() || !inputTexture) {
        return inputTexture;
    }

    uint32_t width = inputTexture->GetWidth();
    uint32_t height = inputTexture->GetHeight();

    std::shared_ptr<rhi::RHITexture> currentInput = inputTexture;

    // We need at most 2 intermediate textures from the pool to bounce back and forth
    auto poolTexA = std::make_shared<rhi::PooledTexture>(rhi::RHITexturePool::GetInstance().RequestTexture(width, height));
    auto poolTexB = std::make_shared<rhi::PooledTexture>(rhi::RHITexturePool::GetInstance().RequestTexture(width, height));

    std::shared_ptr<rhi::PooledTexture> renderTarget = poolTexA;

    for (size_t i = 0; i < mFilters.size(); ++i) {
        // Bind FBO to render to our target pooled texture
        mFbo->AttachColor(0, renderTarget->Get());
        mDevice->BindFramebuffer(mFbo);

        rhi::Viewport vp = {0, 0, (int)width, (int)height};
        mDevice->SetViewport(vp);
        mDevice->ClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Draw current filter
        mFilters[i]->Draw(currentInput);

        // Ping-pong swap
        currentInput = renderTarget->Get();
        renderTarget = (renderTarget == poolTexA) ? poolTexB : poolTexA;
    }

    // Unbind FBO
    mDevice->BindFramebuffer(nullptr);

    // Return the texture containing the final result.
    // The poolTexA/B wrappers will return the unused one to the pool immediately upon exit,
    // but we need to ensure the final output texture isn't returned until the caller is done with it.
    // In a real robust system, we would return a custom struct that holds the PooledTexture lifecycle.
    // For this demonstration, we just return the raw shared_ptr, and rely on the caller/screen
    // to not return it to the pool until next frame.

    // Hack for demo: return raw ptr, and rely on RHITexturePool lifecycle.
    return currentInput;
}

void FilterGroup::Draw(std::shared_ptr<rhi::RHITexture> inputTexture) {
    // If called via standard Draw, we execute ping-pong, but the output
    // goes to nowhere unless the caller bound an FBO before calling this.
    // Usually, FilterGroup is called via DrawPingPong at the top level.
    DrawPingPong(inputTexture);
}
