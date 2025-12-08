package com.datavite.distrivite.data.local.datastore

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.datavite.distrivite.domain.model.DomainFaceRecognition
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions

class TfLiteFaceRecognitionDataSource(
    private val context: Context,
    private val threshold: Float = 0.5f,
    private val maxResults: Int = 3
) : LocalFaceRecognitionDataSource {

    private var detector: FaceDetector? = null
    private var interpreter: Interpreter? = null


    private val _faceRecognitionResults = MutableLiveData<List<DomainFaceRecognition>>()
    val faceRecognitionResults: LiveData<List<DomainFaceRecognition>> get() = _faceRecognitionResults


    init {
        setupDetector()
        setupInterpreter()
    }

    private fun setupInterpreter() {
        val options = Interpreter.Options().setNumThreads(2)
        try {
            interpreter = Interpreter(
                FileUtil.loadMappedFile(context, "mobile_face_net.tflite"),
                options
            )
            Log.i("TfLiteFaceRecognitionTag", "setup mobile_face_net sucess to load interpreter model")

        } catch (e: Exception) {
            Log.e("TfLiteFaceRecognitionTag", "setup mobile_face_net Failed to load interpreter model", e)
        }
    }

    private fun setupDetector() {
        val realTimeOpts = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .build();

        try {
            detector = FaceDetection.getClient(realTimeOpts)
        } catch (e: Exception) {
            Log.e("TfLiteFaceRecognitionDataSource", "Failed to initialize face detector", e)
        }
    }

    private fun getOrientationFromRotation(rotation: Int): ImageProcessingOptions.Orientation {
        return when(rotation) {
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            else -> ImageProcessingOptions.Orientation.RIGHT_TOP
        }
    }

    override fun processFaceRecognitions(
        bitmap: Bitmap,
        rotation: Int
    ) {
        val inputImage = InputImage.fromBitmap(bitmap, rotation)
        detector?.process(inputImage)?.addOnCompleteListener{ task ->
            val results = mutableListOf<DomainFaceRecognition>()
            if (task.isSuccessful) {
                // Task completed successfully, handle the result
                val faces = task.result ?: emptyList() // Get faces or empty list

                for (face in faces) {
                    val bounds = face.boundingBox
                    val faceBitmap = Bitmap.createBitmap(
                        bitmap, bounds.left, bounds.top, bounds.width(), bounds.height()
                    )
                    Bitmap.createBitmap(bitmap, )
                    /*
                                        val faceNetImageProcessor = ImageProcessor.Builder()
                                            .add(ResizeOp(160, 160, ResizeOp.ResizeMethod.BILINEAR))
                                            .add(NormalizeOp(0f, 1f))
                                            .build()

                                        val tensorImage = TensorImage.fromBitmap(faceBitmap)
                                        val processedImage = faceNetImageProcessor.process(tensorImage)
                     */

                    val faceEmbedding = FloatArray(192) // Assuming FaceNet outputs 192-d embeddings
                    //interpreter?.run(processedImage.buffer, faceEmbedding)

                    val recognition = DomainFaceRecognition(
                        embeddings = faceEmbedding,
                        face = face
                    )

                    results.add(recognition)

                    Log.i("FaceRecognitiondistrivite", "Face detection success ${recognition.face}")
                }
            } else {
                // Task failed, handle the error
                Log.e("FaceRecognitiondistrivite", "Face detection failed", task.exception)

                // Update LiveData with an empty list or null
                results.clear()
            }
            _faceRecognitionResults.postValue(results)

        }?.addOnSuccessListener { faces -> }?.addOnFailureListener { e ->
                Log.e("FaceRecognitiondistrivite", "Face detection failed", e)
        }

    }

    override fun FaceRecognitions(): LiveData<List<DomainFaceRecognition>> {
        return faceRecognitionResults
    }
}
