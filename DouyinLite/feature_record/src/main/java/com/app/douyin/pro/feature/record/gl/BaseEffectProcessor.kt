package com.app.douyin.pro.feature.record.gl

import android.content.Context

/**
 * 基础 OpenGL 特效处理器存根。
 * 后续所有的滤镜、美颜特效处理可以继承这个基类来实现自己的纹理渲染逻辑。
 */
abstract class BaseEffectProcessor(protected val context: Context) {

    // 初始化 Shader 和 OpenGL 缓冲
    abstract fun initGL()

    // 处理传入的纹理，应用滤镜效果，并返回处理后的纹理 ID
    abstract fun processTexture(textureId: Int, width: Int, height: Int): Int

    // 释放资源
    abstract fun releaseGL()
}
