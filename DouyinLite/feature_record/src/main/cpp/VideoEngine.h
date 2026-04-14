#ifndef VIDEO_ENGINE_H
#define VIDEO_ENGINE_H

#include <GLES3/gl3.h>
#include <GLES2/gl2ext.h>
#include <vector>
#include "filters/OesTo2DFilter.h"

class VideoEngine {
public:
    VideoEngine(int width, int height);
    ~VideoEngine();

    GLuint ProcessFrame(GLuint inputOesTexture, float* matrix);
    void AddFilter(int filterId);

private:
    int mWidth;
    int mHeight;
    GLuint fbo;
    GLuint texOut;
    OesTo2DFilter* mOesConverter;

    void initFBO();
};

#endif // VIDEO_ENGINE_H
