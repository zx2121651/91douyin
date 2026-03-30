attribute vec4 aPosition;
attribute vec4 aTextureCoord;

uniform mat4 uSTMatrix; // 纹理变换矩阵 (从 SurfaceTexture 获得)
uniform mat4 uMVPMatrix; // 模型视图投影矩阵

varying vec2 vTextureCoord;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    // 采用 uSTMatrix 的基础上翻转Y轴
    vTextureCoord = (uSTMatrix * aTextureCoord).xy;
    vTextureCoord.y = 1.0 - vTextureCoord.y;
}
