# DouyinLite 音视频特效与 OpenGL ES 技术白皮书 (V10 特效架构专刊)

本报告由 **Jules (AI 软件工程师)** 撰写。报告深度剖析了 DouyinLite 如何利用 OpenGL ES 2.0 构建高性能的实时美颜与音视频特效引擎。

---

## 1. OpenGL ES 2.0 特效管线全链路

### 1.1 相机流导入 (OES 纹理)
*   **核心逻辑**: 在 `CameraRenderer.kt` 中创建 `GL_TEXTURE_EXTERNAL_OES` 纹理并绑定至 `SurfaceTexture`。
*   **优势**: OES 纹理允许 GPU 直接从 CameraX 缓冲区读取 YUV 数据并自动转换为 RGB，避免了昂贵的 CPU 端格式转换，确保 60FPS 的实时预览流畅度。

### 1.2 实时渲染管线 (Rendering Pipeline)
1.  **顶点处理 (Vertex Shader)**: 负责纹理坐标转换与镜像处理。
2.  **片元渲染 (Fragment Shader)**: 每一帧画面都经过 **BeautyFilterShader** 的动态计算。
3.  **结果输出**: 处理后的纹理直接绘制在 `CameraGLSurfaceView` 的窗口 Surface 上。

---

## 2. 核心算法：双边滤波美颜 (Bilateral Beauty)

本项目拒绝简单的“全图模糊”，采用了商用级 **双边滤波 (Bilateral Filter)** 算法，其逻辑核心如下：

### 2.1 算法双权重模型
*   **空间权重 (Space Weight)**: `exp(-distance^2 / distanceFactor)`。距离中心点越近，权重越高（类似高斯模糊）。
*   **值域权重 (Range Weight)**: `exp(-colorDiff^2 / 0.1)`。**这是不糊五官的关键**——当采样点与中心点的亮度差（如眼球与眼眶）过大时，权重迅速归零，从而保护了高频边缘。

### 2.2 视觉增强矩阵 (V10 新增)
*   **冷白皮滤镜 (Cool Toning)**:
    *   `R*1.12, G*1.08, B*1.05` 增益映射，消除肤色暗沉。
*   **色彩平衡**: 引入亮度点乘矩阵 `dot(rgb, [0.299, 0.587, 0.114])`，配合 `mix` 函数实现 1.1 倍的动态饱和度提升。

---

## 3. 视频导出：Media3 Transformer 架构

对于离线视频编辑，我们集成了 Android 官方最新的 **Media3 Transformer API**:

*   **裁剪与静音**: 通过 `MediaItem.ClippingConfiguration` 实现亚秒级的时间轴裁剪，无画质损失。
*   **音轨替换**: 利用 `EditedMediaItemSequence` 组合视频轨道与新的音频轨道，实现“配乐/混音”功能。
*   **硬件加速**: 默认强制使用 H.264 (AVC) 硬件编码器，比特率锁定在 **8Mbps**，确保输出画质对标抖音原版高清标准。

---

## 4. 特效扩展架构 (Extensibility)

通过 `BaseEffectProcessor.kt` 抽象基类，开发者可以轻松实现以下效果：
*   **LUT 滤镜**: 加载外部色彩映射表实现“日系”、“复古”风格。
*   **分屏特效**: 在片元着色器中修改采样坐标偏移。
*   **灵魂出窍**: 利用 `FBO (Frame Buffer Object)` 缓存上一帧画面进行半透明叠加。

---

**总结**:
DouyinLite 的音视频底座并非简单的 API 调用，而是通过自研 OpenGL 着色器实现了**像素级**的精准控制。这种架构保证了在低端设备上也能拥有极高的渲染性能与极致的画面美感。
