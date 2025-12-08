package com.datavite.distrivite.presentation.ai.model

import com.google.mlkit.vision.face.Face

// The result of a recognition process
data class FaceRecognitionResult(
    val id:String,
    val name: String,
    val isUnknownFace:Boolean = false,
    val confidence: Float,
    val face:Face
)

