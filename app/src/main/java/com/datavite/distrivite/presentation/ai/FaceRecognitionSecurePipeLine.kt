package com.datavite.distrivite.presentation.ai

import android.graphics.Bitmap
import com.datavite.distrivite.presentation.ai.model.FaceRecognitionSecureResult
import com.datavite.distrivite.presentation.ai.model.KnownFace
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FaceRecognitionSecurePipeLine @Inject constructor  (
    private val faceSpoofingDetector: FaceAntiSpoofingPlainDetector,
    private val faceRecogniser: FaceRecogniser
) {
    
    fun getFaceRecogniser() = faceRecogniser
    fun getFaceSpoofingDetector() = faceSpoofingDetector

    suspend fun recognize(
        bitmap: Bitmap,
        face: Face,
        knownFaces: List<KnownFace>,
        isLivenessActive : Boolean =true
    ): FaceRecognitionSecureResult = withContext(Dispatchers.Default) {
        val croppedBitmap = BitmapUtils.cropRectFromBitmap(bitmap, face.boundingBox)

        if (isLivenessActive) {
            // Run both tasks in parallel on the default dispatcher
            val isSecureDeferred = async { faceSpoofingDetector.isLiveFace(croppedBitmap) }
            val faceRecognitionResultDeferred = async { faceRecogniser.recognize(croppedBitmap, face, knownFaces) }

            // Wait for both results
            val isSecure = isSecureDeferred.await()
            val faceRecognitionResult = faceRecognitionResultDeferred.await()

            FaceRecognitionSecureResult(
                isSecure = isSecure,
                face = face,
                faceRecognitionResult = faceRecognitionResult
            )
        } else {
            // Run tasks sequentially
            val faceRecognitionResult = faceRecogniser.recognize(croppedBitmap, face, knownFaces)

            FaceRecognitionSecureResult(
                isSecure = true,
                face = face,
                faceRecognitionResult = faceRecognitionResult
            )
        }
    }
}