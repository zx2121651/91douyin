package com.app.douyin.pro.feature.record.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer

data class NosePosition(val x: Float, val y: Float)

class FaceTracker(context: Context) : ImageAnalysis.Analyzer {

    private var faceLandmarker: FaceLandmarker? = null

    private val _nosePosition = MutableStateFlow<NosePosition?>(null)
    val nosePosition: StateFlow<NosePosition?> = _nosePosition.asStateFlow()

    init {
        try {
            val baseOptions = BaseOptions.builder().setModelAssetPath("face_landmarker.task").build()
            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result: FaceLandmarkerResult, mpImage: MPImage ->
                    if (result.faceLandmarks().isNotEmpty()) {
                        // Nose tip is usually landmark 1 in MediaPipe
                        val nose = result.faceLandmarks()[0][1]
                        _nosePosition.value = NosePosition(nose.x(), nose.y())
                    } else {
                        _nosePosition.value = null
                    }
                }
                .setErrorListener { error ->
                    error.printStackTrace()
                }
                .build()

            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun analyze(imageProxy: ImageProxy) {
        val landmarker = faceLandmarker
        if (landmarker != null) {
            try {
                val bitmap = imageProxy.toBitmap()
                val mpImage = BitmapImageBuilder(bitmap).build()
                val frameTime = imageProxy.imageInfo.timestamp
                landmarker.detectAsync(mpImage, frameTime)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        imageProxy.close()
    }
}
