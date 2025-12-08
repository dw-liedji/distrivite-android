package com.datavite.distrivite.presentation.holiday

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.datavite.distrivite.presentation.ai.FaceDetectorStreamAnalyser
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun HolidayCameraPreview(
    controller: LifecycleCameraController,
    faceVideoStreamAnalyser: FaceDetectorStreamAnalyser,
    modifier: Modifier = Modifier
) {
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    controller.setImageAnalysisAnalyzer( cameraExecutor, faceVideoStreamAnalyser )

    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.controller = controller
            }
        },
        update = {view ->
            view.controller = controller
        },

        onRelease = {
            controller.unbind()
        },
        onReset = { controller.unbind() },
        modifier = modifier
    )
}