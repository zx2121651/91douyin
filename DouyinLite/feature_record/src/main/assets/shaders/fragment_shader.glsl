#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

void main() {
    // 获取相机原始像素
    vec4 color = texture2D(sTexture, vTextureCoord);

    // 黑白灰度滤镜：计算亮度 (Luminance)
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));

    gl_FragColor = vec4(gray, gray, gray, color.a);
}
