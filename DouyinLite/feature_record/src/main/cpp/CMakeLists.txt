cmake_minimum_required(VERSION 3.22.1)

project("feature_record")

# Include RHI directories
include_directories(
    ${CMAKE_SOURCE_DIR}
    ${CMAKE_SOURCE_DIR}/rhi
)

# Gathers all RHI sources
file(GLOB_RECURSE RHI_SRC "rhi/*.cpp")

add_library(
        video_engine
        SHARED
        native-lib.cpp
        VideoEngine.cpp
        filters/BaseFilter.cpp
        filters/OesTo2DFilter.cpp
        filters/FilterGroup.cpp
        filters/LUTFilter.cpp
        filters/BeautyFilter.cpp
        filters/BrightnessFilter.cpp
        filters/FilterFactory.cpp
        ${RHI_SRC}
)

find_library(
        log-lib
        log
)

find_library(
        gles3-lib
        GLESv3
)

find_library(
        egl-lib
        ${jnigraphics-lib}

find_library(
        jnigraphics-lib
        jnigraphics
)
        EGL
)

# Vulkan support
find_library(
        vulkan-lib
        vulkan
)

target_link_libraries(
        video_engine
        ${log-lib}
        ${gles3-lib}
        ${egl-lib}
        ${jnigraphics-lib}

find_library(
        jnigraphics-lib
        jnigraphics
)
        ${vulkan-lib}
)

# Add FilterGroup to CMake
# (It's already covered by file(GLOB_RECURSE RHI_SRC "rhi/*.cpp") but we need to ensure filters are included)
