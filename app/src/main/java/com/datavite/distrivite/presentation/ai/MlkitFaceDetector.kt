package com.datavite.distrivite.presentation.ai

import android.util.Log
import androidx.camera.core.ImageProxy
import com.datavite.distrivite.presentation.ai.model.FaceFrame
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MlkitFaceDetector {

    private var detector: FaceDetector? = null

    init {
        setupDetector()
    }

    private fun setupDetector() {
        val realTimeOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .build();

        try {
            detector = FaceDetection.getClient(realTimeOpts)
        } catch (e: Exception) {
            Log.e("TfLiteFaceRecognitionDataSource", "Failed to initialize face detector", e)
        }
    }

    suspend fun process(
        imageProxy: ImageProxy, isFrontCamera:Boolean
    ) : FaceFrame = withContext(Dispatchers.Default) {
         val rotation = imageProxy.imageInfo.rotationDegrees
         val bitmap = BitmapUtils.rotateBitmap(imageProxy.toBitmap(), rotation.toFloat())
         // Check if the current camera is the front camera
         val transformedBitmap = if (!isFrontCamera) {BitmapUtils.flipHorizontally(bitmap) } else {bitmap }
         // The image is already rotated...
         val inputImage = InputImage.fromBitmap(transformedBitmap, 0)
        val faces = detectFaces(inputImage)
        Log.i("distrivite-ai-debug","Face detection finished")
        imageProxy.close()
        return@withContext FaceFrame(transformedBitmap, rotation, faces)
    }

    private suspend fun detectFaces (inputImage:InputImage) : List<Face> = suspendCoroutine { continuation ->
        detector?.process(inputImage)?.addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                // Task completed successfully, handle the result
                val numberOfFaceToDetect = 1
                if (task.result.isNotEmpty() && task.result.size > numberOfFaceToDetect) {
                    continuation.resume(task.result.subList(fromIndex = 0, toIndex = numberOfFaceToDetect-1))
                }else {
                    continuation.resume(task.result)
                }
                Log.e("distrivite-ai", "Face detection completed ${task.result.size}")
            } else {
                // Task failed, handle the error
                Log.e("FaceRecognitiondistrivite", "Face detection failed", task.exception)
                // Update LiveData with an empty list or null

            }
        }?.addOnFailureListener { e ->
            Log.e("FaceRecognitiondistrivite", "Face detection failed", e)
            continuation.resumeWithException(e)
        }
    }
}