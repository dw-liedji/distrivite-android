package com.datavite.distrivite.presentation.contribute

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.presentation.components.CameraPreview
import com.datavite.distrivite.presentation.ai.FaceDetectorStreamAnalyser
import com.datavite.distrivite.presentation.ai.PhotoBottomSheetContent
import com.datavite.distrivite.presentation.ai.UserBitmap
import java.util.concurrent.Executors


@Composable
fun CameraScannerPreview(controller: LifecycleCameraController,
                  onImageCaptureCallback: ImageCapture.OnImageCapturedCallback,
                  onCameraToggle: () -> Unit,
                  userBitmaps: UserBitmap,
                  onPhotosSaved : () -> Unit,
                  faceDetectorStreamAnalyser: FaceDetectorStreamAnalyser
) {

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    controller.setImageAnalysisAnalyzer(cameraExecutor, faceDetectorStreamAnalyser)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        CameraPreview(
            controller = controller,
            faceDetectorStreamAnalyser=faceDetectorStreamAnalyser,
            modifier = Modifier
                .fillMaxSize()
        )

        PhotoBottomSheetContent(
            bitmaps = userBitmaps.bitmaps,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.TopCenter),
            onSavePhotos = {
                onPhotosSaved()
            }
        )

        Row (horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)) {
            IconButton(
                onClick = {
                    controller.cameraSelector =
                        if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else CameraSelector.DEFAULT_BACK_CAMERA
                    onCameraToggle()
                },
                modifier = Modifier
                    .offset(16.dp, 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch camera"
                )
            }

            IconButton(
                onClick = {
                    controller.takePicture(cameraExecutor, onImageCaptureCallback)
                },
                modifier = Modifier
                    .offset(16.dp, 16.dp)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Take photo"
                )
            }

        }
    }

    // Properly shutdown the executor when the view is disposed
    DisposableEffect(Unit) {
        onDispose {
            controller.unbind()
            cameraExecutor.shutdown()
        }
    }
}