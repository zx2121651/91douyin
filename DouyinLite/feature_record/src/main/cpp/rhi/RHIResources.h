#ifndef RHI_RESOURCES_H
#define RHI_RESOURCES_H

#include "RHITypes.h"
#include <string>
#include <vector>
#include <memory>

namespace rhi {

class RHITexture {
public:
    virtual ~RHITexture() = default;
    virtual uint32_t GetWidth() const = 0;
    virtual uint32_t GetHeight() const = 0;
    virtual TextureFormat GetFormat() const = 0;

    // Optional: Get underlying native handle (e.g., GLuint or VkImage)
    virtual void* GetNativeHandle() const = 0;
};

class RHIBuffer {
public:
    virtual ~RHIBuffer() = default;
    virtual size_t GetSize() const = 0;
    virtual void UpdateData(const void* data, size_t size, size_t offset = 0) = 0;
};

class RHIShader {
public:
    virtual ~RHIShader() = default;
    virtual ShaderStage GetStage() const = 0;
};

class RHIProgram {
public:
    virtual ~RHIProgram() = default;
    virtual void SetUniformInt(const std::string& name, int value) = 0;
    virtual void SetUniformFloat(const std::string& name, float value) = 0;
    virtual void SetUniformMatrix4fv(const std::string& name, const float* value) = 0;
    // Binding for VBOs/VAOs if abstracted at program level for simplicity
    virtual void BindVertexAttribute(const std::string& name, std::shared_ptr<RHIBuffer> buffer, int size, int stride, int offset) = 0;
};

class RHIFramebuffer {
public:
    virtual ~RHIFramebuffer() = default;
    virtual void AttachColor(uint32_t index, std::shared_ptr<RHITexture> texture) = 0;
    virtual void AttachDepthStencil(std::shared_ptr<RHITexture> texture) = 0;
    virtual bool IsValid() const = 0;
};

struct Viewport {
    int x;
    int y;
    int width;
    int height;
};

} // namespace rhi

#endif // RHI_RESOURCES_H
