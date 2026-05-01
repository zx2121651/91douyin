#ifndef GLES_RESOURCES_H
#define GLES_RESOURCES_H

#include "../RHIResources.h"
#include "GLESTypes.h"
#include <android/log.h>

#define LOG_TAG "RHI_GLES"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace rhi {

class GLESTexture : public RHITexture {
public:
    GLESTexture(uint32_t width, uint32_t height, TextureFormat format, TextureType type)
        : mWidth(width), mHeight(height), mFormat(format), mType(type) {
        glGenTextures(1, &mTextureId);
        GLenum target = MapTextureTypeToGLES(mType);
        glBindTexture(target, mTextureId);

        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        if (type == TextureType::TEXTURE_2D) {
            glTexImage2D(target, 0, MapTextureFormatToGLESInternal(format), width, height, 0,
                         MapTextureFormatToGLESFormat(format), MapTextureFormatToGLESType(format), nullptr);
        }
        glBindTexture(target, 0);
    }

    GLESTexture(GLuint existingTextureId, uint32_t width, uint32_t height, TextureType type)
        : mTextureId(existingTextureId), mWidth(width), mHeight(height), mFormat(TextureFormat::RGBA8), mType(type), mOwnsTexture(false) {
    }

    ~GLESTexture() override {
        if (mOwnsTexture && mTextureId != 0) {
            glDeleteTextures(1, &mTextureId);
        }
    }

    uint32_t GetWidth() const override { return mWidth; }
    uint32_t GetHeight() const override { return mHeight; }
    TextureFormat GetFormat() const override { return mFormat; }

    void* GetNativeHandle() const override {
        return (void*)(uintptr_t)mTextureId;
    }

    GLuint GetGLTextureId() const { return mTextureId; }
    GLenum GetGLTarget() const { return MapTextureTypeToGLES(mType); }

private:
    GLuint mTextureId = 0;
    uint32_t mWidth = 0;
    uint32_t mHeight = 0;
    TextureFormat mFormat;
    TextureType mType;
    bool mOwnsTexture = true;
};

class GLESBuffer : public RHIBuffer {
public:
    GLESBuffer(size_t size, BufferUsage usage, const void* initialData)
        : mSize(size), mTarget(MapBufferUsageToGLES(usage)) {
        glGenBuffers(1, &mBufferId);
        glBindBuffer(mTarget, mBufferId);
        glBufferData(mTarget, size, initialData, GL_DYNAMIC_DRAW);
        glBindBuffer(mTarget, 0);
    }

    ~GLESBuffer() override {
        if (mBufferId != 0) {
            glDeleteBuffers(1, &mBufferId);
        }
    }

    size_t GetSize() const override { return mSize; }

    void UpdateData(const void* data, size_t size, size_t offset) override {
        glBindBuffer(mTarget, mBufferId);
        glBufferSubData(mTarget, offset, size, data);
        glBindBuffer(mTarget, 0);
    }

    GLuint GetGLBufferId() const { return mBufferId; }
    GLenum GetGLTarget() const { return mTarget; }

private:
    GLuint mBufferId = 0;
    size_t mSize = 0;
    GLenum mTarget;
};

class GLESShader : public RHIShader {
public:
    GLESShader(ShaderStage stage, const std::string& source) : mStage(stage) {
        GLenum glStage = MapShaderStageToGLES(stage);
        mShaderId = glCreateShader(glStage);
        const char* src = source.c_str();
        glShaderSource(mShaderId, 1, &src, nullptr);
        glCompileShader(mShaderId);

        GLint compiled;
        glGetShaderiv(mShaderId, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            GLint infoLen = 0;
            glGetShaderiv(mShaderId, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen > 1) {
                std::vector<char> infoLog(infoLen);
                glGetShaderInfoLog(mShaderId, infoLen, nullptr, infoLog.data());
                LOGE("Error compiling shader:\n%s", infoLog.data());
            }
            glDeleteShader(mShaderId);
            mShaderId = 0;
        }
    }

    ~GLESShader() override {
        if (mShaderId != 0) {
            glDeleteShader(mShaderId);
        }
    }

