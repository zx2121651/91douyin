#include <jni.h>
#include "VideoEngine.h"

extern "C" JNIEXPORT void JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_initEngine(JNIEnv *env, jobject thiz, jint width, jint height) {
    VideoEngine* engine = new VideoEngine(width, height);

    jclass clazz = env->GetObjectClass(thiz);
    jfieldID handleField = env->GetFieldID(clazz, "nativeHandle", "J");
    env->SetLongField(thiz, handleField, reinterpret_cast<jlong>(engine));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_processFrame(JNIEnv *env, jobject thiz, jint oes_texture_id, jfloatArray matrix) {
    jclass clazz = env->GetObjectClass(thiz);
    jfieldID handleField = env->GetFieldID(clazz, "nativeHandle", "J");
    VideoEngine* engine = reinterpret_cast<VideoEngine*>(env->GetLongField(thiz, handleField));

    if (!engine) return 0;

    jfloat* mat = env->GetFloatArrayElements(matrix, JNI_FALSE);

    GLuint outTextureId = engine->ProcessFrame(oes_texture_id, mat);

    env->ReleaseFloatArrayElements(matrix, mat, 0);
    return outTextureId;
}

extern "C" JNIEXPORT void JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_addFilter(JNIEnv *env, jobject thiz, jint filter_id) {
    jclass clazz = env->GetObjectClass(thiz);
    jfieldID handleField = env->GetFieldID(clazz, "nativeHandle", "J");
    VideoEngine* engine = reinterpret_cast<VideoEngine*>(env->GetLongField(thiz, handleField));

    if (engine) {
        engine->AddFilter(filter_id);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_app_douyin_pro_feature_record_gl_NativeVideoEngine_release(JNIEnv *env, jobject thiz) {
    jclass clazz = env->GetObjectClass(thiz);
    jfieldID handleField = env->GetFieldID(clazz, "nativeHandle", "J");
    VideoEngine* engine = reinterpret_cast<VideoEngine*>(env->GetLongField(thiz, handleField));

    if (engine) {
        delete engine;
        env->SetLongField(thiz, handleField, 0);
    }
}
