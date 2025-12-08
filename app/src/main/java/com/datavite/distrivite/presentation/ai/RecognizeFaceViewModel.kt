package com.datavite.distrivite.presentation.ai

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.presentation.ai.model.FaceFrame
import com.datavite.distrivite.presentation.ai.model.FaceFrameRecognitionSecureResult
import com.datavite.distrivite.presentation.ai.model.FaceRecognitionSecureResult
import com.datavite.distrivite.presentation.ai.model.KnownFace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RecognizeFaceViewModel @Inject constructor (
    private val mlkitFaceDetector: MlkitFaceDetector,
    private val faceVideoStreamAnalyser: FaceVideoStreamAnalyser,
    private val faceRecognitionSecurePipeLine: FaceRecognitionSecurePipeLine,
    private val internalStorageFaceImageRepository: InternalStorageFaceImageRepository) : ViewModel() {
    private val knownFaces = mutableListOf<KnownFace>()
    private val _faceFrameRecognitionSecureResult = MutableStateFlow<FaceFrameRecognitionSecureResult?>(null)
    val faceFrameRecognitionSecureResult = _faceFrameRecognitionSecureResult.asStateFlow()
    private var isFrontCamera = true

    fun getVideoStreamAnalyser() = faceVideoStreamAnalyser
    init {
        faceVideoStreamAnalyser.setOnImageStreamAnalyse(
            onImageStreamAnalyse = this::onAnalyze
        )
        loadKnownFaces()
    }

    private fun loadKnownFaces() {
        val userBitmaps = internalStorageFaceImageRepository.readImagesFromInternalStorage()
        for(userBitmap in userBitmaps){
            val embeddings = mutableListOf<FloatArray>()
            for (bitmap in userBitmap.bitmaps){
                embeddings.add(faceRecognitionSecurePipeLine.getFaceRecogniser().getFaceEmbeddingProcessor().process(bitmap))
            }
            val knownFace =
                KnownFace(id = userBitmap.userName, userBitmap.userName, embeddings = embeddings)
            Log.i("distrivite-ai", "$knownFace loaded successfully")
            knownFaces.add(knownFace)
        }
    }

    private var isProcessing = false // Flag to track processing state
    public fun toggleCamera(){
        isFrontCamera = !isFrontCamera
    }
    private fun onAnalyze(imageProxy: ImageProxy) {
        // Prevent multiple simultaneous processing

        //Log.i("distrivite-ai", "processing frame from recognize view models")
        viewModelScope.launch {
            imageProxy.use {
                val faceFrame = mlkitFaceDetector.process(imageProxy, isFrontCamera)
                val faceFrameRecognitionSecureResult = secureRecognizeFaces(faceFrame = faceFrame)
                _faceFrameRecognitionSecureResult.value = faceFrameRecognitionSecureResult
            }
        }
    }

    private suspend fun secureRecognizeFaces(faceFrame: FaceFrame) : FaceFrameRecognitionSecureResult = withContext(
        Dispatchers.Default) {
        val faceRecognitionSecureResults = mutableListOf<FaceRecognitionSecureResult>()
        for (face in faceFrame.faces){
            val faceRecognitionSecureResult = faceRecognitionSecurePipeLine.recognize(faceFrame.bitmap, face, knownFaces)
            Log.i("distrivite-ai", "Secure ${faceRecognitionSecureResult.isSecure} is the face of ${faceRecognitionSecureResult.faceRecognitionResult.name} with confidence of ${faceRecognitionSecureResult.faceRecognitionResult.confidence}")
            faceRecognitionSecureResults.add(faceRecognitionSecureResult)
        }
        FaceFrameRecognitionSecureResult(
            faceFrame = faceFrame,
            faceRecognitionSecureResults = faceRecognitionSecureResults
        )
    }

    fun onTakePhoto(bitmap: Bitmap) {
        Log.i("distrivite-ai", "a photo have been taking ${bitmap.height}")
    }

}