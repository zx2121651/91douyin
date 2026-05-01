#ifndef GLES_TYPES_H
#define GLES_TYPES_H

#include <GLES3/gl3.h>
#include <GLES2/gl2ext.h>
#include "../RHITypes.h"

namespace rhi {

inline GLenum MapTextureFormatToGLESInternal(TextureFormat format) {
    switch (format) {
        case TextureFormat::RGBA8: return GL_RGBA8;
        case TextureFormat::RGB8: return GL_RGB8;
        case TextureFormat::R8: return GL_R8;
        case TextureFormat::RGBA16F: return GL_RGBA16F;
        case TextureFormat::RGBA32F: return GL_RGBA32F;
        case TextureFormat::DEPTH24_STENCIL8: return GL_DEPTH24_STENCIL8;
        default: return GL_RGBA8;
    }
}

inline GLenum MapTextureFormatToGLESFormat(TextureFormat format) {
    switch (format) {
        case TextureFormat::RGBA8:
        case TextureFormat::RGBA16F:
        case TextureFormat::RGBA32F: return GL_RGBA;
        case TextureFormat::RGB8: return GL_RGB;
        case TextureFormat::R8: return GL_RED;
        case TextureFormat::DEPTH24_STENCIL8: return GL_DEPTH_STENCIL;
        default: return GL_RGBA;
    }
}

inline GLenum MapTextureFormatToGLESType(TextureFormat format) {
    switch (format) {
        case TextureFormat::RGBA8:
        case TextureFormat::RGB8:
        case TextureFormat::R8: return GL_UNSIGNED_BYTE;
        case TextureFormat::RGBA16F: return GL_HALF_FLOAT;
        case TextureFormat::RGBA32F: return GL_FLOAT;
        case TextureFormat::DEPTH24_STENCIL8: return GL_UNSIGNED_INT_24_8;
        default: return GL_UNSIGNED_BYTE;
    }
}

inline GLenum MapTextureTypeToGLES(TextureType type) {
    switch (type) {
        case TextureType::TEXTURE_2D: return GL_TEXTURE_2D;
        case TextureType::TEXTURE_OES: return GL_TEXTURE_EXTERNAL_OES;
        default: return GL_TEXTURE_2D;
    }
}

inline GLenum MapBufferUsageToGLES(BufferUsage usage) {
    switch (usage) {
        case BufferUsage::VERTEX_BUFFER: return GL_ARRAY_BUFFER;
        case BufferUsage::INDEX_BUFFER: return GL_ELEMENT_ARRAY_BUFFER;
        case BufferUsage::UNIFORM_BUFFER: return GL_UNIFORM_BUFFER;
        // Storage buffers are GL_SHADER_STORAGE_BUFFER in GLES 3.1
        case BufferUsage::STORAGE_BUFFER: return 0x90D2; // GL_SHADER_STORAGE_BUFFER
        default: return GL_ARRAY_BUFFER;
    }
}

inline GLenum MapShaderStageToGLES(ShaderStage stage) {
    switch (stage) {
        case ShaderStage::VERTEX: return GL_VERTEX_SHADER;
        case ShaderStage::FRAGMENT: return GL_FRAGMENT_SHADER;
        // Compute shader is GLES 3.1
        case ShaderStage::COMPUTE: return 0x91B9; // GL_COMPUTE_SHADER
        default: return GL_VERTEX_SHADER;
    }
}

inline GLenum MapTopologyToGLES(PrimitiveTopology topology) {
    switch (topology) {
        case PrimitiveTopology::POINT_LIST: return GL_POINTS;
        case PrimitiveTopology::LINE_LIST: return GL_LINES;
        case PrimitiveTopology::LINE_STRIP: return GL_LINE_STRIP;
        case PrimitiveTopology::TRIANGLE_LIST: return GL_TRIANGLES;
        case PrimitiveTopology::TRIANGLE_STRIP: return GL_TRIANGLE_STRIP;
        default: return GL_TRIANGLES;
    }
}

} // namespace rhi

#endif // GLES_TYPES_H
