package com.app.douyin.pro.feature.record.gl

object BeautyFilterShader {

    const val VERTEX_SHADER = """
        attribute vec4 aPosition;
        attribute vec2 aTextureCoord;
        varying vec2 vTextureCoord;
        void main() {
            gl_Position = aPosition;
            vTextureCoord = aTextureCoord;
        }
    """

    // 基础的双边滤波(Bilateral Filter)思想实现简单磨皮效果
    const val FRAGMENT_SHADER = """
        precision mediump float;
        varying vec2 vTextureCoord;
        uniform sampler2D sTexture;
        uniform vec2 uTexSize; // 纹理分辨率

        // 简单的高斯模糊参数模拟磨皮
        const float blurRadius = 4.0;
        const float distanceNormalizationFactor = 3.0;

        void main() {
            vec4 centralColor = texture2D(sTexture, vTextureCoord);
            float sampleColor = 0.0;
            float totalWeight = 0.0;
            vec4 sum = vec4(0.0);

            // 简单的周围像素采样模糊
            for (float i = -blurRadius; i <= blurRadius; i++) {
                for (float j = -blurRadius; j <= blurRadius; j++) {
                    vec2 offset = vec2(i, j) / uTexSize;
                    vec4 sampleTex = texture2D(sTexture, vTextureCoord + offset);

                    float distance = length(vec2(i, j));
                    float weight = exp(-distance * distance / distanceNormalizationFactor);

                    // 基于亮度差的权重，保留边缘
                    float colorDiff = length(centralColor.rgb - sampleTex.rgb);
                    weight *= exp(-colorDiff * colorDiff / 0.1);

                    sum += sampleTex * weight;
                    totalWeight += weight;
                }
            }

            vec4 finalColor = sum / totalWeight;
            // 简单的皮肤提亮
            finalColor.r = min(1.0, finalColor.r * 1.1);
            finalColor.g = min(1.0, finalColor.g * 1.05);

            gl_FragColor = finalColor;
        }
    """
}
