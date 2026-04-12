#ifndef OES_TO_2D_FILTER_H
#define OES_TO_2D_FILTER_H

#include "BaseFilter.h"

class OesTo2DFilter : public BaseFilter {
private:
    GLuint muMVPMatrixHandle;
    GLuint muSTMatrixHandle;
    float mvpMatrix[16];
    float stMatrix[16];

public:
    OesTo2DFilter();
    void SetMatrix(float* matrix);
    void Draw(GLuint inputTextureId) override;
};

#endif // OES_TO_2D_FILTER_H
