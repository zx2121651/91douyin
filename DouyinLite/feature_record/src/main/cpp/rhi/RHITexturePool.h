#ifndef RHI_TEXTURE_POOL_H
#define RHI_TEXTURE_POOL_H

#include "RHIDevice.h"
#include <vector>
#include <memory>
#include <mutex>

namespace rhi {

class RHITexturePool {
public:
    static RHITexturePool& GetInstance() {
        static RHITexturePool instance;
        return instance;
    }

    void Init(std::shared_ptr<RHIDevice> device) {
        mDevice = device;
    }

    // Requests an intermediate texture for FBO attachments
    std::shared_ptr<RHITexture> RequestTexture(uint32_t width, uint32_t height, TextureFormat format = TextureFormat::RGBA8) {
        std::lock_guard<std::mutex> lock(mMutex);

        for (auto it = mPool.begin(); it != mPool.end(); ++it) {
            auto tex = *it;
            if (tex->GetWidth() == width && tex->GetHeight() == height && tex->GetFormat() == format) {
                mPool.erase(it);
                return tex;
            }
        }

        // If no suitable texture found, create a new one
        if (mDevice) {
            return mDevice->CreateTexture(width, height, format, TextureUsage::COLOR_ATTACHMENT);
        }
        return nullptr;
    }

    // Returns the texture to the pool for reuse
    void ReturnTexture(std::shared_ptr<RHITexture> texture) {
        if (!texture) return;
        std::lock_guard<std::mutex> lock(mMutex);
        mPool.push_back(texture);
    }

    void Clear() {
        std::lock_guard<std::mutex> lock(mMutex);
        mPool.clear(); // shared_ptr goes out of scope, releasing GL/Vk resources
    }

private:
    RHITexturePool() = default;
    ~RHITexturePool() { Clear(); }

    std::shared_ptr<RHIDevice> mDevice;
    std::vector<std::shared_ptr<RHITexture>> mPool;
    std::mutex mMutex;
};

// A smart wrapper that automatically returns the texture to the pool upon destruction
class PooledTexture {
public:
    PooledTexture(std::shared_ptr<RHITexture> tex) : mTex(tex) {}
    ~PooledTexture() {
        if (mTex) {
            RHITexturePool::GetInstance().ReturnTexture(mTex);
        }
    }

    std::shared_ptr<RHITexture> Get() const { return mTex; }
    operator bool() const { return mTex != nullptr; }

private:
    std::shared_ptr<RHITexture> mTex;
};

} // namespace rhi

#endif // RHI_TEXTURE_POOL_H