    ShaderStage GetStage() const override { return mStage; }
    GLuint GetGLShaderId() const { return mShaderId; }

private:
    GLuint mShaderId = 0;
    ShaderStage mStage;
};

class GLESProgram : public RHIProgram {
public:
    GLESProgram(std::shared_ptr<GLESShader> vertexShader, std::shared_ptr<GLESShader> fragmentShader) {
        mProgramId = glCreateProgram();
        if (vertexShader) glAttachShader(mProgramId, vertexShader->GetGLShaderId());
        if (fragmentShader) glAttachShader(mProgramId, fragmentShader->GetGLShaderId());
        glLinkProgram(mProgramId);

        GLint linked;
        glGetProgramiv(mProgramId, GL_LINK_STATUS, &linked);
        if (!linked) {
            GLint infoLen = 0;
            glGetProgramiv(mProgramId, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen > 1) {
                std::vector<char> infoLog(infoLen);
                glGetProgramInfoLog(mProgramId, infoLen, nullptr, infoLog.data());
                LOGE("Error linking program:\n%s", infoLog.data());
            }
            glDeleteProgram(mProgramId);
            mProgramId = 0;
        }
    }

    ~GLESProgram() override {
        if (mProgramId != 0) {
            glDeleteProgram(mProgramId);
        }
    }

    void SetUniformInt(const std::string& name, int value) override {
        GLint location = glGetUniformLocation(mProgramId, name.c_str());
        if (location != -1) {
            glUniform1i(location, value);
        }
    }

    void SetUniformFloat(const std::string& name, float value) override {
        GLint location = glGetUniformLocation(mProgramId, name.c_str());
        if (location != -1) {
            glUniform1f(location, value);
        }
    }

    void SetUniformMatrix4fv(const std::string& name, const float* value) override {
        GLint location = glGetUniformLocation(mProgramId, name.c_str());
        if (location != -1) {
            glUniformMatrix4fv(location, 1, GL_FALSE, value);
        }
    }

    void BindVertexAttribute(const std::string& name, std::shared_ptr<RHIBuffer> buffer, int size, int stride, int offset) override {
        GLint location = glGetAttribLocation(mProgramId, name.c_str());
        if (location != -1) {
            auto glBuffer = std::static_pointer_cast<GLESBuffer>(buffer);
            glBindBuffer(GL_ARRAY_BUFFER, glBuffer->GetGLBufferId());
            glEnableVertexAttribArray(location);
            glVertexAttribPointer(location, size, GL_FLOAT, GL_FALSE, stride, reinterpret_cast<const void*>((uintptr_t)offset));
            // Keep it bound for draw call, this is simplified.
        }
    }

    GLuint GetGLProgramId() const { return mProgramId; }

private:
    GLuint mProgramId = 0;
};

class GLESFramebuffer : public RHIFramebuffer {
public:
    GLESFramebuffer() {
        glGenFramebuffers(1, &mFboId);
    }

    ~GLESFramebuffer() override {
        if (mFboId != 0) {
            glDeleteFramebuffers(1, &mFboId);
        }
    }

    void AttachColor(uint32_t index, std::shared_ptr<RHITexture> texture) override {
        glBindFramebuffer(GL_FRAMEBUFFER, mFboId);
        auto glTex = std::static_pointer_cast<GLESTexture>(texture);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + index, GL_TEXTURE_2D, glTex->GetGLTextureId(), 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    void AttachDepthStencil(std::shared_ptr<RHITexture> texture) override {
        glBindFramebuffer(GL_FRAMEBUFFER, mFboId);
        auto glTex = std::static_pointer_cast<GLESTexture>(texture);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, glTex->GetGLTextureId(), 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    bool IsValid() const override {
        glBindFramebuffer(GL_FRAMEBUFFER, mFboId);
        GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return status == GL_FRAMEBUFFER_COMPLETE;
    }

    GLuint GetGLFramebufferId() const { return mFboId; }

private:
    GLuint mFboId = 0;
};

} // namespace rhi

#endif // GLES_RESOURCES_H
