#ifndef BASE_FILTER_H
#define BASE_FILTER_H

#include <GLES3/gl3.h>

class BaseFilter {
protected:
    GLuint mProgramId;
    GLuint mPositionHandle;
    GLuint mTextureCoordHandle;
    GLuint mTextureSamplerHandle;

    static const float VERTICES[];
    static const float TEX_COORDS[];

    GLuint createProgram(const char* vertexSource, const char* fragmentSource);

public:
    BaseFilter();
    virtual ~BaseFilter();
    virtual void Draw(GLuint inputTextureId);
};

#endif // BASE_FILTER_H
