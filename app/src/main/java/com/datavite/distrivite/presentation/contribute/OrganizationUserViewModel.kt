package com.datavite.distrivite.presentation.contribute

import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
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
import com.datavite.distrivite.domain.model.DomainOrganizationUser
import com.datavite.distrivite.domain.repository.OrganizationUserRepository
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
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
class OrganizationUserViewModel @Inject constructor(
    private val organizationUserRepository: OrganizationUserRepository,
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


    private val _faceFrameRecognitionSecureResult = MutableStateFlow<FaceFrameRecognitionSecureResult?>(null)
    val faceFrameRecognitionSecureResult = _faceFrameRecognitionSecureResult.asStateFlow()

    // State to hold the currently selected user for facial recognition
    private val _selectedDomainOrganizationUser = MutableStateFlow<DomainOrganizationUser?>(null)
    val selectedDomainOrganizationUser: StateFlow<DomainOrganizationUser?> = _selectedDomainOrganizationUser

    private val _organizationUsersUiState = MutableStateFlow<OrganizationUsersUiState>(OrganizationUsersUiState.Loading)
    val organizationUsersUiState: StateFlow<OrganizationUsersUiState> = _organizationUsersUiState

    // State to hold the currently selected user for facial recognition
    private val _organization = MutableStateFlow<AuthOrgUser?>(null)
    val organization: StateFlow<AuthOrgUser?> = _organization

    init {
        Log.d("ContributeViewModel", "Initialized")
        observeLocalOrganizationUsersData()
        observeOrganization()
    }

    private fun observeLocalOrganizationUsersData() {
        viewModelScope.launch {
            organizationUserRepository.getOrganizationUsersFlow()
                .catch { error ->
                    _organizationUsersUiState.value = OrganizationUsersUiState.Error(error.message ?: "Unknown Error")
                }
                .collect { users ->
                    _organizationUsersUiState.value = OrganizationUsersUiState.Success(organizationUsers = users)
                }
        }
    }

    private fun observeOrganization() = viewModelScope.launch(Dispatchers.IO) {
        authUserFlow.collectLatest { organization ->
            organization?.let {
                _organization.value = it
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
            this@OrganizationUserViewModel.onTakePhoto(imageProxy)
        }

        override fun onError(exception: ImageCaptureException) {
            super.onError(exception)
            Log.e("Camera", "Couldn't take photo: ", exception)
        }
    }

    // Function to add a photo for the current user
    fun onTakePhoto(imageProxy: ImageProxy)  {
        viewModelScope.launch {
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
                }
                return@withContext
            }
        }
    }

    fun onSavePhotos(navigator: DestinationsNavigator) {
        _selectedDomainOrganizationUser.value?.let {
            viewModelScope.launch {
                withContext(context = Dispatchers.IO) {
                    //internalStorageFaceImageRepository.createAndSaveBitmaps(userBitmap = _userBitmap.value)
                    val embeddings = convertUserBitmapToEmbedding()
                    updateUser(it.orgSlug, it.copy(embeddings = convertToListOfLists(embeddings)))
                    Log.i("distrivite-ai-img", "Users face images save successfully")
                    _userBitmap.value = UserBitmap("", emptyList())
                    // Trigger success alert
                    _showSuccessAlert.value = true
                    // Hide alert after 2 seconds
                    delay(2000)
                    _showSuccessAlert.value = false
                }
                navigator.navigateUp()
            }
        }

    }

    fun onDeleteOrganizationUserEmbeddings(navigator: DestinationsNavigator) {
        _selectedDomainOrganizationUser.value?.let {
            viewModelScope.launch {
                withContext(context = Dispatchers.IO) {
                    updateUser(it.orgSlug, it.copy(embeddings = emptyList()))
                    Log.i("distrivite-ai-user-img", "Users face images save successfully")
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

    fun selectDomainOrganizationUser(domainOrganizationUser: DomainOrganizationUser) {
        _selectedDomainOrganizationUser.value = domainOrganizationUser
        faceVideoStreamAnalyser.setOnImageStreamAnalyse(this::onAnalyze)
    }

    fun createUser(organization: String, user: DomainOrganizationUser) {
        viewModelScope.launch {
            organizationUserRepository.createOrganizationUser(organization, user)
        }
    }

    private fun updateUser(organization: String, user: DomainOrganizationUser) {
        viewModelScope.launch {
            organizationUserRepository.updateOrganizationUser(organization, user)
        }
    }

    fun deleteUser(organization: String, user: DomainOrganizationUser) {
        viewModelScope.launch {
            organizationUserRepository.deleteOrganizationUser(organization, user)
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
            //_organizationUsersUiState.value = OrganizationUsersUiState.Loading
            try {
                organizationUserRepository.syncOrganizationUsers(organization)
            } catch (e: Exception) {
               // _organizationUsersUiState.value = OrganizationUsersUiState.Error(e.message ?: "Sync Failed")
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