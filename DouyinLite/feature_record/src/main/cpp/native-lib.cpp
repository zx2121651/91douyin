#include <jni.h>
#include <string>
#include "VideoEngine.h"

extern "C" JNIEXPORT void JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_initEngine(
        JNIEnv* env,
        jobject /* this */,
        jint width, jint height) {
    // In a full implementation, we might pass a pointer back to Java.
    // For now, VideoEngine instance could be stored globally or attached to nativeHandle.
}

extern "C" JNIEXPORT jint JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_processFrame(
        JNIEnv* env,
        jobject /* this */,
        jint oesTextureId, jfloatArray matrix) {
    // Stub implementation to compile. The Java NativeVideoEngine would need to hold the pointer.
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_addFilter(
        JNIEnv* env,
        jobject /* this */,
        jint filterId) {
}

extern "C" JNIEXPORT void JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_release(
        JNIEnv* env,
        jobject /* this */) {
}
