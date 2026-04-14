#include <jni.h>
#include <android/log.h>
#include "core/LockFreeRingBuffer.h"
#include "core/SpinLock.h"
#include "player/HardwareDecoder.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "DouyinNative", __VA_ARGS__)

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    LOGI("Douyin Core C++ Engine Loaded Successfully. RingBuffer & SpinLock & NDK Decoder Ready.");

    return JNI_VERSION_1_6;
}
