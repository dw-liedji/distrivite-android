package com.datavite.distrivite.presentation.student

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.generated.destinations.StudentScreenDestination
import com.datavite.distrivite.presentation.ai.FaceOverLayResult
import com.datavite.distrivite.presentation.contribute.CameraScannerPreview
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

//@ContributeNavGraph
@Destination<RootGraph>
@Composable
fun StudentRegisterFaceScreen(
    navigator: DestinationsNavigator,
    viewModel: StudentViewModel
) {
    val selectedDomainStudent by viewModel.selectedDomainStudent.collectAsState()
    val showSuccessAlert by viewModel.showSuccessAlert.collectAsState()
    val studentRegistrationState by viewModel.studentRegistrationState.collectAsState()

    selectedDomainStudent?.let {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val userBitmaps by viewModel.userBitmap.collectAsState()
        val faceFrameRecognitionSecureResult by viewModel.faceFrameRecognitionSecureResult.collectAsState()

        val controller = remember {
            LifecycleCameraController(context).apply {
                imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                imageAnalysisOutputImageFormat = ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
                // Initialize with front camera
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                setEnabledUseCases(
                    CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS
                )
            }
        }

        // Handle camera lifecycle and executor cleanup
        DisposableEffect(Unit) {
            controller.bindToLifecycle(lifecycleOwner)
            onDispose {
                controller.unbind() // Ensure controller is unbound when the composable is removed
            }
        }

        CameraScannerPreview(
            controller = controller,
            onCameraToggle = {
                viewModel.toggleCamera()
            },
            onImageCaptureCallback = viewModel.getImageCapturedCallback(),
            userBitmaps = userBitmaps,
            onPhotosSaved = {
                viewModel.onSavePhotos()
            },
            faceDetectorStreamAnalyser = viewModel.getFaceVideoStreamAnalyser()
        )

        when(studentRegistrationState) {
            is StudentRegistrationState.ScanningFace  -> {
                FaceOverLayResult(
                    faceFrameRecognitionSecureResult = faceFrameRecognitionSecureResult,
                    showSuccessCheckMark = false)
            }
            is StudentRegistrationState.SavingSingleFace -> {
                ProcessingFaceIndicator()
                FaceOverLayResult(
                    faceFrameRecognitionSecureResult = faceFrameRecognitionSecureResult,
                    showSuccessCheckMark = true)
            }

            is StudentRegistrationState.SavingAllFaces -> {
                val message =(studentRegistrationState as StudentRegistrationState.SavingAllFaces).message
                ProcessingFaceIndicator()
            }

            is StudentRegistrationState.AllFacesSaved -> {
                val message = (studentRegistrationState as StudentRegistrationState.AllFacesSaved).message
                Box(modifier = Modifier.fillMaxSize()) {
                    SuccessAlert()
                }
            }

            is StudentRegistrationState.RegistrationCompleted -> {
                Box(modifier = Modifier.fillMaxSize()){
                    navigator.navigate(StudentScreenDestination){
                       // popUpTo(StudentRegisterFaceScreenDestination.route){inclusive= true}
                    }
                }
            }

        }

    }

}


@Composable
fun ProcessingFaceIndicator() {
    // Loading State: Show a circular progress indicator
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun SuccessAlert() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Photos saved successfully!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}