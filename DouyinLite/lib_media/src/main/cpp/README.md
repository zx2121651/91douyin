# DouyinLite Native Media Base

This directory contains the NDK (Native Development Kit) components for the DouyinLite media module.

## Directory Structure

- `common/`: Shared utilities and headers (e.g., `Log.h`).
- `core/`: Low-level primitives (e.g., `SpinLock.h`, `LockFreeRingBuffer.h`).
- `player/`: Media player components and hardware decoder logic.
- `CMakeLists.txt`: Build configuration for the `douyin_core` shared library.
- `JniOnLoad.cpp`: JNI initialization and library entry point.

## Key Components

### HardwareDecoder
A wrapper around the Android NDK `AMediaCodec` and `ANativeWindow` APIs.
- **Location**: `player/HardwareDecoder.h` and `player/HardwareDecoder.cpp`.
- **Functionality**: Minimal lifecycle for initialization, configuration, rendering, and resource release.
- **JNI Bridge**: `player/HardwareDecoderJni.cpp` and `NativeHardwareDecoder.kt`.

## Lifecycle and Memory Management

- **Object Creation**: Native objects are created via JNI and returned as a `jlong` (pointer) to the Java layer.
- **Ownership**: The Java class `NativeHardwareDecoder` holds the reference to the native object.
- **Cleanup**: Resources MUST be released by calling `release()` on the Java object, which triggers `delete` on the native pointer. A `finalize()` method is provided as a safety net.
- **Thread Safety**: Currently, the `HardwareDecoder` is not thread-safe by design; synchronization should be handled by the caller or evolved in future iterations.

## Error Handling

- **Logging**: Use the macros in `common/Log.h` (`LOGI`, `LOGE`, etc.) for consistent logging under the `DouyinNative` tag.
- **Return Values**: JNI methods return `jboolean` or other status indicators to propagate failures to the Java layer.
- **Crashes**: Parameter validation is performed at both JNI and C++ levels to prevent NULL pointer dereferences and native crashes.

## Future Evolution

1. **Demuxer Integration**: Add NDK `AMediaExtractor` support.
2. **Rendering Pipeline**: Integrate OpenGL/Vulkan for post-processing and display.
3. **Audio Sync**: Implement clock synchronization for A/V playback.
4. **FBO 管线**: Support Frame Buffer Objects for advanced effects.
