#include <jni.h>
#include <android/native_window_jni.h>
#include <new>
#include "HardwareDecoder.h"
#include "../common/Log.h"

using namespace douyin::player;

extern "C"
JNIEXPORT jlong JNICALL
Java_com_app_douyin_pro_lib_media_NativeHardwareDecoder_nativeCreate(JNIEnv *env, jobject thiz) {
    auto* decoder = new (std::nothrow) HardwareDecoder();
    if (!decoder) {
        LOGE("JNI: Failed to allocate HardwareDecoder");
        return 0;
    }
    LOGD("JNI: nativeCreate success, ptr=%p", decoder);
    return reinterpret_cast<jlong>(decoder);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_app_douyin_pro_lib_media_NativeHardwareDecoder_nativeInit(JNIEnv *env, jobject thiz, jlong ptr) {
    auto* decoder = reinterpret_cast<HardwareDecoder*>(ptr);
    if (!decoder) return JNI_FALSE;
    return decoder->Init() ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_app_douyin_pro_lib_media_NativeHardwareDecoder_nativeConfigure(JNIEnv *env, jobject thiz,
                                                                       jlong ptr, jstring mime_type,
                                                                       jint width, jint height,
                                                                       jobject surface) {
    auto* decoder = reinterpret_cast<HardwareDecoder*>(ptr);
    if (!decoder) return JNI_FALSE;

    const char* mime = env->GetStringUTFChars(mime_type, nullptr);
    ANativeWindow* window = nullptr;
    if (surface) {
        window = ANativeWindow_fromSurface(env, surface);
    }

    bool success = decoder->Configure(mime, width, height, window);

    if (window) {
        ANativeWindow_release(window); // HardwareDecoder::Configure will acquire its own reference if needed
    }
    env->ReleaseStringUTFChars(mime_type, mime);

    return success ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_app_douyin_pro_lib_media_NativeHardwareDecoder_nativeRender(JNIEnv *env, jobject thiz,
                                                                    jlong ptr, jint timeout_us) {
    auto* decoder = reinterpret_cast<HardwareDecoder*>(ptr);
    if (decoder) {
        decoder->RenderOutputBufferToSurface(timeout_us);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_app_douyin_pro_lib_media_NativeHardwareDecoder_nativeRelease(JNIEnv *env, jobject thiz, jlong ptr) {
    auto* decoder = reinterpret_cast<HardwareDecoder*>(ptr);
    if (decoder) {
        LOGD("JNI: nativeRelease, ptr=%p", decoder);
        delete decoder;
    }
}
