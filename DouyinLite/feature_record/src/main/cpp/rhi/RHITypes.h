#ifndef RHI_TYPES_H
#define RHI_TYPES_H

#include <cstdint>

namespace rhi {

enum class BackendType {
    GLES,
    VULKAN
};

enum class TextureFormat {
    RGBA8,
    RGB8,
    R8,
    RGBA16F,
    RGBA32F,
    DEPTH24_STENCIL8
};

enum class TextureUsage {
    SAMPLED,
    COLOR_ATTACHMENT,
    DEPTH_STENCIL_ATTACHMENT,
    STORAGE
};

enum class TextureType {
    TEXTURE_2D,
    TEXTURE_OES // For Android SurfaceTexture
};

enum class BufferUsage {
    VERTEX_BUFFER,
    INDEX_BUFFER,
    UNIFORM_BUFFER,
    STORAGE_BUFFER
};

enum class ShaderStage {
    VERTEX,
    FRAGMENT,
    COMPUTE
};

enum class PrimitiveTopology {
    POINT_LIST,
    LINE_LIST,
    LINE_STRIP,
    TRIANGLE_LIST,
    TRIANGLE_STRIP
};

} // namespace rhi

#endif // RHI_TYPES_H
