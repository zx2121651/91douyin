#ifndef GLES_DEVICE_H
#define GLES_DEVICE_H

#include "../RHIDevice.h"
#include "GLESResources.h"

namespace rhi {

class GLESDevice : public RHIDevice {
public:
    GLESDevice() = default;
    ~GLESDevice() override = default;

    BackendType GetBackendType() const override { return BackendType::GLES; }

    std::shared_ptr<RHITexture> CreateTexture(uint32_t width, uint32_t height, TextureFormat format, TextureUsage usage, TextureType type) override {
        return std::make_shared<GLESTexture>(width, height, format, type);
    }

    std::shared_ptr<RHITexture> CreateTextureFromNative(void* nativeHandle, uint32_t width, uint32_t height, TextureType type) override {
        GLuint tex = (GLuint)(uintptr_t)nativeHandle;
        return std::make_shared<GLESTexture>(tex, width, height, type);
    }

    std::shared_ptr<RHITexture> CreateTextureFromPixels(uint32_t width, uint32_t height, TextureFormat format, const void* pixels) override {
        auto tex = std::make_shared<GLESTexture>(width, height, format, TextureType::TEXTURE_2D);
        tex->UploadPixels(pixels);
        return tex;
    }

    std::shared_ptr<RHIBuffer> CreateBuffer(size_t size, BufferUsage usage, const void* initialData) override {
        return std::make_shared<GLESBuffer>(size, usage, initialData);
    }

    std::shared_ptr<RHIShader> CreateShader(ShaderStage stage, const std::string& sourceCode) override {
        return std::make_shared<GLESShader>(stage, sourceCode);
    }

    std::shared_ptr<RHIProgram> CreateProgram(std::shared_ptr<RHIShader> vertexShader, std::shared_ptr<RHIShader> fragmentShader) override {
        auto glesVs = std::static_pointer_cast<GLESShader>(vertexShader);
        auto glesFs = std::static_pointer_cast<GLESShader>(fragmentShader);
        return std::make_shared<GLESProgram>(glesVs, glesFs);
    }

    std::shared_ptr<RHIFramebuffer> CreateFramebuffer() override {
        return std::make_shared<GLESFramebuffer>();
    }

    void BindProgram(std::shared_ptr<RHIProgram> program) override {
        if (program) {
            auto glesProg = std::static_pointer_cast<GLESProgram>(program);
            glUseProgram(glesProg->GetGLProgramId());
        } else {
            glUseProgram(0);
        }
    }

    void BindFramebuffer(std::shared_ptr<RHIFramebuffer> framebuffer) override {
        if (framebuffer) {
            auto glesFbo = std::static_pointer_cast<GLESFramebuffer>(framebuffer);
            glBindFramebuffer(GL_FRAMEBUFFER, glesFbo->GetGLFramebufferId());
        } else {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    void BindTexture(uint32_t slot, std::shared_ptr<RHITexture> texture) override {
        glActiveTexture(GL_TEXTURE0 + slot);
        if (texture) {
            auto glTex = std::static_pointer_cast<GLESTexture>(texture);
            glBindTexture(glTex->GetGLTarget(), glTex->GetGLTextureId());
        } else {
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    void SetViewport(const Viewport& viewport) override {
        glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
    }

    void ClearColor(float r, float g, float b, float a) override {
        glClearColor(r, g, b, a);
        glClear(GL_COLOR_BUFFER_BIT);
    }

    void ClearDepthStencil(float depth, uint32_t stencil) override {
        glClearDepthf(depth);
        glClearStencil(stencil);
        glClear(GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    void DrawArrays(PrimitiveTopology topology, uint32_t first, uint32_t count) override {
        glDrawArrays(MapTopologyToGLES(topology), first, count);
    }

    void DrawIndexed(PrimitiveTopology topology, uint32_t indexCount, std::shared_ptr<RHIBuffer> indexBuffer) override {
        auto glesIbo = std::static_pointer_cast<GLESBuffer>(indexBuffer);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, glesIbo->GetGLBufferId());
        // Assumes 16-bit indices for simplicity
        glDrawElements(MapTopologyToGLES(topology), indexCount, GL_UNSIGNED_SHORT, 0);
    }

    void Flush() override {
        glFlush();
    }

    void Present() override {
        // Not implemented here, handled by EGL swap buffers at Java/JNI layer or caller
    }
};

} // namespace rhi

#endif // GLES_DEVICE_H
