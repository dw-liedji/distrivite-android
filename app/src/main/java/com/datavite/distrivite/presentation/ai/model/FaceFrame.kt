package com.datavite.distrivite.presentation.ai.model

import android.graphics.Bitmap
import com.google.mlkit.vision.face.Face

data class FaceFrame(
    val bitmap: Bitmap,
    val rotation:Int,
    val faces: List<Face>
)

