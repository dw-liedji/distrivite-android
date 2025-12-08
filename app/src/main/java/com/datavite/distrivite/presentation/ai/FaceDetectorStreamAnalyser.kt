package com.datavite.distrivite.presentation.ai

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.datavite.distrivite.presentation.ai.model.FaceFrame
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FaceDetectorStreamAnalyser : ImageAnalysis.Analyzer {

    private val faceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .build()
        FaceDetection.getClient(options)
    }

    private var frameSkipCounter = 0

    // SharedFlow to emit face frames
    private val _faceFrameFlow = MutableSharedFlow<FaceFrame?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val faceFrameFlow: SharedFlow<FaceFrame?> = _faceFrameFlow

    // Function to detect faces from the given image
    private suspend fun detectFaces(inputImage: InputImage): List<Face> =
        suspendCancellableCoroutine { continuation ->
            faceDetector.process(inputImage)
                .addOnSuccessListener { faces ->
                    continuation.resume(faces)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                    e.printStackTrace()
                }
        }

    // Function to create a flow of detected faces
    private fun process(imageProxy: ImageProxy, isFrontCamera: Boolean = true): Flow<FaceFrame> = flow {
        val rotation = imageProxy.imageInfo.rotationDegrees
        val bitmap = BitmapUtils.rotateBitmap(imageProxy.toBitmap(), rotation.toFloat())
        val transformedBitmap = if (!isFrontCamera) BitmapUtils.flipHorizontally(bitmap) else bitmap
        val inputImage = InputImage.fromBitmap(transformedBitmap, 0)

        try {
            val faces = detectFaces(inputImage)
            if (faces.isNotEmpty()) {
                val numberOfFaceToDetect = 1
                val detectedFaces = faces.subList(0, numberOfFaceToDetect.coerceAtMost(faces.size))
                emit(FaceFrame(transformedBitmap, rotation, detectedFaces))
            } else {
                emit(FaceFrame(transformedBitmap, rotation, emptyList()))
            }
            Log.e("distrivite-ai", "Face detection completed ${faces.size}")
        } catch (e: Exception) {
            Log.e("FaceRecognitiondistrivite", "Face detection failed", e)
            // You can emit an empty FaceFrame or handle it as necessary
        } finally {
            imageProxy.close()
        }
    }.flowOn(Dispatchers.Default) // Ensure this runs on a background thread

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (frameSkipCounter % 5 == 0) { // Adjust the frame skip count as needed
            Executors.newSingleThreadExecutor().execute {
                runBlocking {
                    process(imageProxy).collect { faceFrame ->
                        // Emit the faceFrame to the SharedFlow
                        _faceFrameFlow.emit(faceFrame)
                    }
                }
            }
        } else {
            imageProxy.close() // Ensure the image is closed to avoid memory leaks
        }
        frameSkipCounter++
    }
}
