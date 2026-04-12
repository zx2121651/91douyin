cmake_minimum_required(VERSION 3.22.1)
project("video_engine")

add_library(
        video_engine
        SHARED
        native-lib.cpp
        VideoEngine.cpp
        filters/BaseFilter.cpp
        filters/OesTo2DFilter.cpp
)

target_link_libraries(
        video_engine
        log
        GLESv3
        EGL
)
