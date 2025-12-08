package com.datavite.distrivite.presentation.teachingcourse

import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.presentation.ai.FaceDetectorStreamAnalyser
import com.datavite.distrivite.presentation.ai.FaceRecognitionSecurePipeLine
import com.datavite.distrivite.presentation.ai.model.FaceFrame
import com.datavite.distrivite.presentation.ai.model.FaceFrameRecognitionSecureResult
import com.datavite.distrivite.presentation.ai.model.FaceRecognitionSecureResult
import com.datavite.distrivite.presentation.ai.model.KnownFace
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.location.LocationManager
import com.datavite.distrivite.data.network.NetworkStatusMonitor
import com.datavite.distrivite.data.mapper.TeachingCourseMapper
import com.datavite.distrivite.domain.model.DomainInstructorContract
import com.datavite.distrivite.domain.model.DomainTeachingCourse
import com.datavite.distrivite.domain.repository.TeachingCourseRepository
import com.datavite.distrivite.domain.repository.InstructorContractRepository
import com.datavite.distrivite.domain.repository.WorkingPeriodRepository
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
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TeachingCourseViewModel @Inject constructor(
    private val teachingCourseRepository: TeachingCourseRepository,
    private val instructorContractRepository: InstructorContractRepository,
    private val workingPeriodRepository: WorkingPeriodRepository,
    private val networkStatusMonitor: NetworkStatusMonitor,
    private val teachingCourseMapper: TeachingCourseMapper,
    private val locationManager: LocationManager,
    private val faceDetectorStreamAnalyser: FaceDetectorStreamAnalyser,
    private val faceRecognitionSecurePipeLine: FaceRecognitionSecurePipeLine,
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager
    ): ViewModel() {

    private val _teachingCoursesUiState = MutableStateFlow<TeachingCoursesUiState>(
        TeachingCoursesUiState.Loading)
    val teachingCoursesUiState: StateFlow<TeachingCoursesUiState> = _teachingCoursesUiState

    val authUserFlow = authOrgUserCredentialManager.sharedAuthOrgUserFlow


    // State to hold the currently selected user for facial recognition
    private val _currentDomainTeachingCourse = MutableStateFlow<DomainTeachingCourse?>(null)
    val currentDomainTeachingCourse: StateFlow<DomainTeachingCourse?> = _currentDomainTeachingCourse

    // State to hold the currently selected user for facial recognition
    private val _authOrgUser = MutableStateFlow<AuthOrgUser?>(null)
    val authOrgUser: StateFlow<AuthOrgUser?> = _authOrgUser

    private val _teachingCourseAction = MutableStateFlow<TeachingCourseAction?>(null)
    val teachingCourseAction: StateFlow<TeachingCourseAction?> = _teachingCourseAction

    // State for showing success alert
    private val _showSuccessResult = MutableStateFlow(false)
    val showSuccessResult: StateFlow<Boolean> = _showSuccessResult.asStateFlow()

    // State for showing success alert
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // State for showing success alert
    private val _recognitionFinished = MutableStateFlow(false)
    val recognitionFinished: StateFlow<Boolean> = _recognitionFinished.asStateFlow()

    private val knownFaces = mutableListOf<KnownFace>()
    private var isFrontCamera = true

    private val _faceFrameRecognitionSecureResult = MutableStateFlow<FaceFrameRecognitionSecureResult?>(null)
    val faceFrameRecognitionSecureResult = _faceFrameRecognitionSecureResult.asStateFlow()

    private val _faceFrame = MutableStateFlow<FaceFrame?>(null)
    val faceFrame = _faceFrame.asStateFlow()

    fun getVideoStreamAnalyser() = faceDetectorStreamAnalyser
    fun toggleCamera(){
        isFrontCamera = !isFrontCamera
    }

    private suspend fun recognizeFaceFrame(faceFrame: FaceFrame) {
        _teachingCourseAction.value?.let {
            // update teaching session on the server
            //updateTeachingSessionForSelected()
            withContext(Dispatchers.IO){
                when (it) {
                    TeachingCourseAction.CREATE -> {

                        val recognitionResult = getFaceRecognitionResultForFrame(faceFrame)
                        val faceRecognitionSecureResults = recognitionResult.faceRecognitionSecureResults
                        if (faceRecognitionSecureResults.isNotEmpty()) {
                            val result = faceRecognitionSecureResults[0]
                            _faceFrameRecognitionSecureResult.value = recognitionResult

                            if (!result.faceRecognitionResult.isUnknownFace) {

                                _showSuccessResult.value = true
                                createTeachingCourseForInstructorContract(result.faceRecognitionResult.id)
                                //speakText("${checkedFace.faceRecognitionResult.name.split(" ")[0]} validé")
                                withContext(Dispatchers.IO){
                                    delay(500)
                                }
                                _teachingCourseAction.value = null
                                _showSuccessResult.value = false
                                _recognitionFinished.value = true
                                Log.i("distrivite-ai-gps", "Success: You are in the organization")
                            }
                        }

                    }
                    TeachingCourseAction.UPDATE -> {}
                    TeachingCourseAction.DELETE -> {}

                }
            }
        }
    }

    private suspend fun getFaceRecognitionResultForFrame(faceFrame: FaceFrame): FaceFrameRecognitionSecureResult {
        val faceFrameRecognitionSecureResult =
            secureRecognizeFaces(faceFrame = faceFrame)
        val checkedFaceFrameRecognitionSecureResult =
            checkFaceMatchWithSelectedItem(faceFrameRecognitionSecureResult)
        return checkUserFaceGPSMatchWithCurrentGPS(checkedFaceFrameRecognitionSecureResult)
    }

    private suspend fun checkUserFaceGPSMatchWithCurrentGPS(
        faceFrameRecognitionSecureResult: FaceFrameRecognitionSecureResult,
        isCheckIn: Boolean = true
    ): FaceFrameRecognitionSecureResult {

        val updatedResults = faceFrameRecognitionSecureResult.faceRecognitionSecureResults.map { faceRecognitionSecureResult ->
            val instructorContract = instructorContractRepository.getDomainInstructorContractById(faceRecognitionSecureResult.faceRecognitionResult.id)
            _authOrgUser.value?.let {
                instructorContract?.let { domainInstructorContract ->
                    val expectedLocation = getCheckGPSLocationForInstructorContract(domainInstructorContract, isCheckIn)
                    //val isGPSMatch = locationManager.isDeviceWithinOrganization(expectedLocation, it.isGPSActive)
                    val isGPSMatch = true
                    Log.i("distrivite-ai-gps", if (isGPSMatch) "GPS Match: You are within the organization" else "Failure: You are not in the organization")
                    faceRecognitionSecureResult.copy(isGPSMatch = isGPSMatch)
                } ?: faceRecognitionSecureResult.copy(isGPSMatch = false)
            }?: faceRecognitionSecureResult.copy(isGPSMatch = false)
        // In case instructorContract is null
        }

        return faceFrameRecognitionSecureResult.copy(faceRecognitionSecureResults = updatedResults)
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
    private fun checkFaceMatchWithSelectedItem(faceFrameRecognitionSecureResult:FaceFrameRecognitionSecureResult) : FaceFrameRecognitionSecureResult {
        val checkedFaceRecognitionSecureResults = faceFrameRecognitionSecureResult.faceRecognitionSecureResults.map {
            FaceRecognitionSecureResult(
                isFaceMatchWithSelectedItem = true,
                faceRecognitionResult = it.faceRecognitionResult,
                isSecure = it.isSecure,
                face = it.face
            )
        }
        return FaceFrameRecognitionSecureResult(
            faceFrameRecognitionSecureResult.faceFrame, checkedFaceRecognitionSecureResults
        )
    }

    private suspend fun secureRecognizeFaces(faceFrame: FaceFrame) : FaceFrameRecognitionSecureResult = withContext(
        Dispatchers.Default) {
        val faceRecognitionSecureResults = mutableListOf<FaceRecognitionSecureResult>()

        _authOrgUser.value?.let {
            for (face in faceFrame.faces){
                val faceRecognitionSecureResult = faceRecognitionSecurePipeLine.recognize(faceFrame.bitmap, face, knownFaces, isLivenessActive = it.isLivenessActive)
                Log.i("distrivite-ai-test", "Secure ${faceRecognitionSecureResult.isSecure} is the face of ${faceRecognitionSecureResult.faceRecognitionResult.name} with confidence of ${faceRecognitionSecureResult.faceRecognitionResult.confidence}")
                faceRecognitionSecureResults.add(faceRecognitionSecureResult)
            }
        }

        FaceFrameRecognitionSecureResult(
            faceFrame=faceFrame,
            faceRecognitionSecureResults=faceRecognitionSecureResults
        )
    }

    fun onTakePhoto(bitmap: Bitmap) {
        Log.i("distrivite-ai", "a photo have been taking ${bitmap.height}")
    }

    init {
        observeOrganization()
        observeFaceFrame()
        observeLocalTeachingCoursesData()
        observeLocalInstructorContractsData()
    }

    private fun observeFaceFrame() {
        viewModelScope.launch {
            faceDetectorStreamAnalyser.faceFrameFlow.collectLatest {
                _faceFrame.value = it
                it?.let {
                    //mutex.withLock {
                        recognizeFaceFrame(it)
                    //}
                }
            }
        }
    }

    fun onRefresh() {
        authOrgUser.value?.let {
            syncLocalDataWithServer(it.orgSlug)
        }
    }
    private fun observeOrganization() = viewModelScope.launch(Dispatchers.IO) {
        authUserFlow.collectLatest { collectedAuthOrgUser ->
            collectedAuthOrgUser?.let {
                Log.i("distrivite_first_comsummer","token changed from cosumer teachingCourse ${it.orgSlug}")
                _authOrgUser.value = it

                observeNetworkStateAndSyncLocalData(it)
            }
        }
    }

    private suspend fun observeNetworkStateAndSyncLocalData(authOrgUser: AuthOrgUser) {
        networkStatusMonitor.isConnected.collect { isConnected ->
            if (isConnected) {
                syncLocalDataWithServer(authOrgUser.orgSlug)
            }
        }
    }

    private fun observeLocalTeachingCoursesData() {
        viewModelScope.launch {
            teachingCourseRepository.getDomainTeachingCoursesFlow()
                .catch { error ->
                    _teachingCoursesUiState.value = TeachingCoursesUiState.Error(error.message ?: "Unknown Error")
                }
                .collect { teachingCourses ->
                    _teachingCoursesUiState.value = TeachingCoursesUiState.Success(teachingCourses = teachingCourses)
                    Log.i("distrivite-teachingCourses", "teachingCourses loaded successfully...${teachingCourses.size.toString()}")
                }
        }
    }

    private fun syncLocalDataWithServer(organization: String) {
        viewModelScope.launch() {
            _isRefreshing.value = true
            withContext(Dispatchers.IO) {
                //_teachingSessionsUiState.value = InstructorContractsUiState.Loading
                try {
                    teachingCourseRepository.syncDomainTeachingCourses(organization)
                    Log.i("distrivite-teachingCourses", "support for remote server")
                } catch (e: Exception) {
                    e.printStackTrace()
                    // _teachingSessionsUiState.value = InstructorContractsUiState.Error(e.message ?: "Sync Failed")
                }
            }
            _isRefreshing.value = false
        }
    }

    private fun observeLocalInstructorContractsData() {
        viewModelScope.launch {
            instructorContractRepository.getDomainInstructorContractsFlow()
                .catch {
                    // error fetching organizations users
                }
                .collect { users ->
                    updateKnownFacesFromInstructorContracts(users)
                }
        }
    }

    private fun updateKnownFacesFromInstructorContracts(domainInstructorContracts:List<DomainInstructorContract>) {
        knownFaces.clear()
        for(organizationInstructorContract in domainInstructorContracts){
            val knownFace = KnownFace(id = organizationInstructorContract.id, organizationInstructorContract.name, embeddings = convertToFloatArrayList(organizationInstructorContract.embeddings))
            Log.i("distrivite-ai", "$knownFace loaded successfully")
            knownFaces.add(knownFace)
        }
    }

    private fun convertToFloatArrayList(listOfLists: List<List<Float>>): List<FloatArray> {
        return listOfLists.map { it.toFloatArray() }
    }


    fun onCreate(){
        _recognitionFinished.value = false
        _teachingCourseAction.value = TeachingCourseAction.CREATE
    }

    fun onDomainTeachingCourseUpdate(domainTeachingCourse: DomainTeachingCourse){
        _recognitionFinished.value = false
        _teachingCourseAction.value = TeachingCourseAction.UPDATE
        _currentDomainTeachingCourse.value = domainTeachingCourse.copy(course = "update name!")
        _currentDomainTeachingCourse.value?.let {
            updateTeachingCourse(it)
        }

    }

    fun onDomainTeachingCourseDelete(domainTeachingCourse: DomainTeachingCourse){
        _recognitionFinished.value = false
        _teachingCourseAction.value = TeachingCourseAction.DELETE
        _currentDomainTeachingCourse.value = domainTeachingCourse.copy(course = "delete!")
        _currentDomainTeachingCourse.value?.let {
            updateTeachingCourse(it)
        }
    }

    private fun createTeachingCourseForInstructorContract(id: String) {

        _authOrgUser.value?.let {
            Log.i("distrivite-ati", "check in one time ${id}")
            viewModelScope.launch {
                val instructorContract = instructorContractRepository.getDomainInstructorContractById(id)
                instructorContract?.let {

                    if (it.isManager){
                        val day = LocalDateTime.now().toLocalDate().dayOfWeek.toString()
                        _currentDomainTeachingCourse.value = _currentDomainTeachingCourse.value?.copy(
                            id = UUID.randomUUID().toString(),
                            created = LocalDateTime.now().toString(),
                            modified = LocalDateTime.now().toString(),
                            instructor = it.name,
                            orgUserId = it.orgUserId,
                            orgSlug = it.orgSlug,
                            orgId = it.orgId,
                            userId = it.userId,
                        )

                        createTeachingCourse()
                    }
                }
            }
        }
        Log.i("distrivite-teachingCourse","checkIn success for $id")
    }


    private fun createTeachingCourse() {
        _authOrgUser.value?.let {
            viewModelScope.launch {
                _currentDomainTeachingCourse.value?.let {
                    teachingCourseRepository.createDomainTeachingCourse(it.orgSlug, it)
                }
            }
        }
    }

    private fun updateTeachingCourse(domainTeachingCourse: DomainTeachingCourse) {
        _authOrgUser.value?.let {
            viewModelScope.launch {
                teachingCourseRepository.updateDomainTeachingCourse(it.orgSlug, domainTeachingCourse)
            }
        }
    }

    fun deleteTeachingCourse(domainTeachingCourse: DomainTeachingCourse) {
        _authOrgUser.value?.let {
            viewModelScope.launch {
                teachingCourseRepository.deleteDomainTeachingCourse(it.orgSlug, domainTeachingCourse.id)
            }
        }
    }

    fun onTeachingCourseChange(domainTeachingCourse: DomainTeachingCourse) {
        _currentDomainTeachingCourse.value = domainTeachingCourse
    }

    fun onSearchQueryChanged(searchQuery:String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val searchedTeachingCourses = teachingCourseRepository.getDomainTeachingCoursesFor(searchQuery)
                    _teachingCoursesUiState.value = TeachingCoursesUiState.Success(searchedTeachingCourses)
                    Log.i("distrivite-search", "search completed!")
                }catch (e:Exception) {
                    _teachingCoursesUiState.value = TeachingCoursesUiState.Error("")
                }
            }
        }
    }

}