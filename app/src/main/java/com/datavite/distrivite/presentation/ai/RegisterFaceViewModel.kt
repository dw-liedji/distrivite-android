package com.datavite.distrivite.presentation.ai

import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.presentation.ai.model.FaceFrameRecognitionSecureResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RegisterFaceViewModel @Inject constructor(
    private val internalStorageFaceImageRepository: InternalStorageFaceImageRepository,
    private val faceVideoStreamAnalyser: FaceVideoStreamAnalyser,
    private val mlkitFaceDetector: MlkitFaceDetector) : ViewModel() {
    fun getImageCapturedCallback() : ImageCapture.OnImageCapturedCallback = imageCapturedCallback
    fun getFaceVideoStreamAnalyser() = faceVideoStreamAnalyser
    private var isFrontCamera = true

    private val imageCapturedCallback =  object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(imageProxy: ImageProxy) {
            super.onCaptureSuccess(imageProxy)
            this@RegisterFaceViewModel.onTakePhoto(imageProxy)
        }

        override fun onError(exception: ImageCaptureException) {
            super.onError(exception)
            Log.e("Camera", "Couldn't take photo: ", exception)
        }
    }

    init {
        faceVideoStreamAnalyser.setOnImageStreamAnalyse(this::onAnalyze)
    }

    // State flow to hold the UserBitmap instance for the current user
    private val _userBitmap = MutableStateFlow<UserBitmap>(UserBitmap("", emptyList()))
    val userBitmap: StateFlow<UserBitmap> = _userBitmap.asStateFlow()

    private val _faceFrameRecognitionSecureResult = MutableStateFlow<FaceFrameRecognitionSecureResult?>(null)
    val faceFrameRecognitionSecureResult = _faceFrameRecognitionSecureResult.asStateFlow()


    // Function to register a user with a username
    fun onUserInfoSaved(username: String) {
        _userBitmap.value = UserBitmap(username, emptyList())
    }

    fun toggleCamera() {
        isFrontCamera = !isFrontCamera
    }

    // Function to add a photo for the current user
    fun onTakePhoto(imageProxy: ImageProxy)  {
        viewModelScope.launch {
            withContext(context = Dispatchers.Default) {
                try {
                        val faceFrame = mlkitFaceDetector.process(imageProxy, isFrontCamera)
                        if (faceFrame.faces.isNotEmpty()){
                            val croppedBitmap = BitmapUtils.cropRectFromBitmap(faceFrame.bitmap, faceFrame.faces[0].boundingBox)
                            val currentUserBitmap = _userBitmap.value
                            if (currentUserBitmap.userName.isNotEmpty()) {
                                val updatedBitmaps = currentUserBitmap.bitmaps.toMutableList()
                                updatedBitmaps.add(croppedBitmap)
                                _userBitmap.value = currentUserBitmap.copy(bitmaps = updatedBitmaps)
                            } else {}
                        }else {
                            Log.i("distrivite-ai-img", "Image not save missing face, Please place your face correctly")
                        }
                }finally {
                }
                return@withContext
            }
        }
    }

    fun onSavePhotos() {
        viewModelScope.launch {
            withContext(context = Dispatchers.IO) {
                internalStorageFaceImageRepository.createAndSaveBitmaps(userBitmap = _userBitmap.value)
                Log.i("distrivite-ai-img", "Users face images save successfully")
                _userBitmap.value = UserBitmap("", emptyList())
            }
        }
    }

    private fun onAnalyze(imageProxy: ImageProxy) {
        viewModelScope.launch {
            imageProxy.use { imageProxy ->
                val faceFrame = mlkitFaceDetector.process(imageProxy, isFrontCamera)
                Log.i("distrivite-ai", "Processing imager for registering a new user")
                _faceFrameRecognitionSecureResult.value = FaceFrameRecognitionSecureResult(
                    faceFrame = faceFrame, faceRecognitionSecureResults = emptyList()
                )
            }
        }
    }
}
