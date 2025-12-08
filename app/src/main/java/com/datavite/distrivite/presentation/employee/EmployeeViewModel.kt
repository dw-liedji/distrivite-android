package com.datavite.distrivite.presentation.employee

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.presentation.ai.BitmapUtils
import com.datavite.distrivite.presentation.ai.FaceEmbeddingProcessor
import com.datavite.distrivite.presentation.ai.FaceVideoStreamAnalyser
import com.datavite.distrivite.presentation.ai.MlkitFaceDetector
import com.datavite.distrivite.presentation.ai.UserBitmap
import com.datavite.distrivite.presentation.ai.model.FaceFrameRecognitionSecureResult
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.network.NetworkStatusMonitor
import com.datavite.distrivite.domain.model.DomainEmployee
import com.datavite.distrivite.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val networkStatusMonitor: NetworkStatusMonitor,
    private val faceVideoStreamAnalyser: FaceVideoStreamAnalyser,
    private val mlkitFaceDetector: MlkitFaceDetector,
    private val faceEmbeddingProcessor: FaceEmbeddingProcessor,
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager
) : ViewModel() {

    private var isFrontCamera = true

    val authUserFlow = authOrgUserCredentialManager.sharedAuthOrgUserFlow

    // State flow to hold the UserBitmap instance for the current user
    private val _userBitmap = MutableStateFlow<UserBitmap>(UserBitmap("", emptyList()))
    val userBitmap: StateFlow<UserBitmap> = _userBitmap.asStateFlow()

    // State for showing success alert
    private val _showSuccessAlert = MutableStateFlow(false)
    val showSuccessAlert: StateFlow<Boolean> = _showSuccessAlert.asStateFlow()

    private val _employeeRegistrationState = MutableStateFlow<EmployeeRegistrationState>(
        EmployeeRegistrationState.ScanningFace)
    val employeeRegistrationState: StateFlow<EmployeeRegistrationState> = _employeeRegistrationState

    private val _faceFrameRecognitionSecureResult = MutableStateFlow<FaceFrameRecognitionSecureResult?>(null)
    val faceFrameRecognitionSecureResult = _faceFrameRecognitionSecureResult.asStateFlow()

    // State to hold the currently selected user for facial recognition
    private val _selectedDomainEmployee = MutableStateFlow<DomainEmployee?>(null)
    val selectedDomainEmployee: StateFlow<DomainEmployee?> = _selectedDomainEmployee

    private val _employeesUiState = MutableStateFlow<EmployeesUiState>(EmployeesUiState.Loading)
    val employeesUiState: StateFlow<EmployeesUiState> = _employeesUiState

    // State to hold the currently selected user for facial recognition
    private val _orgOrgUser = MutableStateFlow<AuthOrgUser?>(null)
    val orgOrgUser: StateFlow<AuthOrgUser?> = _orgOrgUser

    // Initialize the LifecycleCameraController in the ViewModel
    private val _cameraController = MutableLiveData<LifecycleCameraController?>()

    init {
        Log.d("ContributeViewModel", "Initialized")
        observeLocalEmployeesData()
        observeOrganization()
    }


    fun getCameraController(context: Context): LifecycleCameraController {
        return _cameraController.value ?: LifecycleCameraController(context).apply {
            // Set initial configurations
            imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
            imageAnalysisOutputImageFormat = ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS
            )
            _cameraController.value = this
        }
    }

    // Unbind and clear the controller when no longer needed
    fun clearCameraController() {
        _cameraController.value?.unbind()
        _cameraController.value = null
    }

    private fun observeLocalEmployeesData() {
        viewModelScope.launch {
            employeeRepository.getEmployeesFlow()
                .catch { error ->
                    _employeesUiState.value = EmployeesUiState.Error(error.message ?: "Unknown Error")
                }
                .collect { employees ->
                    _employeesUiState.value = EmployeesUiState.Success(employees = employees)
                }
        }
    }

    private fun observeOrganization() = viewModelScope.launch(Dispatchers.IO) {
        authUserFlow.collectLatest { orgOrgUser ->
            orgOrgUser?.let {
                Log.i("distrivite_first_comsummer","token changed from cosumer employee ${it.orgSlug}")
                _orgOrgUser.value = it
                observeNetworkStateAndSyncLocalData(it)
            }
        }
    }

    fun toggleCamera() {
        isFrontCamera = !isFrontCamera
    }
    fun getFaceVideoStreamAnalyser() = faceVideoStreamAnalyser
    fun getImageCapturedCallback() : ImageCapture.OnImageCapturedCallback = imageCapturedCallback

    private val imageCapturedCallback =  object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(imageProxy: ImageProxy) {
            super.onCaptureSuccess(imageProxy)
            this@EmployeeViewModel.onTakePhoto(imageProxy)
        }

        override fun onError(exception: ImageCaptureException) {
            super.onError(exception)
            Log.e("Camera", "Couldn't take photo: ", exception)
        }
    }

    // Function to add a photo for the current user
    fun onTakePhoto(imageProxy: ImageProxy)  {
        viewModelScope.launch {
            _employeeRegistrationState.value = EmployeeRegistrationState.SavingSingleFace
            withContext(context = Dispatchers.Default) {
                try {
                    val faceFrame = mlkitFaceDetector.process(imageProxy, isFrontCamera)
                    if (faceFrame.faces.isNotEmpty()){
                        val croppedBitmap = BitmapUtils.cropRectFromBitmap(faceFrame.bitmap, faceFrame.faces[0].boundingBox)
                        val currentUserBitmap = _userBitmap.value
                        //if (currentUserBitmap.userName.isNotEmpty()) {
                            val updatedBitmaps = currentUserBitmap.bitmaps.toMutableList()
                            updatedBitmaps.add(croppedBitmap)
                            _userBitmap.value = currentUserBitmap.copy(bitmaps = updatedBitmaps)
                        //} else {}
                    }else {
                        Log.i("distrivite-ai-img", "Image not save missing face, Please place your face correctly")
                    }
                }finally {
                    _employeeRegistrationState.value = EmployeeRegistrationState.ScanningFace
                }
                return@withContext
            }
        }
    }

    fun onSavePhotos() {
        _selectedDomainEmployee.value?.let {
            viewModelScope.launch {
                withContext(context = Dispatchers.IO) {
                    _employeeRegistrationState.value = EmployeeRegistrationState.SavingAllFaces("Saving faces...")
                    //internalStorageFaceImageRepository.createAndSaveBitmaps(userBitmap = _userBitmap.value)
                    val embeddings = convertUserBitmapToEmbedding()
                    updateEmployee(it.orgSlug, it.copy(embeddings = convertToListOfLists(embeddings)))
                    Log.i("distrivite-ai-employee-face", "Users face images save successfully")
                    _userBitmap.value = UserBitmap("", emptyList())
                    _employeeRegistrationState.value = EmployeeRegistrationState.AllFacesSaved("Faces processed successfully")
                    delay(2000)
                    _employeeRegistrationState.value = EmployeeRegistrationState.RegistrationCompleted
                }
            }
        }

    }

    fun onRegisterFace(domainEmployee: DomainEmployee) {
        selectDomainEmployee(domainEmployee)
        _employeeRegistrationState.value = EmployeeRegistrationState.ScanningFace
    }

    fun onDeleteEmployeeEmbeddings(domainEmployee: DomainEmployee) {
        selectDomainEmployee(domainEmployee)
        _selectedDomainEmployee.value?.let {
            viewModelScope.launch {
                withContext(context = Dispatchers.IO) {
                    updateEmployee(it.orgSlug, it.copy(embeddings = emptyList()))
                    Log.i("distrivite-ai-employee-img", "Employees face images save successfully")
                    _userBitmap.value = UserBitmap("", emptyList())
                    // Trigger success alert
                    _showSuccessAlert.value = true
                    // Hide alert after 2 seconds
                    delay(2000)
                    _showSuccessAlert.value = false
                }
                //navigator.navigateUp()
            }
        }
    }

    private fun onAnalyze(imageProxy: ImageProxy) {
        viewModelScope.launch {
            imageProxy.use { imageProxy ->
                val faceFrame = mlkitFaceDetector.process(imageProxy, isFrontCamera)
                Log.i("distrivite-ai", "Processing imager for registering a new user")
                _faceFrameRecognitionSecureResult.value = FaceFrameRecognitionSecureResult(
                    faceFrame=faceFrame, faceRecognitionSecureResults = emptyList())
            }
        }
    }

    private fun selectDomainEmployee(domainEmployee: DomainEmployee) {
        _selectedDomainEmployee.value = domainEmployee
        Log.i("distrivite-employee-selection", "selection is ${_selectedDomainEmployee.value?.name}")
        faceVideoStreamAnalyser.setOnImageStreamAnalyse(this::onAnalyze)
    }

    fun createUser(orgOrgUser: AuthOrgUser, user: DomainEmployee) {
        viewModelScope.launch {
            employeeRepository.createEmployee(orgOrgUser.orgSlug, user)
        }
    }

    private fun updateEmployee(organization: String, user: DomainEmployee) {
        viewModelScope.launch {
            employeeRepository.updateEmployee(organization, user)
        }
    }

    fun deleteUser(organization: String, user: DomainEmployee) {
        viewModelScope.launch {
            employeeRepository.deleteEmployee(organization, user)
        }
    }

    private suspend fun observeNetworkStateAndSyncLocalData(authOrgUser: AuthOrgUser) {
        networkStatusMonitor.isConnected.collect { isConnected ->
            if (isConnected) {
                syncLocalDataWithServer(authOrgUser.orgSlug)
            }
        }

    }

    private fun syncLocalDataWithServer(organization: String) {
        viewModelScope.launch(Dispatchers.IO) {
            //_employeesUiState.value = EmployeesUiState.Loading
            try {
                employeeRepository.syncEmployees(organization)
            } catch (e: Exception) {
               // _employeesUiState.value = EmployeesUiState.Error(e.message ?: "Sync Failed")
            }
        }
    }

    private fun convertUserBitmapToEmbedding() : List<FloatArray>{
        Log.i("distrivite-ai-img", "starting processing bitmaps")

        val embeddings = mutableListOf<FloatArray>()
        for (bitmap in _userBitmap.value.bitmaps){
            embeddings.add(faceEmbeddingProcessor.process(bitmap))
        }
        Log.i("distrivite-ai-img", "processing bitmaps finished")
        return embeddings
    }

    private fun convertToListOfLists(floatArrayList: List<FloatArray>): List<List<Float>> {
        return floatArrayList.map { it.toList() }
    }

}