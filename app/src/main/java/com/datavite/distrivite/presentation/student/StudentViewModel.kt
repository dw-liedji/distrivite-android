package com.datavite.distrivite.presentation.student

import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.presentation.ai.BitmapUtils
import com.datavite.distrivite.presentation.ai.FaceDetectorStreamAnalyser
import com.datavite.distrivite.presentation.ai.FaceEmbeddingProcessor
import com.datavite.distrivite.presentation.ai.MlkitFaceDetector
import com.datavite.distrivite.presentation.ai.UserBitmap
import com.datavite.distrivite.presentation.ai.model.FaceFrame
import com.datavite.distrivite.presentation.ai.model.FaceFrameRecognitionSecureResult
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.network.NetworkStatusMonitor
import com.datavite.distrivite.domain.model.DomainStudent
import com.datavite.distrivite.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val networkStatusMonitor: NetworkStatusMonitor,
    private val faceDetectorStreamAnalyser: FaceDetectorStreamAnalyser,
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

    // State for showing success alert
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _studentRegistrationState = MutableStateFlow<StudentRegistrationState>(
        StudentRegistrationState.ScanningFace)
    val studentRegistrationState: StateFlow<StudentRegistrationState> = _studentRegistrationState

    private val _faceFrameRecognitionSecureResult = MutableStateFlow<FaceFrameRecognitionSecureResult?>(null)
    val faceFrameRecognitionSecureResult = _faceFrameRecognitionSecureResult.asStateFlow()

    private val _faceFrame = MutableStateFlow<FaceFrame?>(null)
    val faceFrame = _faceFrame.asStateFlow()

    private val processingMutex = Mutex() // Mutex for managing single image processing

    // State to hold the currently selected user for facial recognition
    private val _selectedDomainStudent = MutableStateFlow<DomainStudent?>(null)
    val selectedDomainStudent: StateFlow<DomainStudent?> = _selectedDomainStudent

    private val _studentsUiState = MutableStateFlow<StudentsUiState>(StudentsUiState.Loading)
    val studentsUiState: StateFlow<StudentsUiState> = _studentsUiState

    // State to hold the currently selected user for facial recognition
    private val _authOrgUser = MutableStateFlow<AuthOrgUser?>(null)
    val authOrgUser: StateFlow<AuthOrgUser?> = _authOrgUser

    // Initialize the LifecycleCameraController in the ViewModel
    private val _cameraController = MutableLiveData<LifecycleCameraController?>()

    init {
        Log.d("ContributeViewModel", "Initialized")
        observeLocalStudentsData()
        observeOrganization()
        observeVideoStreamAnalyserFaceFrame()
    }

    // Unbind and clear the controller when no longer needed
    fun clearCameraController() {
        _cameraController.value?.unbind()
        _cameraController.value = null
    }

    private fun observeLocalStudentsData() {
        viewModelScope.launch {
            studentRepository.getDomainStudentsFlow()
                .catch { error ->
                    _studentsUiState.value = StudentsUiState.Error(error.message ?: "Unknown Error")
                }
                .collect { domainStudents ->
                    _studentsUiState.value = StudentsUiState.Success(domainStudents = domainStudents)
                }
        }
    }

    private fun observeOrganization() = viewModelScope.launch(Dispatchers.IO) {
        authUserFlow.collectLatest { authOrgUser ->
            authOrgUser?.let {
                Log.i("distrivite_first_comsummer","token changed from cosumer student ${it.orgSlug}")
                _authOrgUser.value = it
                observeNetworkStateAndSyncLocalData(it)
            }
        }
    }

    fun toggleCamera() {
        isFrontCamera = !isFrontCamera
    }
    fun getFaceVideoStreamAnalyser() = faceDetectorStreamAnalyser
    fun getImageCapturedCallback() : ImageCapture.OnImageCapturedCallback = imageCapturedCallback



    private fun observeVideoStreamAnalyserFaceFrame() {
        viewModelScope.launch {
            faceDetectorStreamAnalyser.faceFrameFlow.collectLatest {
                _faceFrame.value = it

                it?.let {
                    viewModelScope.launch {
                        processingMutex.withLock {
                            //recognizeFaceFrame(it)
                            Log.i("distrivite-ai", "Processing imager for registering a new user")
                            _faceFrameRecognitionSecureResult.value = FaceFrameRecognitionSecureResult(
                                faceFrame=it, faceRecognitionSecureResults = emptyList())
                        }
                    }
                }
            }
        }
    }

    private val imageCapturedCallback =  object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(imageProxy: ImageProxy) {
            super.onCaptureSuccess(imageProxy)
            this@StudentViewModel.onTakePhoto(imageProxy)
        }

        override fun onError(exception: ImageCaptureException) {
            super.onError(exception)
            Log.e("Camera", "Couldn't take photo: ", exception)
        }
    }

    // Function to add a photo for the current user
    fun onTakePhoto(imageProxy: ImageProxy)  {
        viewModelScope.launch {
            _studentRegistrationState.value = StudentRegistrationState.SavingSingleFace
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
                    _studentRegistrationState.value = StudentRegistrationState.ScanningFace
                }
                return@withContext
            }
        }
    }

    fun onSavePhotos() {
        _selectedDomainStudent.value?.let {
            viewModelScope.launch {
                withContext(context = Dispatchers.IO) {
                    _studentRegistrationState.value = StudentRegistrationState.SavingAllFaces("Saving faces...")
                    //internalStorageFaceImageRepository.createAndSaveBitmaps(userBitmap = _userBitmap.value)
                    val embeddings = convertUserBitmapToEmbedding()
                    updateStudent(it.orgSlug, it.copy(embeddings = convertToListOfLists(embeddings)))
                    Log.i("distrivite-ai-student-face", "Users face images save successfully")
                    _userBitmap.value = UserBitmap("", emptyList())
                    _studentRegistrationState.value = StudentRegistrationState.AllFacesSaved("Faces processed successfully")
                    delay(2000)
                    _studentRegistrationState.value = StudentRegistrationState.RegistrationCompleted
                }
            }
        }

    }

    fun onRegisterFace(domainStudent: DomainStudent) {
        selectDomainStudent(domainStudent)
        _studentRegistrationState.value = StudentRegistrationState.ScanningFace
    }

    fun onDeleteStudentEmbeddings(domainStudent: DomainStudent) {
        selectDomainStudent(domainStudent)
        _selectedDomainStudent.value?.let {
            viewModelScope.launch {
                withContext(context = Dispatchers.IO) {
                    updateStudent(it.orgSlug, it.copy(embeddings = emptyList()))
                    Log.i("distrivite-ai-student-img", "Students face images save successfully")
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

    fun onRefresh() {
        authOrgUser.value?.let {
            syncLocalDataWithServer(it.orgSlug)
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

    private fun selectDomainStudent(domainStudent: DomainStudent) {
        _selectedDomainStudent.value = domainStudent
        Log.i("distrivite-student-selection", "selection is ${_selectedDomainStudent.value?.name}")
        //faceVideoStreamAnalyser.setOnImageStreamAnalyse(this::onAnalyze)
    }

    fun createUser(authOrgUser: AuthOrgUser, user: DomainStudent) {
        viewModelScope.launch {
            studentRepository.createDomainStudent(authOrgUser.orgSlug, user)
        }
    }

    private fun updateStudent(organization: String, user: DomainStudent) {
        viewModelScope.launch {
            studentRepository.updateDomainStudent(organization, user)
        }
    }

    fun deleteDomainUser(organization: String, user: DomainStudent) {
        viewModelScope.launch {
            studentRepository.deleteDomainStudent(organization, user)
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
        viewModelScope.launch() {
            _isRefreshing.value = true
            withContext(Dispatchers.IO){
                //_studentsUiState.value = StudentsUiState.Loading
                try {
                    studentRepository.syncLocalWithRemoteStudents(organization)
                } catch (e: Exception) {
                    // _studentsUiState.value = StudentsUiState.Error(e.message ?: "Sync Failed")
                }
            }
            _isRefreshing.value = false

        }
    }

    fun onSearchQueryChanged(searchQuery:String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val searchedStudents = studentRepository.searchDomainStudentsFor(searchQuery)
                    _studentsUiState.value = StudentsUiState.Success(searchedStudents)
                    Log.i("distrivite-search", "search completed!")
                }catch (e:Exception) {
                    _studentsUiState.value = StudentsUiState.Error("")
                }
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