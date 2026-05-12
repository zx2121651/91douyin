set(RHI_SRC
    RHIDeviceFactory.cpp
    gles/GLESDevice.cpp
    vulkan/VulkanDevice.cpp
)

# Vulkan is supported starting Android 7.0 (API 24) natively in NDK.
# We will link against GLES3 and vulkan.
