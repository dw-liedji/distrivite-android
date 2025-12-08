package com.datavite.distrivite.presentation.instructorContract

import android.content.Context
import android.location.Location
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
import com.datavite.distrivite.presentation.ai.FaceDetectorStreamAnalyser
import com.datavite.distrivite.presentation.ai.FaceEmbeddingProcessor
import com.datavite.distrivite.presentation.ai.FaceRecognitionSecurePipeLine
import com.datavite.distrivite.presentation.ai.MlkitFaceDetector
import com.datavite.distrivite.presentation.ai.UserBitmap
import com.datavite.distrivite.presentation.ai.model.FaceFrame
import com.datavite.distrivite.presentation.ai.model.FaceFrameRecognitionSecureResult
import com.datavite.distrivite.presentation.ai.model.FaceRecognitionSecureResult
import com.datavite.distrivite.presentation.ai.model.KnownFace
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.location.LocationManager
import com.datavite.distrivite.data.network.NetworkStatusMonitor
import com.datavite.distrivite.domain.model.DomainInstructorContract
import com.datavite.distrivite.domain.repository.InstructorContractRepository
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
class InstructorContractViewModel @Inject constructor(
    private val instructorContractRepository: InstructorContractRepository,
    private val networkStatusMonitor: NetworkStatusMonitor,
    private val mlkitFaceDetector: MlkitFaceDetector,
    private val faceDetectorStreamAnalyser: FaceDetectorStreamAnalyser,
    private val faceEmbeddingProcessor: FaceEmbeddingProcessor,
    private val locationManager: LocationManager,
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager,
    private val faceRecognitionSecurePipeLine: FaceRecognitionSecurePipeLine,
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
    private val _sessionReportUrl = MutableStateFlow("")
    val sessionReportUrl: StateFlow<String> = _sessionReportUrl.asStateFlow()

    private val instructorContractKnownFaces = mutableListOf<KnownFace>()

    // State for showing success alert
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _instructorContractRegistrationState = MutableStateFlow<InstructorContractRegistrationState>(
        InstructorContractRegistrationState.ScanningFace)
    val instructorContractRegistrationState: StateFlow<InstructorContractRegistrationState> = _instructorContractRegistrationState

    private val _faceFrameRecognitionSecureResult = MutableStateFlow<FaceFrameRecognitionSecureResult?>(null)
    val faceFrameRecognitionSecureResult = _faceFrameRecognitionSecureResult.asStateFlow()

    private val _faceFrame = MutableStateFlow<FaceFrame?>(null)
    val faceFrame = _faceFrame.asStateFlow()

    private val processingMutex = Mutex() // Mutex for managing single image processing

    // State for showing success alert
    private val _recognitionFinished = MutableStateFlow(false)
    val recognitionFinished: StateFlow<Boolean> = _recognitionFinished.asStateFlow()

    private val _instructorContractValidationState = MutableStateFlow<InstructorContractValidationState>(
        InstructorContractValidationState.FaceScanning)
    val instructorContractValidationState: StateFlow<InstructorContractValidationState> = _instructorContractValidationState


    private val _instructorContractAction = MutableStateFlow<InstructorContractAction?>(null)
    val instructorContractAction: StateFlow<InstructorContractAction?> = _instructorContractAction

    // State to hold the currently selected user for facial recognition
    private val _selectedDomainInstructorContract = MutableStateFlow<DomainInstructorContract?>(null)
    val selectedDomainInstructorContract: StateFlow<DomainInstructorContract?> = _selectedDomainInstructorContract

    private val _instructorContractsUiState = MutableStateFlow<InstructorContractsUiState>(InstructorContractsUiState.Loading)
    val instructorContractsUiState: StateFlow<InstructorContractsUiState> = _instructorContractsUiState

    // State to hold the currently selected user for facial recognition
    private val _authOrgUser = MutableStateFlow<AuthOrgUser?>(null)
    val authOrgUser: StateFlow<AuthOrgUser?> = _authOrgUser

    // Initialize the LifecycleCameraController in the ViewModel
    private val _cameraController = MutableLiveData<LifecycleCameraController?>()

    init {
        Log.d("ContributeViewModel", "Initialized")
        observeLocalInstructorContractsData()
        observeOrganization()
        observeVideoStreamAnalyserFaceFrame()
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

    private fun observeLocalInstructorContractsData() {
        viewModelScope.launch {
            instructorContractRepository.getDomainInstructorContractsFlow()
                .catch { error ->
                    _instructorContractsUiState.value = InstructorContractsUiState.Error(error.message ?: "Unknown Error")
                }
                .collect { domainInstructorContracts ->
                    Log.i("eat-instructors", "domainInstructorContracts ${domainInstructorContracts.size}")
                    _instructorContractsUiState.value = InstructorContractsUiState.Success(domainInstructorContracts = domainInstructorContracts)
                    updateKnownFacesFromInstructorContracts(domainInstructorContracts)
                }
        }
    }



    private fun observeOrganization() = viewModelScope.launch(Dispatchers.IO) {
        authUserFlow.collectLatest { authOrgUser ->
            authOrgUser?.let {
                Log.i("distrivite_first_comsummer","token changed from cosumer instructorContract ${it.orgSlug}")
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
                            recognizeFaceFrame(it)
                        }
                    }
                }
            }
        }
    }

    private val imageCapturedCallback =  object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(imageProxy: ImageProxy) {
            super.onCaptureSuccess(imageProxy)
            this@InstructorContractViewModel.onTakePhoto(imageProxy)
        }

        override fun onError(exception: ImageCaptureException) {
            super.onError(exception)
            Log.e("Camera", "Couldn't take photo: ", exception)
        }
    }

    // Function to add a photo for the current user
    fun onTakePhoto(imageProxy: ImageProxy)  {
        viewModelScope.launch {
            _instructorContractRegistrationState.value = InstructorContractRegistrationState.SavingSingleFace
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
                    _instructorContractRegistrationState.value = InstructorContractRegistrationState.ScanningFace
                }
                return@withContext
            }
        }
    }

    fun onSavePhotos() {
        _selectedDomainInstructorContract.value?.let {
            viewModelScope.launch {
                withContext(context = Dispatchers.IO) {
                    _instructorContractRegistrationState.value = InstructorContractRegistrationState.SavingAllFaces("Saving faces...")
                    //internalStorageFaceImageRepository.createAndSaveBitmaps(userBitmap = _userBitmap.value)
                    val embeddings = convertUserBitmapToEmbedding()
                    updateInstructorContract(it.orgSlug, it.copy(embeddings = convertToListOfLists(embeddings)))
                    Log.i("distrivite-ai-instructorContract-face", "Users face images save successfully")
                    _userBitmap.value = UserBitmap("", emptyList())
                    _instructorContractRegistrationState.value = InstructorContractRegistrationState.AllFacesSaved("Faces processed successfully")
                    delay(2000)
                    _instructorContractRegistrationState.value = InstructorContractRegistrationState.RegistrationCompleted
                }
            }
        }

    }

    private fun updateKnownFacesFromInstructorContracts(domainInstructorContracts:List<DomainInstructorContract>) {
        instructorContractKnownFaces.clear()
        for(domainInstructorContract in domainInstructorContracts){
            val knownFace = KnownFace(id = domainInstructorContract.id, domainInstructorContract.name, embeddings = convertToFloatArrayList(domainInstructorContract.embeddings))
            Log.i("distrivite-ai", "$knownFace loaded successfully")
            instructorContractKnownFaces.add(knownFace)
        }
    }

    private suspend fun recognizeFaceFrame(faceFrame: FaceFrame) {
        Log.i("distrivite-recognizeFaceFrame", "recognizeFaceFrame() called")

        _instructorContractAction.value?.let {
            // update teaching session on the server
            //updateInstructorContractForSelected()
            withContext(Dispatchers.IO){
                when (it) {
                    InstructorContractAction.REPORT -> {
                        Log.i("distrivite-ai-session-started", "Success: You are in the organization")
                        val recognitionResult = getInstructorContractFaceRecognitionResultForFrame(faceFrame)
                        val faceRecognitionSecureResults = recognitionResult.faceRecognitionSecureResults
                        if (faceRecognitionSecureResults.isNotEmpty()) {
                            val result = faceRecognitionSecureResults[0]
                            _faceFrameRecognitionSecureResult.value = recognitionResult

                            if (!result.faceRecognitionResult.isUnknownFace && result.isFaceMatchWithSelectedItem) {
                                _authOrgUser.value?.let {
                                    val instructorContractResponse = instructorContractRepository.getDomainInstructorContractById(result.faceRecognitionResult.id)
                                    instructorContractResponse?.let { instructorContract ->
                                        val expectedLocation = getCheckGPSLocationForInstructorContract(instructorContract, true)

                                        _instructorContractValidationState.value = InstructorContractValidationState.GPSResultInsideOrganization("cours débuté ${result.faceRecognitionResult.name.split(" ").first()}")
                                        //checkInEmployee(result.faceRecognitionResult.id)
                                        loadSessionReportForInstructorContract()
                                        withContext(Dispatchers.IO){
                                            //speakText("Succèss! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} votre cours à débuté")
                                            delay(500)
                                        }
                                        _instructorContractAction.value = null
                                        //_showSuccessResult.value = false
                                        _recognitionFinished.value = true
                                        Log.i("distrivite-ai-gps", "Success: You are in the organization")

                                    }
                                }
                                Log.i("distrivite-ai-gps", "Success: You are in the organization")
                            }
                        }
                    }

                }
            }
        }
    }

    private suspend fun getInstructorContractFaceRecognitionResultForFrame(faceFrame: FaceFrame): FaceFrameRecognitionSecureResult {
        val faceFrameRecognitionSecureResult = secureRecognizeInstructorContractFaces(faceFrame = faceFrame)
        return checkInstructorContractFaceMatchWithSelectedItem(faceFrameRecognitionSecureResult)
    }

    private fun getCheckGPSLocationForInstructorContract(domainInstructorContract: DomainInstructorContract, isCheckIn: Boolean = true): Location {

        if (isCheckIn) {
            return Location("manual").apply {
                longitude = domainInstructorContract.checkInLongitude
                latitude = domainInstructorContract.checkInLatitude
            }
        }
        return Location("manual").apply {
            longitude = domainInstructorContract.checkOutLongitude
            latitude = domainInstructorContract.checkOutLatitude
        }
    }

    private fun checkInstructorContractFaceMatchWithSelectedItem(faceFrameRecognitionSecureResult:FaceFrameRecognitionSecureResult) : FaceFrameRecognitionSecureResult {
        val checkedFaceRecognitionSecureResults = faceFrameRecognitionSecureResult.faceRecognitionSecureResults.map {
            Log.i("tiqtaq-facematch-test", "${it.faceRecognitionResult.id} and ${_selectedDomainInstructorContract.value?.id}::::${it.faceRecognitionResult.id == _selectedDomainInstructorContract.value?.id}")
            FaceRecognitionSecureResult(
                isFaceMatchWithSelectedItem = it.faceRecognitionResult.id == _selectedDomainInstructorContract.value?.id,
                faceRecognitionResult = it.faceRecognitionResult,
                isSecure = it.isSecure,
                face = it.face
            )
        }
        return FaceFrameRecognitionSecureResult(
            faceFrameRecognitionSecureResult.faceFrame, checkedFaceRecognitionSecureResults
        )
    }

    private suspend fun secureRecognizeInstructorContractFaces(faceFrame: FaceFrame) : FaceFrameRecognitionSecureResult = withContext(
        Dispatchers.Default) {
        val faceRecognitionSecureResults = mutableListOf<FaceRecognitionSecureResult>()

        _authOrgUser.value?.let {
            for (face in faceFrame.faces){
                val faceRecognitionSecureResult = faceRecognitionSecurePipeLine.recognize(faceFrame.bitmap, face, instructorContractKnownFaces, isLivenessActive = it.isLivenessActive)
                Log.i("distrivite-ai-test", "Secure ${faceRecognitionSecureResult.isSecure} is the face of ${faceRecognitionSecureResult.faceRecognitionResult.name} with confidence of ${faceRecognitionSecureResult.faceRecognitionResult.confidence}")
                faceRecognitionSecureResults.add(faceRecognitionSecureResult)
            }
        }

        FaceFrameRecognitionSecureResult(
            faceFrame=faceFrame,
            faceRecognitionSecureResults=faceRecognitionSecureResults
        )
    }

    fun onRegisterFace(domainInstructorContract: DomainInstructorContract) {
        selectDomainInstructorContract(domainInstructorContract)
        _instructorContractRegistrationState.value = InstructorContractRegistrationState.ScanningFace
    }

    fun onDeleteInstructorContractEmbeddings(domainInstructorContract: DomainInstructorContract) {
        selectDomainInstructorContract(domainInstructorContract)
        _selectedDomainInstructorContract.value?.let {
            viewModelScope.launch {
                withContext(context = Dispatchers.IO) {
                    updateInstructorContract(it.orgSlug, it.copy(embeddings = emptyList()))
                    Log.i("distrivite-ai-instructorContract-img", "InstructorContracts face images save successfully")
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

    private fun selectDomainInstructorContract(domainInstructorContract: DomainInstructorContract) {
        _selectedDomainInstructorContract.value = domainInstructorContract
        Log.i("distrivite-instructorContract-selection", "selection is ${_selectedDomainInstructorContract.value?.name}")
        //faceVideoStreamAnalyser.setOnImageStreamAnalyse(this::onAnalyze)
    }

    fun createUser(authOrgUser: AuthOrgUser, user: DomainInstructorContract) {
        viewModelScope.launch {
            instructorContractRepository.createDomainInstructorContract(authOrgUser.orgSlug, user)
        }
    }

    private fun updateInstructorContract(organization: String, user: DomainInstructorContract) {
        viewModelScope.launch {
            instructorContractRepository.updateDomainInstructorContract(organization, user)
        }
    }

    fun deleteDomainUser(organization: String, user: DomainInstructorContract) {
        viewModelScope.launch {
            instructorContractRepository.deleteDomainInstructorContract(organization, user)
        }
    }

    fun onLoadSessionReportForInstructorContract(instructorContract: DomainInstructorContract) {
        _recognitionFinished.value = false
        _selectedDomainInstructorContract.value = instructorContract
        _instructorContractAction.value = InstructorContractAction.REPORT
        _instructorContractValidationState.value = InstructorContractValidationState.FaceScanning
    }

    private fun convertToFloatArrayList(listOfLists: List<List<Float>>): List<FloatArray> {
        return listOfLists.map { it.toFloatArray() }
    }
    private fun loadSessionReportForInstructorContract() {
        _selectedDomainInstructorContract.value?.let {
            _sessionReportUrl.value = "en/${it.orgSlug}/cameis/instructor-contracts/${it.id}/sessions"
        }
    }

    fun onRefresh() {
        authOrgUser.value?.let {
            syncLocalDataWithServer(it.orgSlug)
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
                //_instructorContractsUiState.value = InstructorContractsUiState.Loading
                try {
                    instructorContractRepository.syncLocalWithRemoteInstructorContracts(organization)
                } catch (e: Exception) {
                    // _instructorContractsUiState.value = InstructorContractsUiState.Error(e.message ?: "Sync Failed")
                }
            }
            _isRefreshing.value = false

        }
    }

    fun onSearchQueryChanged(searchQuery:String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val searchedInstructorContracts = instructorContractRepository.searchDomainInstructorContractsFor(searchQuery)
                    _instructorContractsUiState.value = InstructorContractsUiState.Success(searchedInstructorContracts)
                    Log.i("distrivite-search", "search completed!")
                }catch (e:Exception) {
                    _instructorContractsUiState.value = InstructorContractsUiState.Error("")
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