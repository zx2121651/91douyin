cmake_minimum_required(VERSION 3.22.1)
project("douyin_core")

add_library(
        douyin_core
        SHARED
        JniOnLoad.cpp
)

target_link_libraries(
        douyin_core
        log
        mediandk
        android
)
