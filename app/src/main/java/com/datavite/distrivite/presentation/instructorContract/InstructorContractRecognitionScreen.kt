package com.datavite.distrivite.presentation.instructorContract


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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import com.ramcosta.composedestinations.generated.destinations.SessionReportScreenDestination
import com.datavite.distrivite.presentation.ai.FaceOverLay
import com.datavite.distrivite.presentation.ai.FaceOverLayResult
import com.datavite.distrivite.presentation.session.EnsureGPSIsEnabled
import com.datavite.distrivite.presentation.session.GPSFailureDialog
import com.datavite.distrivite.presentation.session.TeachingSessionCameraPreview
import com.datavite.distrivite.presentation.student.ProcessingFaceIndicator
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

//@TeachingNavGraph
@Destination<RootGraph>
@Composable
fun InstructorContractRecognitionScreen(
    navigator: DestinationsNavigator,
    viewModel: InstructorContractViewModel
) {
    val context = LocalContext.current
    val instructorContractValidationState by viewModel.instructorContractValidationState.collectAsState()

    val faceFrameRecognitionSecureResult by viewModel.faceFrameRecognitionSecureResult.collectAsState()
    val faceFrame by viewModel.faceFrame.collectAsState()
    val recognitionFinished by viewModel.recognitionFinished.collectAsState()
    val authOrgUser by viewModel.authOrgUser.collectAsState()

    LaunchedEffect(context, recognitionFinished){
        if (recognitionFinished) {
            navigator.navigate(SessionReportScreenDestination()){
                //popUpTo(InstructorContractRecognitionScreenDestination.route){inclusive= true }
            }
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

        when(instructorContractValidationState) {
            is InstructorContractValidationState.FaceScanning  -> {
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


                DisposableEffect(Unit) {
                    // Bind camera to lifecycle owner
                    controller.bindToLifecycle(lifecycleOwner)

                    onDispose {
                        controller.unbind() // Ensure controller is unbound when the composable is removed
                    }
                }

                TeachingSessionCameraPreview(
                    controller = controller,
                    faceVideoStreamAnalyser = viewModel.getFaceVideoStreamAnalyser(),
                    modifier = Modifier
                        .fillMaxSize()
                )

                FaceOverLay(faceFrame = faceFrame)
                /*
                FaceOverLayResult(
                    faceFrameRecognitionSecureResult = faceFrameRecognitionSecureResult,
                    showSuccessCheckMark = false)
                 */
            }
            is InstructorContractValidationState.GPSResultProcessing -> {
                ProcessingFaceIndicator()
                FaceOverLayResult(
                    faceFrameRecognitionSecureResult = faceFrameRecognitionSecureResult,
                    showSuccessCheckMark = false)
            }

            is InstructorContractValidationState.GPSResultInsideOrganization -> {
                val message =(instructorContractValidationState as InstructorContractValidationState.GPSResultInsideOrganization).message
                FaceOverLayResult(
                    faceFrameRecognitionSecureResult = faceFrameRecognitionSecureResult,
                    showSuccessCheckMark = true)
            }

            is InstructorContractValidationState.GPSResultOutsideOrganization -> {
                val action = (instructorContractValidationState as InstructorContractValidationState.GPSResultOutsideOrganization).sessionAction


                Box(modifier = Modifier.fillMaxSize()){
                    GPSFailureDialog(
                        onRetry = {
                            //viewModel.onRetrySessionItemAction(action)
                        },
                        onCancel = {
                            navigator.navigate(SessionReportScreenDestination){
                                //popUpTo(InstructorContractRecognitionScreenDestination.route){inclusive= true}
                            }
                        }
                    )
                }
            }

            is InstructorContractValidationState.GPSResultError -> {
                val action = (instructorContractValidationState as InstructorContractValidationState.GPSResultError).sessionAction

                Box(modifier = Modifier.fillMaxSize()){
                    GPSFailureDialog(
                        onRetry = {
                            //viewModel.onRetrySessionItemAction(action)
                        },
                        onCancel = {
                            navigator.navigate(SessionReportScreenDestination){
                                //popUpTo(InstructorContractRecognitionScreenDestination.route){inclusive= true}
                            }
                        }
                    )
                }
            }

            else -> {}
        }

        /*
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
                    navigator.navigate(RegisterFaceScreenDestination())
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
         */
    }


    authOrgUser?.let {
        if (it.isGPSActive) {
            // Call the composable to ensure GPS is enabled
            EnsureGPSIsEnabled(
                onGPSEnabled = {
                },
                onGPSDisabled = { errorMessage ->
                },
                context = context
            )
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

