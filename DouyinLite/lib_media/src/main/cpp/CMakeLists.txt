cmake_minimum_required(VERSION 3.22.1)
project("douyin_core")

# Define project-wide includes
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/common
    ${CMAKE_CURRENT_SOURCE_DIR}/player
    ${CMAKE_CURRENT_SOURCE_DIR}/core
)

# Core library sources
set(DOUYIN_CORE_SOURCES
    JniOnLoad.cpp
    player/HardwareDecoder.cpp
    player/HardwareDecoderJni.cpp
)

add_library(
    douyin_core
    SHARED
    ${DOUYIN_CORE_SOURCES}
)

target_link_libraries(
    douyin_core
    log
    mediandk
    android
)
