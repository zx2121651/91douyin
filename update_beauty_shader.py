import os

file_path = 'DouyinLite/feature_record/src/main/java/com/app/douyin/pro/feature/record/gl/BeautyFilterShader.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Enhance the fragment shader with more sophisticated color tuning
enhanced_shader = """            vec4 finalColor = sum / totalWeight;

            // 抖音风格：高级冷白皮调色矩阵 (Cool White Toning)
            // 提升 R/G 通道增益，微调 B 通道保持清冷感
            finalColor.r = min(1.0, finalColor.r * 1.12);
            finalColor.g = min(1.0, finalColor.g * 1.08);
            finalColor.b = min(1.0, finalColor.b * 1.05);

            // 增加饱和度微调 (Saturation Adjustment)
            float luminance = dot(finalColor.rgb, vec3(0.299, 0.587, 0.114));
            finalColor.rgb = mix(vec3(luminance), finalColor.rgb, 1.1);

            gl_FragColor = finalColor;"""

content = content.replace('            vec4 finalColor = sum / totalWeight;\n            // 简单的皮肤提亮\n            finalColor.r = min(1.0, finalColor.r * 1.1);\n            finalColor.g = min(1.0, finalColor.g * 1.05);\n\n            gl_FragColor = finalColor;', enhanced_shader)

with open(file_path, 'w') as f:
    f.write(content)
