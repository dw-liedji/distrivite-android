package com.datavite.distrivite.presentation.ai.model

data class FaceFrameRecognitionSecureResult(
    val faceFrame: FaceFrame,
    val faceRecognitionSecureResults: List<FaceRecognitionSecureResult>
)
