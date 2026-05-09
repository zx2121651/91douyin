#include <jni.h>
#include <string>
#include "VideoEngine.h"

// For simplicity in this structure, we hold a global instance.
// A real app should pass pointer address (jlong) inside Java NativeVideoEngine class.
static VideoEngine* g_videoEngine = nullptr;

extern "C" JNIEXPORT void JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_initEngine(
        JNIEnv* env,
        jobject /* this */,
        jint width, jint height) {
    if (g_videoEngine) {
        delete g_videoEngine;
    }
    g_videoEngine = new VideoEngine(width, height);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_processFrame(
        JNIEnv* env,
        jobject /* this */,
        jint oesTextureId, jfloatArray matrix) {
    if (!g_videoEngine) return 0;

    jfloat* matElements = env->GetFloatArrayElements(matrix, 0);
    int result = g_videoEngine->ProcessFrame(oesTextureId, matElements);
    env->ReleaseFloatArrayElements(matrix, matElements, 0);

    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_addFilter(
        JNIEnv* env,
        jobject /* this */,
        jint filterId) {
    if (g_videoEngine) {
        g_videoEngine->AddFilter(filterId);
    }
}

// Support for dynamic rule string from Java
extern "C" JNIEXPORT void JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_setFilterWithRule(
        JNIEnv* env,
        jobject /* this */,
        jstring rule) {
    if (g_videoEngine && rule) {
        const char* ruleChars = env->GetStringUTFChars(rule, 0);
        g_videoEngine->SetFilterWithRule(std::string(ruleChars));
        env->ReleaseStringUTFChars(rule, ruleChars);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_release(
        JNIEnv* env,
        jobject /* this */) {
    if (g_videoEngine) {
        delete g_videoEngine;
        g_videoEngine = nullptr;
    }
}
