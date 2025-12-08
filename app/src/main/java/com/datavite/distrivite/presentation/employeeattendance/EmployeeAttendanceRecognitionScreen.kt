package com.datavite.distrivite.presentation.employeeattendance


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ramcosta.composedestinations.generated.destinations.EmployeeAttendanceScreenDestination
import com.datavite.distrivite.presentation.ai.FaceOverLay
import com.datavite.distrivite.presentation.ai.FaceOverLayResult
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

//@TeachingNavGraph
@Destination<RootGraph>
@Composable
fun EmployeeAttendanceRecognitionScreen(
    navigator: DestinationsNavigator,
    viewModel: EmployeeAttendanceViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    // State to track the camera direction
    val isFrontCamera = remember { mutableStateOf(true) }

    val controller = remember {
        LifecycleCameraController(context).apply {

            imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
            imageAnalysisOutputImageFormat = ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888

            // Initialize with front camera
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.IMAGE_ANALYSIS // Ensure IMAGE_ANALYSIS is enabled
            )
        }
    }

    val faceFrameRecognitionSecureResult by viewModel.faceFrameRecognitionSecureResult.collectAsState()
    val faceFrame by viewModel.faceFrame.collectAsState()
    val showSuccessResult by viewModel.showSuccessResult.collectAsState()
    val recognitionFinished by viewModel.recognitionFinished.collectAsState()

    LaunchedEffect(context, recognitionFinished){
        if (recognitionFinished) {
            navigator.navigate(EmployeeAttendanceScreenDestination){
                //popUpTo(EmployeeAttendanceRecognitionScreenDestination.route){inclusive= true}
            }
        }
    }

    DisposableEffect(Unit) {
        // Bind camera to lifecycle owner
        controller.bindToLifecycle(lifecycleOwner)

        onDispose {
            controller.unbind() // Ensure controller is unbound when the composable is removed
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val currentView = LocalView.current

        DisposableEffect(Unit) {
            currentView.keepScreenOn = true
            onDispose {
                currentView.keepScreenOn = false
            }
        }
        EmployeeAttendanceCameraPreview(
            controller = controller,
            faceVideoStreamAnalyser = viewModel.getVideoStreamAnalyser(),
            modifier = Modifier
                .fillMaxSize()
        )

        if(showSuccessResult) FaceOverLayResult(faceFrameRecognitionSecureResult=faceFrameRecognitionSecureResult) else FaceOverLay(faceFrame = faceFrame)

        IconButton(
            onClick = {
                isFrontCamera.value = !isFrontCamera.value
                controller.cameraSelector =
                    if (isFrontCamera.value) CameraSelector.DEFAULT_FRONT_CAMERA
                    else CameraSelector.DEFAULT_BACK_CAMERA
                viewModel.toggleCamera()
            },
            modifier = Modifier
                .offset(16.dp, 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = "Switch camera"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(
                onClick = {
                    //navigator.navigate(RegisterFaceScreenDestination())
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Add new face"
                )
            }
            IconButton(
                onClick = {
                    scope.launch {
                        TODO("IMPLEMENT REFRESH KNOWN FACES")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = "Refresh known faces"
                )
            }
            IconButton(
                onClick = {
                    takePhoto(
                        controller = controller,
                        onPhotoTaken = viewModel::onTakePhoto,
                        context
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Take photo"
                )
            }
        }
    }

}


private fun takePhoto(
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    context: Context
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )

                onPhotoTaken(rotatedBitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}

