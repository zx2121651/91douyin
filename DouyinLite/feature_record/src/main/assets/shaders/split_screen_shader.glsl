#version 310 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;

in vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

out vec4 fragColor;

void main() {
    vec2 uv = vTextureCoord;

    // Create a 2-split screen effect (top and bottom showing the same center)
    if (uv.y > 0.5) {
        // Top half: shift uv.y down
        uv.y = uv.y - 0.25;
    } else {
        // Bottom half: shift uv.y up
        uv.y = uv.y + 0.25;
    }

    fragColor = texture(sTexture, uv);
}
