package com.datavite.distrivite.presentation.instructorContract

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
import com.ramcosta.composedestinations.generated.destinations.InstructorContractScreenDestination
import com.datavite.distrivite.presentation.ai.FaceOverLay
import com.datavite.distrivite.presentation.contribute.CameraScannerPreview
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

//@ContributeNavGraph
@Destination<RootGraph>
@Composable
fun InstructorContractRegisterFaceScreen(
    navigator: DestinationsNavigator,
    viewModel: InstructorContractViewModel
) {
    val selectedDomainInstructorContract by viewModel.selectedDomainInstructorContract.collectAsState()
    val instructorContractRegistrationState by viewModel.instructorContractRegistrationState.collectAsState()
    val faceFrame by viewModel.faceFrame.collectAsState()

    selectedDomainInstructorContract?.let {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val userBitmaps by viewModel.userBitmap.collectAsState()

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

        when(instructorContractRegistrationState) {
            is InstructorContractRegistrationState.ScanningFace  -> {
                FaceOverLay(faceFrame = faceFrame)
            }
            is InstructorContractRegistrationState.SavingSingleFace -> {
                ProcessingInstructorContractFaceIndicator()
                FaceOverLay(faceFrame = faceFrame)
            }

            is InstructorContractRegistrationState.SavingAllFaces -> {
                val message =(instructorContractRegistrationState as InstructorContractRegistrationState.SavingAllFaces).message
                ProcessingInstructorContractFaceIndicator()
            }

            is InstructorContractRegistrationState.AllFacesSaved -> {
                val message = (instructorContractRegistrationState as InstructorContractRegistrationState.AllFacesSaved).message
                Box(modifier = Modifier.fillMaxSize()) {
                    InstructorContractSuccessAlert()
                }
            }

            is InstructorContractRegistrationState.RegistrationCompleted -> {
                Box(modifier = Modifier.fillMaxSize()){
                    navigator.navigate(InstructorContractScreenDestination){
                        //popUpTo(InstructorContractRegisterFaceScreenDestination.route){ inclusive= true}
                    }
                }
            }

        }

    }

}


@Composable
fun ProcessingInstructorContractFaceIndicator() {
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
fun InstructorContractSuccessAlert() {
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