package com.app.douyin.pro.feature.record.gl

object BeautyFilterShader {

    const val VERTEX_SHADER = """#version 300 es
        layout (location = 0) in vec4 aPosition;
        layout (location = 1) in vec2 aTextureCoord;
        out vec2 vTextureCoord;
        void main() {
            gl_Position = aPosition;
            vTextureCoord = aTextureCoord;
        }
    """

    const val FRAGMENT_SHADER = """#version 300 es
        precision mediump float;
        in vec2 vTextureCoord;
        uniform sampler2D sTexture;
        uniform vec2 uTexSize;

        out vec4 fragColor;

        const float blurRadius = 4.0;
        const float distanceNormalizationFactor = 3.0;

        void main() {
            vec4 centralColor = texture(sTexture, vTextureCoord);
            float totalWeight = 0.0;
            vec4 sum = vec4(0.0);

            for (float i = -blurRadius; i <= blurRadius; i++) {
                for (float j = -blurRadius; j <= blurRadius; j++) {
                    vec2 offset = vec2(i, j) / uTexSize;
                    vec4 sampleTex = texture(sTexture, vTextureCoord + offset);

                    float distance = length(vec2(i, j));
                    float weight = exp(-distance * distance / distanceNormalizationFactor);

                    float colorDiff = length(centralColor.rgb - sampleTex.rgb);
                    weight *= exp(-colorDiff * colorDiff / 0.1);

                    sum += sampleTex * weight;
                    totalWeight += weight;
                }
            }

            vec4 finalColor = sum / totalWeight;

            finalColor.r = min(1.0, finalColor.r * 1.12);
            finalColor.g = min(1.0, finalColor.g * 1.08);
            finalColor.b = min(1.0, finalColor.b * 1.05);

            float luminance = dot(finalColor.rgb, vec3(0.299, 0.587, 0.114));
            finalColor.rgb = mix(vec3(luminance), finalColor.rgb, 1.1);

            fragColor = finalColor;
        }
    """
}
