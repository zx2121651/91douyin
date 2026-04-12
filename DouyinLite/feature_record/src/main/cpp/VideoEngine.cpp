#include "VideoEngine.h"
#include <android/log.h>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "VideoEngine", __VA_ARGS__)

VideoEngine::VideoEngine(int width, int height) : mWidth(width), mHeight(height), fbo(0), texOut(0) {
    mOesConverter = new OesTo2DFilter();
    initFBO();
}

VideoEngine::~VideoEngine() {
    delete mOesConverter;
    if (fbo != 0) glDeleteFramebuffers(1, &fbo);
    if (texOut != 0) glDeleteTextures(1, &texOut);
}

void VideoEngine::initFBO() {
    glGenTextures(1, &texOut);
    glBindTexture(GL_TEXTURE_2D, texOut);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, mWidth, mHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glGenFramebuffers(1, &fbo);
    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texOut, 0);

    if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
        LOGE("FBO initialization failed.");
    }
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

GLuint VideoEngine::ProcessFrame(GLuint inputOesTexture, float* matrix) {
    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    glViewport(0, 0, mWidth, mHeight);

    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    mOesConverter->SetMatrix(matrix);
    mOesConverter->Draw(inputOesTexture);

    // If ping-pong rendering were fully implemented, it would go here.
    // We render output to FBO attached texture and return it, keeping default screen framebuffer clean.

    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    return texOut;
}

void VideoEngine::AddFilter(int filterId) {
    // Dynamically adding filters would be implemented here
}
