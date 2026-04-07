#version 310 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;

in vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

// Simulate time passing. We'll use a hardcoded value if time isn't passed,
// or we can just use the Y coordinate to create a static chromatic aberration effect.
// Since we don't have a time uniform yet, let's do a static RGB split based on a small offset.
out vec4 fragColor;

void main() {
    // Offset for chromatic aberration
    float offset = 0.01;

    // Read the Red channel slightly shifted to the left
    float r = texture(sTexture, vec2(vTextureCoord.x + offset, vTextureCoord.y)).r;

    // Read the Green channel normally
    float g = texture(sTexture, vTextureCoord).g;

    // Read the Blue channel slightly shifted to the right
    float b = texture(sTexture, vec2(vTextureCoord.x - offset, vTextureCoord.y)).b;

    // Alpha channel
    float a = texture(sTexture, vTextureCoord).a;

    // Introduce some "glitch" static lines based on the y coordinate
    float glitchLine = step(0.98, fract(vTextureCoord.y * 50.0)) * 0.1;

    fragColor = vec4(r + glitchLine, g + glitchLine, b + glitchLine, a);
}
