package com.datavite.distrivite.presentation.ai

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class FaceVideoStreamAnalyser  (
) : ImageAnalysis.Analyzer {

    private var onImageStreamAnalyse: ((imageProxy: ImageProxy) -> Unit)? = null
    private var frameSkipCounter = 0

    fun setOnImageStreamAnalyse(onImageStreamAnalyse: (imageProxy: ImageProxy) -> Unit) {
        this.onImageStreamAnalyse = onImageStreamAnalyse
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (frameSkipCounter % 5 == 0) { //20 frame same to perform better on my techno k8
            // Process every 60th frame or adjust based on performance needs
            if (imageProxy.image != null){
                onImageStreamAnalyse?.let {
                    it(imageProxy)
                }
            }else {
                imageProxy.close()
            }
        } else {
            imageProxy.close() // Ensure image is closed to avoid memory leaks
        }
        frameSkipCounter++

    }
}
