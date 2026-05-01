#ifndef RHI_DEVICE_H
#define RHI_DEVICE_H

#include "RHIResources.h"
#include <memory>

namespace rhi {

class RHIDevice {
public:
    virtual ~RHIDevice() = default;

    virtual BackendType GetBackendType() const = 0;

    // Resource Creation
    virtual std::shared_ptr<RHITexture> CreateTexture(uint32_t width, uint32_t height, TextureFormat format, TextureUsage usage, TextureType type = TextureType::TEXTURE_2D) = 0;

    // Specifically for wrapping an existing OES texture or Android HardwareBuffer
    virtual std::shared_ptr<RHITexture> CreateTextureFromNative(void* nativeHandle, uint32_t width, uint32_t height, TextureType type) = 0;

    virtual std::shared_ptr<RHIBuffer> CreateBuffer(size_t size, BufferUsage usage, const void* initialData = nullptr) = 0;

    virtual std::shared_ptr<RHIShader> CreateShader(ShaderStage stage, const std::string& sourceCode) = 0;

    virtual std::shared_ptr<RHIProgram> CreateProgram(std::shared_ptr<RHIShader> vertexShader, std::shared_ptr<RHIShader> fragmentShader) = 0;

    virtual std::shared_ptr<RHIFramebuffer> CreateFramebuffer() = 0;

    // Pipeline State
    virtual void BindProgram(std::shared_ptr<RHIProgram> program) = 0;
    virtual void BindFramebuffer(std::shared_ptr<RHIFramebuffer> framebuffer) = 0; // nullptr means default screen/surface FBO
    virtual void BindTexture(uint32_t slot, std::shared_ptr<RHITexture> texture) = 0;

    virtual void SetViewport(const Viewport& viewport) = 0;
    virtual void ClearColor(float r, float g, float b, float a) = 0;
    virtual void ClearDepthStencil(float depth, uint32_t stencil) = 0;

    // Drawing
    virtual void DrawArrays(PrimitiveTopology topology, uint32_t first, uint32_t count) = 0;
    virtual void DrawIndexed(PrimitiveTopology topology, uint32_t indexCount, std::shared_ptr<RHIBuffer> indexBuffer) = 0;

    // Sync & Present
    virtual void Flush() = 0;
    // For Vulkan/EGL SwapBuffers
    virtual void Present() = 0;
};

// Factory method to create the appropriate device
std::shared_ptr<RHIDevice> CreateRHIDevice(BackendType type);

} // namespace rhi

#endif // RHI_DEVICE_H
