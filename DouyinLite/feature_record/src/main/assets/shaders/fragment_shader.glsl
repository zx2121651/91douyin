#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;

in vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

out vec4 fragColor;

void main() {
    vec4 color = texture(sTexture, vTextureCoord);
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    fragColor = vec4(gray, gray, gray, color.a);
}
