package com.datavite.distrivite.presentation.claim

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
import com.datavite.distrivite.data.mapper.ClaimMapper
import com.datavite.distrivite.domain.model.DomainEmployee
import com.datavite.distrivite.domain.model.DomainClaim
import com.datavite.distrivite.domain.repository.ClaimRepository
import com.datavite.distrivite.domain.repository.EmployeeRepository
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
class ClaimViewModel @Inject constructor(
    private val claimRepository: ClaimRepository,
    private val employeeRepository: EmployeeRepository,
    private val workingPeriodRepository: WorkingPeriodRepository,
    private val networkStatusMonitor: NetworkStatusMonitor,
    private val claimMapper: ClaimMapper,
    private val locationManager: LocationManager,
    private val faceDetectorStreamAnalyser: FaceDetectorStreamAnalyser,
    private val faceRecognitionSecurePipeLine: FaceRecognitionSecurePipeLine,
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager
    ): ViewModel() {

    private val _claimsUiState = MutableStateFlow<ClaimsUiState>(
        ClaimsUiState.Loading)
    val claimsUiState: StateFlow<ClaimsUiState> = _claimsUiState


    // In ViewModel
    val authUserFlow = authOrgUserCredentialManager.sharedAuthOrgUserFlow

    // State to hold the currently selected user for facial recognition
    private val _currentDomainClaim = MutableStateFlow<DomainClaim>(
        DomainClaim(
            id = "1",
            created = "2024-09-22T10:00:00",
            modified = "2024-09-22T10:30:00",
            orgId = "org-123",
            orgSlug = "organization-slug",
            userId = "user-456",
            orgUserId = "org-user-789",
            employeeId = "employee-001",
            type = "annual", // Defaulting to "Annual Claim"
            hourlySalary = 15.50,
            claimedHours = "15.50",
            status = "pending", // Defaulting to "Pending"
            reason = "Annual claim for personal reasons",
            employeeName = "liedjify",
            date = "2024-09-22"
        )
    )
    val currentDomainClaim: StateFlow<DomainClaim> = _currentDomainClaim

    // State to hold the currently selected user for facial recognition
    private val _authOrgUser = MutableStateFlow<AuthOrgUser?>(null)
    val authOrgUser: StateFlow<AuthOrgUser?> = _authOrgUser

    private val _claimAction = MutableStateFlow<CLAIM_ACTIONS?>(null)
    val claimAction: StateFlow<CLAIM_ACTIONS?> = _claimAction

    // State for showing success alert
    private val _showSuccessResult = MutableStateFlow(false)
    val showSuccessResult: StateFlow<Boolean> = _showSuccessResult.asStateFlow()

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
        _claimAction.value?.let {
            // update teaching session on the server
            //updateTeachingSessionForSelected()
            withContext(Dispatchers.IO){
                when (it) {
                    CLAIM_ACTIONS.CREATE -> {

                        val recognitionResult = getFaceRecognitionResultForFrame(faceFrame)
                        val faceRecognitionSecureResults = recognitionResult.faceRecognitionSecureResults
                        if (faceRecognitionSecureResults.isNotEmpty()) {
                            val result = faceRecognitionSecureResults[0]
                            _faceFrameRecognitionSecureResult.value = recognitionResult

                            if (!result.faceRecognitionResult.isUnknownFace) {

                                _showSuccessResult.value = true
                                createClaimForEmployee(result.faceRecognitionResult.id)
                                //speakText("${checkedFace.faceRecognitionResult.name.split(" ")[0]} validé")
                                withContext(Dispatchers.IO){
                                    delay(500)
                                }
                                _claimAction.value = null
                                _showSuccessResult.value = false
                                _recognitionFinished.value = true
                                Log.i("distrivite-ai-gps", "Success: You are in the organization")
                            }
                        }

                    }
                    CLAIM_ACTIONS.APPROVE -> {}
                    CLAIM_ACTIONS.REJECT -> {}
                    CLAIM_ACTIONS.EDIT -> {
                        // updateClaim()
                    }
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
            val employee = employeeRepository.getEmployeeById(faceRecognitionSecureResult.faceRecognitionResult.id)
            _authOrgUser.value?.let {
                employee?.let { domainEmployee ->
                    val expectedLocation = getCheckGPSLocationForEmployee(domainEmployee, isCheckIn)
                    //val isGPSMatch = locationManager.isDeviceWithinOrganization(expectedLocation, it.isGPSActive)
                    val isGPSMatch = true
                    Log.i("distrivite-ai-gps", if (isGPSMatch) "GPS Match: You are within the organization" else "Failure: You are not in the organization")
                    faceRecognitionSecureResult.copy(isGPSMatch = isGPSMatch)
                } ?: faceRecognitionSecureResult.copy(isGPSMatch = false)
            }?: faceRecognitionSecureResult.copy(isGPSMatch = false)
        // In case employee is null
        }

        return faceFrameRecognitionSecureResult.copy(faceRecognitionSecureResults = updatedResults)
    }

    private fun getCheckGPSLocationForEmployee(domainEmployee: DomainEmployee, isCheckIn: Boolean = true): Location {
        if (isCheckIn) {
            return Location("manual").apply {
                longitude = domainEmployee.checkInLongitude
                latitude = domainEmployee.checkInLatitude
            }
        }
        return Location("manual").apply {
            longitude = domainEmployee.checkOutLongitude
            latitude = domainEmployee.checkOutLatitude
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
        observeLocalClaimsData()
        observeLocalEmployeesData()
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

    private fun observeOrganization() = viewModelScope.launch(Dispatchers.IO) {
        authUserFlow.collectLatest { collectedAuthOrgUser ->
            collectedAuthOrgUser?.let {
                Log.i("distrivite_first_comsummer","token changed from cosumer claim ${it.orgSlug}")
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

    private fun observeLocalClaimsData() {
        viewModelScope.launch {
            claimRepository.getClaimsFlow()
                .catch { error ->
                    _claimsUiState.value = ClaimsUiState.Error(error.message ?: "Unknown Error")
                }
                .collect { claims ->
                    _claimsUiState.value = ClaimsUiState.Success(claims = claims)
                    Log.i("distrivite-claims", "claims loaded successfully...${claims.size.toString()}")
                }
        }
    }

    private fun syncLocalDataWithServer(organization: String) {
        viewModelScope.launch(Dispatchers.IO) {
            //_teachingSessionsUiState.value = EmployeesUiState.Loading
            try {
                claimRepository.syncClaims(organization)
                Log.i("distrivite-claims", "support for remote server")
            } catch (e: Exception) {
                e.printStackTrace()
                // _teachingSessionsUiState.value = EmployeesUiState.Error(e.message ?: "Sync Failed")
            }
        }
    }

    private fun observeLocalEmployeesData() {
        viewModelScope.launch {
            employeeRepository.getEmployeesFlow()
                .catch {
                    // error fetching organizations users
                }
                .collect { users ->
                    updateKnownFacesFromEmployees(users)
                }
        }
    }

    private fun updateKnownFacesFromEmployees(domainEmployees:List<DomainEmployee>) {
        knownFaces.clear()
        for(organizationEmployee in domainEmployees){
            val knownFace = KnownFace(id = organizationEmployee.id, organizationEmployee.name, embeddings = convertToFloatArrayList(organizationEmployee.embeddings))
            Log.i("distrivite-ai", "$knownFace loaded successfully")
            knownFaces.add(knownFace)
        }
    }

    private fun convertToFloatArrayList(listOfLists: List<List<Float>>): List<FloatArray> {
        return listOfLists.map { it.toFloatArray() }
    }


    fun onCreate(){
        _recognitionFinished.value = false
        _claimAction.value = CLAIM_ACTIONS.CREATE
    }

    fun onApprove(domainClaim: DomainClaim){
        _recognitionFinished.value = false
        _claimAction.value = CLAIM_ACTIONS.APPROVE
        _currentDomainClaim.value = domainClaim.copy(status = "approved")
        updateClaim(_currentDomainClaim.value)

    }

    fun onReject(domainClaim: DomainClaim){
        _recognitionFinished.value = false
        _claimAction.value = CLAIM_ACTIONS.EDIT
        _currentDomainClaim.value = domainClaim.copy(status = "pending")
        updateClaim(_currentDomainClaim.value)
    }

    fun onEdit(domainClaim: DomainClaim){
        _recognitionFinished.value = false
        _claimAction.value = CLAIM_ACTIONS.REJECT
        _currentDomainClaim.value = domainClaim.copy(status = "rejected")
        //updateClaim(_currentDomainClaim.value)
    }

    private fun createClaimForEmployee(id: String) {

        _authOrgUser.value?.let {
            Log.i("distrivite-ati", "check in one time ${id}")
            viewModelScope.launch {
                val employee = employeeRepository.getEmployeeById(id)
                employee?.let {

                    val day = LocalDateTime.now().toLocalDate().dayOfWeek.toString()
                    _currentDomainClaim.value = _currentDomainClaim.value.copy(
                        id = UUID.randomUUID().toString(),
                        created = LocalDateTime.now().toString(),
                        modified = LocalDateTime.now().toString(),
                        employeeId = it.id,
                        orgUserId = it.orgUserId,
                        orgSlug = it.orgSlug,
                        orgId = it.orgId,
                        userId = it.userId,
                        hourlySalary = it.monthlySalary,
                        status = "pending",
                        employeeName = it.name
                    )

                    createClaim()

                }
            }
        }
        Log.i("distrivite-claim","checkIn success for $id")
    }




    private fun createClaim() {
        _authOrgUser.value?.let {
            viewModelScope.launch {
                claimRepository.createClaim(it.orgSlug, _currentDomainClaim.value)
            }
        }
    }

    private fun updateClaim(domainClaim: DomainClaim) {
        _authOrgUser.value?.let {
            viewModelScope.launch {
                claimRepository.updateClaim(it.orgSlug, domainClaim)
            }
        }
    }

    fun deleteClaim(domainClaim: DomainClaim) {
        _authOrgUser.value?.let {
            viewModelScope.launch {
                claimRepository.deleteClaim(it.orgSlug, domainClaim)
            }
        }
    }

    fun onClaimChange(domainClaim: DomainClaim) {
        _currentDomainClaim.value = domainClaim
    }
}