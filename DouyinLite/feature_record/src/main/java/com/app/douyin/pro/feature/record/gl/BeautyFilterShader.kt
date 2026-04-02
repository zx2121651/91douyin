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

            // 抖音风格：高级冷白皮调色矩阵 (Cool White Toning)
            // 提升 R/G 通道增益，微调 B 通道保持清冷感
            finalColor.r = min(1.0, finalColor.r * 1.12);
            finalColor.g = min(1.0, finalColor.g * 1.08);
            finalColor.b = min(1.0, finalColor.b * 1.05);

            // 增加饱和度微调 (Saturation Adjustment)
            float luminance = dot(finalColor.rgb, vec3(0.299, 0.587, 0.114));
            finalColor.rgb = mix(vec3(luminance), finalColor.rgb, 1.1);

            gl_FragColor = finalColor;
        }
    """
}
