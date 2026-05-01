#include "RHIDevice.h"
#include "gles/GLESDevice.h"
#include "vulkan/VulkanDevice.h"

namespace rhi {

std::shared_ptr<RHIDevice> CreateRHIDevice(BackendType type) {
    if (type == BackendType::GLES) {
        return std::make_shared<GLESDevice>();
    } else if (type == BackendType::VULKAN) {
        return std::make_shared<VulkanDevice>();
    }
    return nullptr;
}

} // namespace rhi
