package com.datavite.distrivite.presentation.session

import FilterOption
import android.app.Application
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.datavite.distrivite.feature.cameis.domain.repository.RoomRepository
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.location.LocationManager
import com.datavite.distrivite.data.notification.NotificationOrchestrator
import com.datavite.distrivite.data.notification.TextToSpeechNotifier
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.data.sync.SyncOrchestrator
import com.datavite.distrivite.domain.model.DomainInstructorContract
import com.datavite.distrivite.domain.model.DomainRoom
import com.datavite.distrivite.domain.model.DomainStudent
import com.datavite.distrivite.domain.model.DomainStudentAttendance
import com.datavite.distrivite.domain.model.DomainTeachingCourse
import com.datavite.distrivite.domain.model.DomainTeachingPeriod
import com.datavite.distrivite.domain.model.DomainTeachingSession
import com.datavite.distrivite.domain.notification.NotificationBus
import com.datavite.distrivite.domain.notification.NotificationEvent
import com.datavite.distrivite.domain.repository.InstructorContractRepository
import com.datavite.distrivite.domain.repository.StudentAttendanceRepository
import com.datavite.distrivite.domain.repository.StudentRepository
import com.datavite.distrivite.domain.repository.TeachingCourseRepository
import com.datavite.distrivite.domain.repository.TeachingPeriodRepository
import com.datavite.distrivite.domain.repository.TeachingSessionRepository
import com.datavite.distrivite.presentation.ai.FaceDetectorStreamAnalyser
import com.datavite.distrivite.presentation.ai.FaceRecognitionSecurePipeLine
import com.datavite.distrivite.presentation.ai.model.FaceFrame
import com.datavite.distrivite.presentation.ai.model.FaceFrameRecognitionSecureResult
import com.datavite.distrivite.presentation.ai.model.FaceRecognitionSecureResult
import com.datavite.distrivite.presentation.ai.model.KnownFace
import com.datavite.distrivite.utils.launchLoading
import com.ramcosta.composedestinations.generated.destinations.TeachingSessionDetailScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TeachingSessionScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TeachingSessionViewModel @Inject constructor(
    private val instructorContractRepository: InstructorContractRepository,
    private val studentAttendanceRepository: StudentAttendanceRepository,
    private val teachingSessionRepository: TeachingSessionRepository,
    private val teachingCourseRepository: TeachingCourseRepository,
    private val studentRepository: StudentRepository,
    private val roomRepository: RoomRepository,
    private val teachingPeriodRepository: TeachingPeriodRepository,
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager,
    private val locationManager: LocationManager,
    private val syncOrchestrator: SyncOrchestrator,
    private val notificationBus: NotificationBus,
    private val textToSpeechNotifier: TextToSpeechNotifier,
    private val notificationOrchestrator: NotificationOrchestrator,
    private val faceDetectorStreamAnalyser: FaceDetectorStreamAnalyser,
    private val faceRecognitionSecurePipeLine: FaceRecognitionSecurePipeLine,
    application: Application
) : AndroidViewModel(application) {

    private val _teachingSessionsUiState = MutableStateFlow<TeachingSessionsUiState>(TeachingSessionsUiState.Loading)
    val teachingSessionsUiState: StateFlow<TeachingSessionsUiState> = _teachingSessionsUiState

    private val _teachingSessionValidationState = MutableStateFlow<TeachingSessionValidationState>(
        TeachingSessionValidationState.FaceScanning)
    val teachingSessionValidationState: StateFlow<TeachingSessionValidationState> = _teachingSessionValidationState

    private val _teachingSessionActionExecutionState = MutableStateFlow<TeachingSessionActionExecutionState>(
        TeachingSessionActionExecutionState.Started)
    val teachingSessionActionExecutionState: StateFlow<TeachingSessionActionExecutionState> = _teachingSessionActionExecutionState

    // State for showing success alert
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()


    // UI state to show current notification message & type
    private val _notificationState = mutableStateOf<NotificationEvent?>(null)
    val notificationState: State<NotificationEvent?> = _notificationState


    val authUserFlow = authOrgUserCredentialManager.sharedAuthOrgUserFlow


    private val _teachingCourseQuery = MutableStateFlow("")
    val teachingCourseQuery: StateFlow<String> = _teachingCourseQuery.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val teachingCourseResults: StateFlow<List<DomainTeachingCourse>> = _teachingCourseQuery
        .flatMapLatest { query -> teachingCourseRepository.searchDomainTeachingCourses(query) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _teachingPeriods = MutableStateFlow<List<DomainTeachingPeriod>>(emptyList())
    val teachingPeriods: StateFlow<List<DomainTeachingPeriod>> = _teachingPeriods.asStateFlow()


    private val _teachingCourses = MutableStateFlow<List<DomainTeachingCourse>>(emptyList())
    val teachingCourses: StateFlow<List<DomainTeachingCourse>> = _teachingCourses.asStateFlow()

    private val _rooms = MutableStateFlow<List<DomainRoom>>(emptyList())
    val rooms: StateFlow<List<DomainRoom>> = _rooms.asStateFlow()

    private val _currentDomainTeachingSession = MutableStateFlow<DomainTeachingSession?>(null)
    val currentDomainTeachingSession: StateFlow<DomainTeachingSession?> = _currentDomainTeachingSession

    private val _studentAttendances = MutableStateFlow<List<DomainStudentAttendance>>(emptyList())
    val studentAttendances: StateFlow<List<DomainStudentAttendance>> = _studentAttendances.asStateFlow()

    private val _students = MutableStateFlow<List<DomainStudent>>(emptyList())
    val students: StateFlow<List<DomainStudent>> = _students.asStateFlow()


    // 1) All attendances _for_ the current session
    val studentSessionAttendances: StateFlow<List<DomainStudentAttendance>> =
        combine(
            currentDomainTeachingSession,
            studentAttendances
        ) { currentSession, allAttendances ->
            if (currentSession == null) emptyList()
            else allAttendances.filter { it.sessionId == currentSession.id }
        }.distinctUntilChanged().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
        )

    // 2) All students who appear in those attendances
    val sessionStudents: StateFlow<List<DomainStudent>> =
        combine(students, currentDomainTeachingSession) { allStudents, domainTeachingSession ->
            if (domainTeachingSession != null) {
                allStudents.filter { it.educationClassId == domainTeachingSession.educationClassId }
            } else {
                emptyList()
            }
        }.distinctUntilChanged().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    // Total number of students
    val totalStudents: StateFlow<Int> = sessionStudents
        .map { it.size }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Total present
    val presentCount: StateFlow<Int> = studentSessionAttendances
        .map { attendances -> attendances.count { it.isPresent } }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Are all students marked present
    val isAllPresent: StateFlow<Boolean> = studentSessionAttendances
        .map { attendances ->
            // if there are no attendances, we consider “not all present”
            attendances.isNotEmpty() && attendances.all { it.isPresent }
        }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    private val _syncStatus = MutableStateFlow<WorkInfo.State?>(null)
    val syncStatus: StateFlow<WorkInfo.State?> = _syncStatus

    // State for showing success alert
    private val _showSuccessResult = MutableStateFlow(false)
    val showSuccessResult: StateFlow<Boolean> = _showSuccessResult.asStateFlow()

    private val _teachingSessionAction = MutableStateFlow<TeachingSessionAction?>(null)
    val teachingSessionAction: StateFlow<TeachingSessionAction?> = _teachingSessionAction

    private val _location = MutableStateFlow<Pair<Double,Double>?>(null)
    val location: StateFlow<Pair<Double,Double>?> = _location

    // State to hold the currently selected user for facial recognition
    private val _authOrgUser = MutableStateFlow<AuthOrgUser?>(null)
    val authOrgUser: StateFlow<AuthOrgUser?> = _authOrgUser

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    // State for showing success alert
    //private val _recognitionFinished = MutableStateFlow(false)
    //val recognitionFinished: StateFlow<Boolean> = _recognitionFinished.asStateFlow()
    private val instructorContractKnownFaces = mutableListOf<KnownFace>()
    private val studentKnownFaces = mutableListOf<KnownFace>()
    private val delegateStudentKnownFaces = mutableListOf<KnownFace>()
    private val processingMutex = Mutex() // Mutex for managing single image processing
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
        Log.i("distrivite-recognizeFaceFrame", "recognizeFaceFrame() called")

        _teachingSessionAction.value?.let {teachingSessionAction  ->
            _authOrgUser.value?.let { authOrgUser  ->
                withContext(Dispatchers.IO){
                    when (teachingSessionAction) {
                        TeachingSessionAction.CREATE -> {
                            Log.i("distrivite-ai-session-started", "Success: You are in the organization")
                            val recognitionResult = getInstructorContractFaceRecognitionResultForFrame(faceFrame)
                            val faceRecognitionSecureResults = recognitionResult.faceRecognitionSecureResults
                            if (faceRecognitionSecureResults.isNotEmpty()) {
                                val result = faceRecognitionSecureResults[0]
                                _faceFrameRecognitionSecureResult.value = recognitionResult

                                //if (!result.faceRecognitionResult.isUnknownFace || result.faceRecognitionResult.isUnknownFace) {
                                if (!result.faceRecognitionResult.isUnknownFace && result.isFaceMatchWithSelectedItem) {
                                    val instructorContractResponse = instructorContractRepository.getDomainInstructorContractById(result.faceRecognitionResult.id)
                                    instructorContractResponse?.let { instructorContract ->
                                        val expectedLocation = getCheckGPSLocationForInstructorContract(instructorContract, true)

                                        if (authOrgUser.isGPSActive) {
                                            _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultProcessing
                                            val userLocation = locationManager.getCurrentLocation()
                                            val isGPSMatch = locationManager.isDeviceWithinOrganization(expectedLocation, userLocation, authOrgUser.radius)
                                            if (isGPSMatch){
                                                userLocation?.let {
                                                    if(!locationManager.isLocationMock(userLocation)) {
                                                        _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultInsideOrganization("Bye ${result.faceRecognitionResult.name.split(" ")[0]}")
                                                        Log.i("distrivite-ai-gps-verify", "GPS Match: You are within the organization")
                                                        //checkInEmployee(result.faceRecognitionResult.id)
                                                        createTeachingSession()
                                                        withContext(Dispatchers.IO){
                                                            textToSpeechNotifier.speak(NotificationEvent.Success("Succèss! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} votre cours à débuté."))
                                                            delay(500)
                                                        }
                                                        _teachingSessionAction.value = null
                                                        //_recognitionFinished.value = true
                                                        _teachingSessionActionExecutionState.value =
                                                            TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                        Log.i("distrivite-ai-gps", "Success: You are in the organization")
                                                    }else {
                                                        Log.i("distrivite-ai-gps", "Failure: GPS Spoofing detected!")
                                                    }
                                                }
                                            }else if (userLocation != null) {
                                                withContext(Dispatchers.IO){
                                                    textToSpeechNotifier.speak(NotificationEvent.Failure("échec! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} allez proche de votre check-point et ré-éssayez!."))
                                                    delay(500)
                                                }
                                                _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultOutsideOrganization("You are not in the organization. You are trying bad things.", TeachingSessionAction.START_SESSION)
                                                _teachingSessionAction.value = null
                                                //_recognitionFinished.value = true
                                                Log.i("distrivite-ai-gps-verify", "GPS Not Match:You are not in the organization. You are trying bad things.")
                                            } else {
                                                withContext(Dispatchers.IO){
                                                    textToSpeechNotifier.speak(NotificationEvent.Failure("échec! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} activez votre gps et ré-éssayez!."))
                                                    delay(500)
                                                }
                                                _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultError(TeachingSessionAction.START_SESSION)
                                                _teachingSessionAction.value = null
                                                //_recognitionFinished.value = true
                                                Log.i("distrivite-ai-gps-verify", "GPS Failure: Failed to get gps data, please turn on your gps")
                                            }

                                        }else {
                                            _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultInsideOrganization("cours débuté ${result.faceRecognitionResult.name.split(" ").first()}")
                                            //checkInEmployee(result.faceRecognitionResult.id)
                                            createTeachingSession()
                                            withContext(Dispatchers.IO){
                                                textToSpeechNotifier.speak(NotificationEvent.Success("Succèss! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} votre cours est crée."))
                                                delay(500)
                                            }
                                            _teachingSessionAction.value = null
                                            //_showSuccessResult.value = false
                                            //_recognitionFinished.value = true
                                            _teachingSessionActionExecutionState.value =
                                                TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                            _teachingSessionActionExecutionState.value =
                                                TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                            Log.i("distrivite-ai-gps", "Success: You are in the organization")
                                        }

                                    }
                                    Log.i("distrivite-ai-gps", "Success: You are in the organization")
                                }
                            }
                        }
                        TeachingSessionAction.UPDATE -> {}
                        TeachingSessionAction.SCENARIO -> {}
                        TeachingSessionAction.START_SESSION -> {
                            Log.i("distrivite-ai-session-started", "Success: You are in the organization")
                            val recognitionResult = getInstructorContractFaceRecognitionResultForFrame(faceFrame)
                            val faceRecognitionSecureResults = recognitionResult.faceRecognitionSecureResults
                            if (faceRecognitionSecureResults.isNotEmpty()) {
                                val result = faceRecognitionSecureResults[0]
                                _faceFrameRecognitionSecureResult.value = recognitionResult

                                if (!result.faceRecognitionResult.isUnknownFace && result.isFaceMatchWithSelectedItem) {
                                    val instructorContractResponse = instructorContractRepository.getDomainInstructorContractById(result.faceRecognitionResult.id)
                                    instructorContractResponse?.let { instructorContract ->
                                        val expectedLocation = getCheckGPSLocationForInstructorContract(instructorContract, true)

                                        if (authOrgUser.isGPSActive) {
                                            _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultProcessing
                                            val userLocation = locationManager.getCurrentLocation()
                                            val isGPSMatch = locationManager.isDeviceWithinOrganization(expectedLocation, userLocation, authOrgUser.radius)
                                            if (isGPSMatch){
                                                userLocation?.let {
                                                    if(!locationManager.isLocationMock(userLocation)) {
                                                        _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultInsideOrganization("Bye ${result.faceRecognitionResult.name.split(" ")[0]}")
                                                        Log.i("distrivite-ai-gps-verify", "GPS Match: You are within the organization")
                                                        //checkInEmployee(result.faceRecognitionResult.id)
                                                        startDomainTeachingSession()
                                                        withContext(Dispatchers.IO){
                                                            textToSpeechNotifier.speak(NotificationEvent.Success("Succèss! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} votre cours à débuté."))
                                                            delay(500)
                                                        }
                                                        _teachingSessionAction.value = null
                                                        //_recognitionFinished.value = true
                                                        _teachingSessionActionExecutionState.value =
                                                TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                        _teachingSessionActionExecutionState.value =
                                                            TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                        Log.i("distrivite-ai-gps", "Success: You are in the organization")
                                                    }else {
                                                        Log.i("distrivite-ai-gps", "Failure: GPS Spoofing detected!")
                                                    }
                                                }
                                            }else if (userLocation != null) {
                                                withContext(Dispatchers.IO){
                                                    textToSpeechNotifier.speak(NotificationEvent.Failure("échec! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} allez proche de votre check-point et ré-éssayez!."))
                                                    delay(500)
                                                }
                                                _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultOutsideOrganization("You are not in the organization. You are trying bad things.", TeachingSessionAction.START_SESSION)
                                                _teachingSessionAction.value = null
                                                //_recognitionFinished.value = true
                                                _teachingSessionActionExecutionState.value =
                                                TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                Log.i("distrivite-ai-gps-verify", "GPS Not Match:You are not in the organization. You are trying bad things.")
                                            } else {
                                                withContext(Dispatchers.IO){
                                                    textToSpeechNotifier.speak(NotificationEvent.Failure("échec! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} activez votre gps et ré-éssayez!."))
                                                    delay(500)
                                                }
                                                _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultError(TeachingSessionAction.START_SESSION)
                                                _teachingSessionAction.value = null
                                                //_recognitionFinished.value = true
                                                _teachingSessionActionExecutionState.value =
                                                TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                Log.i("distrivite-ai-gps-verify", "GPS Failure: Failed to get gps data, please turn on your gps")
                                            }

                                        }else {
                                            _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultInsideOrganization("cours débuté ${result.faceRecognitionResult.name.split(" ").first()}")
                                            //checkInEmployee(result.faceRecognitionResult.id)
                                            startDomainTeachingSession()
                                            withContext(Dispatchers.IO){
                                                textToSpeechNotifier.speak(NotificationEvent.Success("Succèss! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} votre cours à débuté."))
                                                delay(500)
                                            }
                                            _teachingSessionAction.value = null
                                            //_showSuccessResult.value = false
                                            ////_recognitionFinished.value = true
                                            _teachingSessionActionExecutionState.value =
                                                TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                            Log.i("distrivite-ai-gps", "Success: You are in the organization")
                                        }

                                    }
                                    Log.i("distrivite-ai-gps", "Success: You are in the organization")
                                }
                            }
                        }
                        TeachingSessionAction.END_SESSION -> {
                            val recognitionResult = getInstructorContractFaceRecognitionResultForFrame(faceFrame)
                            val faceRecognitionSecureResults = recognitionResult.faceRecognitionSecureResults
                            if (faceRecognitionSecureResults.isNotEmpty()) {
                                val result = faceRecognitionSecureResults[0]
                                _faceFrameRecognitionSecureResult.value = recognitionResult

                                if (!result.faceRecognitionResult.isUnknownFace && result.isFaceMatchWithSelectedItem) {
                                    val instructorContractResponse = instructorContractRepository.getDomainInstructorContractById(result.faceRecognitionResult.id)
                                    instructorContractResponse?.let { instructorContract ->
                                        val expectedLocation = getCheckGPSLocationForInstructorContract(instructorContract, false)
                                        if (authOrgUser.isGPSActive) {
                                            _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultProcessing
                                            val userLocation = locationManager.getCurrentLocation()
                                            val isGPSMatch = locationManager.isDeviceWithinOrganization(expectedLocation, userLocation, authOrgUser.radius)

                                            if (isGPSMatch){
                                                userLocation?.let {
                                                    if(!locationManager.isLocationMock(userLocation)) {
                                                        _teachingSessionValidationState.value =
                                                            TeachingSessionValidationState.GPSResultInsideOrganization(
                                                                "Bye ${
                                                                    result.faceRecognitionResult.name.split(
                                                                        " "
                                                                    ).last()
                                                                }"
                                                            )
                                                        Log.i(
                                                            "distrivite-ai-gps-verify",
                                                            "GPS Match: You are within the organization"
                                                        )
                                                        //checkInEmployee(result.faceRecognitionResult.id)
                                                        endDomainTeachingSession()
                                                        withContext(Dispatchers.IO) {
                                                            textToSpeechNotifier.speak(
                                                                NotificationEvent.Success("Succèss! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${
                                                                    result.faceRecognitionResult.name.split(
                                                                        " "
                                                                    ).last()
                                                                } votre cours est terminé.")
                                                            )
                                                            delay(500)
                                                        }
                                                        _teachingSessionAction.value = null
                                                        //_recognitionFinished.value = true
                                                        _teachingSessionActionExecutionState.value =
                                                            TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                        Log.i(
                                                            "distrivite-ai-gps",
                                                            "Success: You are in the organization"
                                                        )
                                                    }else {
                                                        Log.i(
                                                            "distrivite-ai-gps",
                                                            "Failure: You are in the organization"
                                                        )
                                                    }
                                                }
                                            }else if (userLocation != null) {
                                                withContext(Dispatchers.IO){
                                                    textToSpeechNotifier.speak(NotificationEvent.Failure("échec! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} allez proche de votre check-point et ré-éssayez!."))
                                                    delay(500)
                                                }
                                                _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultOutsideOrganization("You are not in the organization. You are trying bad things.", TeachingSessionAction.START_SESSION)
                                                _teachingSessionAction.value = null
                                                //_recognitionFinished.value = true
                                                Log.i("distrivite-ai-gps-verify", "GPS Not Match:You are not in the organization. You are trying bad things.")
                                            } else {
                                                withContext(Dispatchers.IO){
                                                    textToSpeechNotifier.speak(NotificationEvent.Failure("échec! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} activez votre gps et ré-éssayez!."))
                                                    delay(500)
                                                }
                                                _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultError(TeachingSessionAction.START_SESSION)
                                                _teachingSessionAction.value = null
                                                //_recognitionFinished.value = true
                                                Log.i("distrivite-ai-gps-verify", "GPS Failure: Failed to get gps data, please turn on your gps")
                                            }

                                        }else {
                                            _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultInsideOrganization("cours terminé ${result.faceRecognitionResult.name.split(" ").first()}")
                                            //checkInEmployee(result.faceRecognitionResult.id)
                                            endDomainTeachingSession()
                                            withContext(Dispatchers.IO){
                                                textToSpeechNotifier.speak(NotificationEvent.Success("Succèss! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} votre cours est terminé."))
                                                delay(500)
                                            }
                                            _teachingSessionAction.value = null
                                            //_showSuccessResult.value = false
                                            //_recognitionFinished.value = true
                                            _teachingSessionActionExecutionState.value =
                                                TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())
                                            Log.i("distrivite-ai-gps", "Success: You are in the organization")
                                        }
                                    }
                                }
                            }

                        }
                        TeachingSessionAction.ATTEND -> {

                            val recognitionResult = getStudentFaceRecognitionResultForFrame(faceFrame)
                            val faceRecognitionSecureResults = recognitionResult.faceRecognitionSecureResults
                            if (faceRecognitionSecureResults.isNotEmpty()) {
                                val result = faceRecognitionSecureResults[0]
                                _faceFrameRecognitionSecureResult.value = recognitionResult

                                //if (!result.faceRecognitionResult.isUnknownFace || result.faceRecognitionResult.isUnknownFace) {
                                if (!result.faceRecognitionResult.isUnknownFace && result.isFaceMatchWithSelectedItem) {
                                    val studentResponse = studentRepository.getDomainStudentById(result.faceRecognitionResult.id)
                                    studentResponse?.let { domainStudent ->
                                        _currentDomainTeachingSession.value?.let { domainTeachingSession ->
                                            val studentAttendance = studentAttendanceRepository.getAttendanceByStudentIdAndSessionId(studentId = domainStudent.id, sessionId = domainTeachingSession.id)
                                            val instructorContractResponse = instructorContractRepository.getDomainInstructorContractById(domainTeachingSession.instructorContractId)

                                            if (studentAttendance == null) {
                                                instructorContractResponse?.let { instructorContract ->
                                                    val expectedLocation = getCheckGPSLocationForInstructorContract(instructorContract, false)
                                                    if (authOrgUser.isGPSActive) {
                                                        _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultProcessing
                                                        val userLocation = locationManager.getCurrentLocation()
                                                        val isGPSMatch = locationManager.isDeviceWithinOrganization(expectedLocation, userLocation, authOrgUser.radius)
                                                        if (isGPSMatch){
                                                            userLocation?.let {
                                                                if(!locationManager.isLocationMock(userLocation)){
                                                                    _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultInsideOrganization("Bye ${result.faceRecognitionResult.name.last()}")
                                                                    Log.i("distrivite-ai-gps-verify", "GPS Match: You are within the organization")
                                                                    //checkInEmployee(result.faceRecognitionResult.id)
                                                                    attendDomainTeachingSessionForStudent(domainStudent)
                                                                    withContext(Dispatchers.IO){
                                                                        textToSpeechNotifier.speak(NotificationEvent.Success("Succèss! ${result.faceRecognitionResult.name.split(" ").last()} présent."))
                                                                        delay(500)
                                                                    }
                                                                    _teachingSessionAction.value = null
                                                                    //_recognitionFinished.value = true
                                                                    _teachingSessionActionExecutionState.value =
                                                                        TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                                    Log.i("distrivite-ai-gps", "Success: You are in the organization")
                                                                }else {
                                                                    Log.i("distrivite-ai-gps", "Failure: GPS spoofing detected")
                                                                }
                                                            }

                                                        }else if (userLocation != null) {
                                                            withContext(Dispatchers.IO){
                                                                textToSpeechNotifier.speak(NotificationEvent.Failure("échec! ${result.faceRecognitionResult.name.split(" ").last()} allez proche de votre check-point et ré-éssayez!."))
                                                                delay(500)
                                                            }
                                                            _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultOutsideOrganization("You are not in the organization. You are trying bad things.", TeachingSessionAction.START_SESSION)
                                                            _teachingSessionAction.value = null
                                                            //_recognitionFinished.value = true
                                                            Log.i("distrivite-ai-gps-verify", "GPS Not Match:You are not in the organization. You are trying bad things.")
                                                        } else {
                                                            withContext(Dispatchers.IO){
                                                                textToSpeechNotifier.speak(NotificationEvent.Failure("échec! ${result.faceRecognitionResult.name.split(" ").last()} activez votre gps et ré-éssayez!."))
                                                                delay(500)
                                                            }
                                                            _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultError(TeachingSessionAction.START_SESSION)
                                                            _teachingSessionAction.value = null
                                                            //_recognitionFinished.value = true
                                                            Log.i("distrivite-ai-gps-verify", "GPS Failure: Failed to get gps data, please turn on your gps")
                                                        }
                                                    }else {
                                                        _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultInsideOrganization("cours terminé ${result.faceRecognitionResult.name.split(" ")[0]}")
                                                        //checkInEmployee(result.faceRecognitionResult.id)
                                                        attendDomainTeachingSessionForStudent(domainStudent)
                                                        withContext(Dispatchers.IO){
                                                            textToSpeechNotifier.speak(NotificationEvent.Success("Succèss! ${result.faceRecognitionResult.name.split(" ").last()} présent."))
                                                            delay(500)
                                                        }
                                                        _teachingSessionAction.value = null
                                                        //_showSuccessResult.value = false
                                                        //_recognitionFinished.value = true
                                                        _teachingSessionActionExecutionState.value =
                                                            TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                        Log.i("distrivite-ai-gps", "Success: You are in the organization")
                                                    }
                                                }
                                            }else {
                                                Log.i("distrivite-student-attendances", "Failed: You have in already attended to this course")
                                                _teachingSessionAction.value = null
                                                //_showSuccessResult.value = false
                                                //_recognitionFinished.value = true
                                                _teachingSessionActionExecutionState.value =
                                                TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                _teachingSessionActionExecutionState.value =
                                                    TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                            }

                                        }
                                    }
                                }
                            }
                        }
                        TeachingSessionAction.APPROVE -> {

                            val recognitionResult = getStudentFaceRecognitionResultForFrame(
                                faceFrame,
                                isDelegateStudentOnly = authOrgUser.isAdmin
                            )

                            Log.i("authOrgUser.isDelegateValidationOnly", "${authOrgUser.isAdmin}")

                            val faceRecognitionSecureResults = recognitionResult.faceRecognitionSecureResults
                            if (faceRecognitionSecureResults.isNotEmpty()) {
                                val result = faceRecognitionSecureResults[0]
                                _faceFrameRecognitionSecureResult.value = recognitionResult

                                if (!result.faceRecognitionResult.isUnknownFace && result.isFaceMatchWithSelectedItem) {
                                    val student = studentRepository.getDomainStudentById(result.faceRecognitionResult.id)
                                    _currentDomainTeachingSession.value?.let { domainTeachingSession ->
                                        val instructorContractResponse = instructorContractRepository.getDomainInstructorContractById(domainTeachingSession.instructorContractId)
                                        instructorContractResponse?.let { instructorContract ->
                                            val expectedLocation = getCheckGPSLocationForInstructorContract(instructorContract, false)
                                            if (authOrgUser.isGPSActive) {
                                                _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultProcessing
                                                val userLocation = locationManager.getCurrentLocation()
                                                val isGPSMatch = locationManager.isDeviceWithinOrganization(expectedLocation, userLocation, authOrgUser.radius)
                                                if (isGPSMatch){
                                                    userLocation?.let {
                                                        if(!locationManager.isLocationMock(userLocation)){
                                                            _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultInsideOrganization("Bye ${result.faceRecognitionResult.name.last()}")
                                                            Log.i("distrivite-ai-gps-verify", "GPS Match: You are within the organization")
                                                            //checkInEmployee(result.faceRecognitionResult.id)
                                                            approveDomainTeachingSession()
                                                            withContext(Dispatchers.IO){
                                                                textToSpeechNotifier.speak(NotificationEvent.Success("Succèss! ${result.faceRecognitionResult.name.split(" ").last()} cours apprové."))
                                                                delay(500)
                                                            }
                                                            _teachingSessionAction.value = null
                                                //            //_recognitionFinished.value = true
                                                            _teachingSessionActionExecutionState.value =
                                                    TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                            _teachingSessionActionExecutionState.value =
                                                                TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                            Log.i("distrivite-ai-gps", "Success: You are in the organization")
                                                        }else {
                                                            Log.i("distrivite-ai-gps", "Failure: GPS spoofing detected")
                                                        }
                                                    }


                                                }else if (userLocation != null) {
                                                    withContext(Dispatchers.IO){
                                                        textToSpeechNotifier.speak(NotificationEvent.Failure("échec! ${result.faceRecognitionResult.name.split(" ").last()} allez proche de votre check-point et ré-éssayez!."))
                                                        delay(500)
                                                    }
                                                    _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultOutsideOrganization("You are not in the organization. You are trying bad things.", TeachingSessionAction.START_SESSION)
                                                    _teachingSessionAction.value = null
                                                //    //_recognitionFinished.value = true
                                                    _teachingSessionActionExecutionState.value =
                                                    TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                    Log.i("distrivite-ai-gps-verify", "GPS Not Match:You are not in the organization. You are trying bad things.")
                                                } else {
                                                    withContext(Dispatchers.IO){
                                                        textToSpeechNotifier.speak(NotificationEvent.Failure("échec! ${result.faceRecognitionResult.name.split(" ").last()} activez votre gps et ré-éssayez!."))
                                                        delay(500)
                                                    }
                                                    _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultError(TeachingSessionAction.START_SESSION)
                                                    _teachingSessionAction.value = null
                                                //    //_recognitionFinished.value = true
                                                    _teachingSessionActionExecutionState.value =
                                                    TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                    Log.i("distrivite-ai-gps-verify", "GPS Failure: Failed to get gps data, please turn on your gps")
                                                }

                                            }else {
                                                _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultInsideOrganization("cours terminé ${result.faceRecognitionResult.name.split(" ")[0]}")
                                                //checkInEmployee(result.faceRecognitionResult.id)
                                                approveDomainTeachingSession()
                                                withContext(Dispatchers.IO){
                                                    textToSpeechNotifier.speak(NotificationEvent.Success("Succèss! ${result.faceRecognitionResult.name.split(" ").last()} cours apprové."))
                                                    delay(500)
                                                }
                                                _teachingSessionAction.value = null
                                                //_showSuccessResult.value = false
                                                //_recognitionFinished.value = true
                                                _teachingSessionActionExecutionState.value =
                                                    TeachingSessionActionExecutionState.Finished(TeachingSessionScreenDestination())

                                                Log.i("distrivite-ai-gps", "Success: You are in the organization")
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        TeachingSessionAction.VIEW_ATTENDANCE -> {
                            val recognitionResult = getInstructorContractFaceRecognitionResultForFrame(faceFrame)
                            val faceRecognitionSecureResults = recognitionResult.faceRecognitionSecureResults
                            if (faceRecognitionSecureResults.isNotEmpty()) {
                                val result = faceRecognitionSecureResults[0]
                                _faceFrameRecognitionSecureResult.value = recognitionResult

                                if (!result.faceRecognitionResult.isUnknownFace && result.isFaceMatchWithSelectedItem) {
                                    val instructorContractResponse = instructorContractRepository.getDomainInstructorContractById(result.faceRecognitionResult.id)
                                    instructorContractResponse?.let { instructorContract ->
                                        val expectedLocation = getCheckGPSLocationForInstructorContract(instructorContract, false)
                                        if (authOrgUser.isGPSActive) {
                                            _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultProcessing
                                            val userLocation = locationManager.getCurrentLocation()
                                            val isGPSMatch = locationManager.isDeviceWithinOrganization(expectedLocation, userLocation, authOrgUser.radius)

                                            if (isGPSMatch){
                                                userLocation?.let {
                                                    if(!locationManager.isLocationMock(userLocation)) {
                                                        _teachingSessionValidationState.value =
                                                            TeachingSessionValidationState.GPSResultInsideOrganization(
                                                                "étudiants au cours de ${_currentDomainTeachingSession.value?.course}"
                                                            )
                                                        Log.i(
                                                            "distrivite-ai-gps-verify",
                                                            "GPS Match: You are within the organization"
                                                        )
                                                        //checkInEmployee(result.faceRecognitionResult.id)
                                                        //endDomainTeachingSession()
                                                        withContext(Dispatchers.IO) {
                                                            textToSpeechNotifier.speak(
                                                                NotificationEvent.Success("étudiants au cours de ${_currentDomainTeachingSession.value?.course}")
                                                            )
                                                            delay(500)
                                                        }
                                                        _teachingSessionAction.value = null
                                                        //_recognitionFinished.value = true
                                                        _teachingSessionActionExecutionState.value =
                                                            TeachingSessionActionExecutionState.Finished(
                                                                TeachingSessionDetailScreenDestination()
                                                            )
                                                        Log.i(
                                                            "distrivite-ai-gps",
                                                            "Success: You are in the organization"
                                                        )
                                                    }else {
                                                        Log.i(
                                                            "distrivite-ai-gps",
                                                            "Failure: You are in the organization"
                                                        )
                                                    }
                                                }
                                            }else if (userLocation != null) {
                                                withContext(Dispatchers.IO){
                                                    textToSpeechNotifier.speak(NotificationEvent.Failure("échec! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} allez proche de votre check-point et ré-éssayez!."))
                                                    delay(500)
                                                }
                                                _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultOutsideOrganization("You are not in the organization. You are trying bad things.", TeachingSessionAction.START_SESSION)
                                                _teachingSessionAction.value = null
                                                //_recognitionFinished.value = true
                                                Log.i("distrivite-ai-gps-verify", "GPS Not Match:You are not in the organization. You are trying bad things.")
                                            } else {
                                                withContext(Dispatchers.IO){
                                                    textToSpeechNotifier.speak(NotificationEvent.Failure("échec! ${getTeacherTitle(result.faceRecognitionResult.name.split(" ").first())} ${result.faceRecognitionResult.name.split(" ").last()} activez votre gps et ré-éssayez!."))
                                                    delay(500)
                                                }
                                                _teachingSessionValidationState.value = TeachingSessionValidationState.GPSResultError(TeachingSessionAction.VIEW_ATTENDANCE)
                                                _teachingSessionAction.value = null
                                                //_recognitionFinished.value = true
                                                Log.i("distrivite-ai-gps-verify", "GPS Failure: Failed to get gps data, please turn on your gps")
                                            }

                                        }else {
                                            _teachingSessionValidationState.value =
                                                TeachingSessionValidationState.GPSResultInsideOrganization(
                                                    "étudiants au cours de ${_currentDomainTeachingSession.value?.course}"
                                                )
                                            Log.i(
                                                "distrivite-ai-gps-verify",
                                                "GPS Match: You are within the organization"
                                            )
                                            //checkInEmployee(result.faceRecognitionResult.id)
                                            //endDomainTeachingSession()
                                            withContext(Dispatchers.IO) {
                                                textToSpeechNotifier.speak(
                                                    NotificationEvent.Success("étudiants au cours de ${_currentDomainTeachingSession.value?.course}")
                                                )
                                                delay(500)
                                            }
                                            _teachingSessionAction.value = null
                                            //_showSuccessResult.value = false
                                            //_recognitionFinished.value = true
                                            _teachingSessionActionExecutionState.value =
                                                TeachingSessionActionExecutionState.Finished(
                                                    TeachingSessionDetailScreenDestination()
                                                )
                                            Log.i("distrivite-ai-gps", "Success: You are in the organization")
                                        }
                                    }
                                }
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

    private suspend fun getStudentFaceRecognitionResultForFrame(faceFrame: FaceFrame, isDelegateStudentOnly:Boolean=false): FaceFrameRecognitionSecureResult {
        val faceFrameRecognitionSecureResult = secureRecognizeStudentFaces(faceFrame = faceFrame, isDelegateStudentOnly)
        return checkStudentFaceMatchWithSelectedItem(faceFrameRecognitionSecureResult)
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
            Log.i("tiqtaq-facematch-test", "${it.faceRecognitionResult.id} and ${_currentDomainTeachingSession.value?.instructorContractId}::::${it.faceRecognitionResult.id == _currentDomainTeachingSession.value?.instructorContractId}")
            FaceRecognitionSecureResult(
                isFaceMatchWithSelectedItem = it.faceRecognitionResult.id == _currentDomainTeachingSession.value?.instructorContractId,
                faceRecognitionResult = it.faceRecognitionResult,
                isSecure = it.isSecure,
                face = it.face
            )
        }
        return FaceFrameRecognitionSecureResult(
            faceFrameRecognitionSecureResult.faceFrame, checkedFaceRecognitionSecureResults
        )
    }



    private suspend fun checkStudentFaceMatchWithSelectedItem(faceFrameRecognitionSecureResult:FaceFrameRecognitionSecureResult) : FaceFrameRecognitionSecureResult {
        val checkedFaceRecognitionSecureResults = faceFrameRecognitionSecureResult.faceRecognitionSecureResults.map {
            val student = studentRepository.getDomainStudentById(it.faceRecognitionResult.id)
            FaceRecognitionSecureResult(
                isFaceMatchWithSelectedItem = student?.educationClassId == _currentDomainTeachingSession.value?.educationClassId,
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

    private suspend fun secureRecognizeStudentFaces(faceFrame: FaceFrame, isDelegateStudentOnly:Boolean) : FaceFrameRecognitionSecureResult = withContext(
        Dispatchers.Default) {
        val faceRecognitionSecureResults = mutableListOf<FaceRecognitionSecureResult>()

        val studentFaces = if (!isDelegateStudentOnly) studentKnownFaces else delegateStudentKnownFaces

        _authOrgUser.value?.let {
            for (face in faceFrame.faces){
                val faceRecognitionSecureResult = faceRecognitionSecurePipeLine.recognize(faceFrame.bitmap, face, studentFaces, isLivenessActive = it.isLivenessActive)
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
        setupNotificationBus()
        observeOrganization()
        observeFaceFrame()
        observeLocalRoomsData()
        observeLocalTeachingPeriodsData()
        observeLocalTeachingCoursesData()
        observeLocalTeachingSessionsData()
        observeLocalStudentAttendancesData()
        observeLocalInstructorContractsData()
        observeLocalStudentsData()
    }
    private fun observeFaceFrame() {

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

    fun onRefresh() {
        Log.i("classairefresh", "refreshcing by liedjify")
        authOrgUser.value?.let {
            syncLocalDataWithServer(it.orgSlug)
        }
    }

    private fun observeOrganization() = viewModelScope.launch(Dispatchers.IO) {

        authUserFlow.collectLatest { collectedAuthOrgUser ->

            collectedAuthOrgUser?.let {
                Log.i("distrivite_first_comsummer2","token changed from cosumer claim ${it.orgSlug}")
                _authOrgUser.value = it
                onRefresh()
            }
        }
    }

    fun updateTeachingCourseQuery(query: String) {
        _teachingCourseQuery.value = query
    }

    private fun observeLocalTeachingPeriodsData() {
        viewModelScope.launch {
            teachingPeriodRepository.getTeachingPeriodsFlow()
                .catch { error ->
                    //_teachingPeriodsUiState.value = TeachingPeriodsUiState.Error(error.message ?: "Unknown Error")
                }
                .collect { teachingPeriods ->
                    _teachingPeriods.value = teachingPeriods
                    for ( teachingPeriod in _teachingPeriods.value) {
                        Log.i("distrivite-log-period", "${teachingPeriod.start} to ${teachingPeriod.end}")
                    }
                }
        }
    }


    private fun observeLocalRoomsData() {
        viewModelScope.launch {
            roomRepository.getRoomsFlow()
                .catch { error ->
                    //_roomsUiState.value = TeachingPeriodsUiState.Error(error.message ?: "Unknown Error")
                }
                .collect { rooms ->
                    _rooms.value = rooms
                }
        }
    }

    private fun observeLocalStudentAttendancesData() {
        viewModelScope.launch {
            studentAttendanceRepository.getAttendancesAsFlow()
                .catch { error ->
                    //_roomsUiState.value = TeachingPeriodsUiState.Error(error.message ?: "Unknown Error")
                }
                .collect { studentAttendances ->
                    _studentAttendances.value = studentAttendances
                    Log.i("distrivite-studentAttendances", "studentAttendances loaded successfully...${studentAttendances.size}")

                }
        }
    }
    private fun observeLocalTeachingSessionsData() {
        viewModelScope.launch {
            teachingSessionRepository.getTeachingSessionsAsFlow()
                .catch { error ->
                    _teachingSessionsUiState.value = TeachingSessionsUiState.Error(error.message ?: "Unknown Error")
                    Log.e("distriviteteachingsessions", "Teaching sessions failed from room... by liedji222${error.message}")
                }
                .collect { teachingSessions ->
                    _teachingSessionsUiState.value = TeachingSessionsUiState.Success(teachingSessions = teachingSessions)
                    Log.i("distriviteteachingsessions", "Teaching sessions loaded successfully... by liedji222${teachingSessions.size}")
                }
        }
    }


    private fun observeLocalTeachingCoursesData() {
        viewModelScope.launch {
            teachingCourseRepository.getDomainTeachingCoursesFlow()
                .catch { error ->
                    error.printStackTrace()
                }
                .collect { teachingCourses ->
                    _teachingCourses.value = teachingCourses
                    Log.i("distrivite-claims", "Teaching courses loaded successfully...${teachingCourses.size.toString()}")
                }
        }
    }
    private fun syncLocalDataWithServer(organization: String) {
        _isRefreshing.launchLoading(viewModelScope) {
            roomRepository.syncRooms(organization)
            teachingPeriodRepository.syncTeachingPeriods(organization)
            syncOrchestrator.push(organization)
            notificationOrchestrator.notify(organization)
            syncOrchestrator.pullAllInParallel(organization)
        }
    }

    private fun observeLocalInstructorContractsData() {
        viewModelScope.launch {
            instructorContractRepository.getDomainInstructorContractsFlow()
                .catch {
                    // error fetching organizations users
                }
                .collect { domainsInstructorContracts ->
                    updateKnownFacesFromInstructorContracts(domainsInstructorContracts)
                }
        }
    }



    private fun observeLocalStudentsData() {
        viewModelScope.launch {
            studentRepository.getDomainStudentsFlow()
                .catch {
                    // error fetching organizations users
                }
                .collect { domainsStudents ->
                    _students.value = domainsStudents
                    updateKnownFacesFromStudents(domainsStudents)
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

    private fun updateKnownFacesFromStudents(students:List<DomainStudent>) {
        studentKnownFaces.clear()
        delegateStudentKnownFaces.clear()
        for(student in students){
            val knownFace = KnownFace(id = student.id, student.name, embeddings = convertToFloatArrayList(student.embeddings))
            Log.i("distrivite-ai", "$knownFace loaded successfully")
            studentKnownFaces.add(knownFace)
            if (student.isDelegate) delegateStudentKnownFaces.add(knownFace)
        }
    }

    private fun convertToFloatArrayList(listOfLists: List<List<Float>>): List<FloatArray> {
        return listOfLists.map { it.toFloatArray() }
    }


    fun onDomainTeachingSessionStart(domainTeachingSession: DomainTeachingSession){
        //_recognitionFinished.value = false
        _teachingSessionActionExecutionState.value =TeachingSessionActionExecutionState.Started
        _currentDomainTeachingSession.value = domainTeachingSession
        _teachingSessionAction.value = TeachingSessionAction.START_SESSION
        _teachingSessionValidationState.value = TeachingSessionValidationState.FaceScanning
        Log.i("tiqtaq-facematch-test", "${domainTeachingSession.instructorContractId} selected successfully")
    }

    fun onDomainTeachingSessionCreate(domainTeachingSession: DomainTeachingSession){
        //_recognitionFinished.value = false
        _teachingSessionActionExecutionState.value =TeachingSessionActionExecutionState.Started
        _currentDomainTeachingSession.value = domainTeachingSession
        _teachingSessionAction.value = TeachingSessionAction.CREATE
        _teachingSessionValidationState.value = TeachingSessionValidationState.FaceScanning
        Log.i("tiqtaq-facematch-test", "${domainTeachingSession.instructorContractId} selected successfully")
    }

    fun onRetrySessionItemAction(teachingSessionAction: TeachingSessionAction) {
        _currentDomainTeachingSession.value?.let {
            when (teachingSessionAction) {
                TeachingSessionAction.START_SESSION -> {
                    onDomainTeachingSessionStart(it)
                }
                TeachingSessionAction.END_SESSION -> {
                    onDomainTeachingSessionEnd(it)
                }
                TeachingSessionAction.APPROVE -> {
                    onDomainTeachingSessionApprove(it)
                }
                TeachingSessionAction.CREATE -> {
                    onDomainTeachingSessionCreate(it)
                }
                else -> {}
            }
        }
    }

    private fun startDomainTeachingSession() {
        _currentDomainTeachingSession.value?.let {
            viewModelScope.launch {
                teachingSessionRepository.start(it)
                //createAttendanceForAllStudents(it)
                syncOrchestrator.push(it.orgSlug)
                notificationOrchestrator.notify(it.orgSlug)
            }
        }
    }

    fun onDomainTeachingSessionEnd(domainTeachingSession: DomainTeachingSession){
        //_recognitionFinished.value = false
        _teachingSessionActionExecutionState.value =TeachingSessionActionExecutionState.Started
        _currentDomainTeachingSession.value = domainTeachingSession
        _teachingSessionAction.value = TeachingSessionAction.END_SESSION
        _teachingSessionValidationState.value = TeachingSessionValidationState.FaceScanning
        Log.i("tiqtaq-facematch-test", "${domainTeachingSession.instructorContractId} selected successfully")
    }

    private fun endDomainTeachingSession() {
        _currentDomainTeachingSession.value?.let {
            viewModelScope.launch {
                teachingSessionRepository.end(it)
                syncOrchestrator.push(it.orgSlug)
                notificationOrchestrator.notify(it.orgSlug)
            }
        }
    }

    fun onDomainTeachingSessionAttend(domainTeachingSession: DomainTeachingSession){
        //_recognitionFinished.value = false
        _teachingSessionActionExecutionState.value =TeachingSessionActionExecutionState.Started
        _teachingSessionAction.value = TeachingSessionAction.ATTEND
        _currentDomainTeachingSession.value = domainTeachingSession
        _teachingSessionValidationState.value = TeachingSessionValidationState.FaceScanning
        Log.i("tiqtaq-facematch-test", "${domainTeachingSession.instructorContractId} selected successfully")
    }

    fun onDomainTeachingSessionApprove(domainTeachingSession: DomainTeachingSession){
        //_recognitionFinished.value = false
        _teachingSessionActionExecutionState.value =TeachingSessionActionExecutionState.Started
        _teachingSessionAction.value = TeachingSessionAction.APPROVE
        _currentDomainTeachingSession.value = domainTeachingSession
        _teachingSessionValidationState.value = TeachingSessionValidationState.FaceScanning
        Log.i("tiqtaq-facematch-test", "${domainTeachingSession.instructorContractId} selected successfully")
    }


    private fun attendDomainTeachingSessionForStudent(domainStudent: DomainStudent) {
        _currentDomainTeachingSession.value?.let { session ->
            viewModelScope.launch {
                studentAttendanceRepository.createAttendance(
                    DomainStudentAttendance(
                        id = UUID.randomUUID().toString(),
                        created = LocalDateTime.now().toString(),
                        modified = LocalDateTime.now().toString(),
                        orgId = session.orgId,
                        orgSlug = session.orgSlug,
                        registerAt = LocalDateTime.now().toString(),
                        studentName = domainStudent.name,
                        studentId = domainStudent.id,
                        sessionId = session.id,
                        isPresent = true,
                        sessionName = session.course,
                        syncStatus = SyncStatus.PENDING,
                        educationClassId = session.educationClassId
                    )
                )
            }
        }
    }


    private fun approveDomainTeachingSession() {
        _currentDomainTeachingSession.value?.let {
            viewModelScope.launch {
                teachingSessionRepository.approve(it)
                syncOrchestrator.push(it.orgSlug)
                notificationOrchestrator.notify(it.orgSlug)
            }
        }
    }

    private fun createTeachingSession() {
        _authOrgUser.value?.let { authOrgUser ->
            viewModelScope.launch {
                _currentDomainTeachingSession.value?.let {
                    teachingSessionRepository.createTeachingSession(it)
                    createAttendanceForAllStudents(it)
                    syncOrchestrator.push(authOrgUser.orgSlug)
                    notificationOrchestrator.notify(it.orgSlug)
                    Log.i("teachingSessionMapper", "teachingSessionMapper ${it.id} ${it.rStart} ${it.rEnd}")
                }
            }
        }
    }


    fun deleteTeachingSession(domainTeachingSession: DomainTeachingSession) {
        _authOrgUser.value?.let {
            viewModelScope.launch {
                teachingSessionRepository.deleteTeachingSession(domainTeachingSession)
                syncOrchestrator.push(it.orgSlug)
                notificationOrchestrator.notify(it.orgSlug)
            }
        }
    }



    fun onSelectFilterOption(filterOption: FilterOption){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val filteredTeachingSessions = teachingSessionRepository.getTeachingSessionsForFilterOption(filterOption)
                    _teachingSessionsUiState.value = TeachingSessionsUiState.Success(filteredTeachingSessions)
                    Log.i("distrivite-search", "search completed!")
                }catch (e:Exception) {
                    _teachingSessionsUiState.value = TeachingSessionsUiState.Error("")
                }
            }
        }
    }
    fun onSearchQueryChanged(searchQuery:String, filterOption: FilterOption) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val searchedTeachingSessions = teachingSessionRepository.getTeachingSessionsFor(searchQuery, filterOption)
                    _teachingSessionsUiState.value = TeachingSessionsUiState.Success(searchedTeachingSessions)
                    Log.i("distrivite-search", "search completed!")
                }catch (e:Exception) {
                    _teachingSessionsUiState.value = TeachingSessionsUiState.Error("")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeechNotifier.shutdown()
    }

    private fun getTeacherTitle(title:String) : String {
        return if(title.contains("Dr")) { "Docteur" }
        else if (title.contains("Prof")) "Professeur"
        else if (title.contains("Msc")) "Maître"
        else if (title.contains("Mme")) "Madame"
        else if (title.contains("Ing")) "Ingenieur"
        else "Monsieur"
    }

    fun onViewDetails(domainTeachingSession: DomainTeachingSession) {
        viewModelScope.launch {
            //_recognitionFinished.value = false
            _teachingSessionActionExecutionState.value = TeachingSessionActionExecutionState.Started
            _teachingSessionAction.value = TeachingSessionAction.VIEW_ATTENDANCE
            _currentDomainTeachingSession.value = domainTeachingSession
            _teachingSessionValidationState.value = TeachingSessionValidationState.FaceScanning
            Log.i("tiqtaq-facematch-test", "${domainTeachingSession.instructorContractId} selected successfully")
        }
    }

    fun toggleAttendance(domainStudentAttendance: DomainStudentAttendance) {

        viewModelScope.launch(Dispatchers.IO) {
            _currentDomainTeachingSession.value?.let {
                try {
                    val now = LocalDateTime.now().toString()

                    studentAttendanceRepository.updateAttendance(
                        domainStudentAttendance.copy(
                            isPresent = !domainStudentAttendance.isPresent,
                            modified = now
                        )
                    )
                    syncOrchestrator.push(it.orgSlug)
                    notificationOrchestrator.notify(it.orgSlug)
                    Log.i("AttendanceToggle", "Attendance toggled for ${domainStudentAttendance.studentName}")
                } catch (e: Exception) {
                    Log.e("AttendanceToggle", "Failed to toggle attendance for ${domainStudentAttendance.studentName}", e)
                }
            }

        }
    }

    private suspend fun toggleAllPresent(isPresent: Boolean) {
        _currentDomainTeachingSession.value?.let {
            if (studentSessionAttendances.value.isNotEmpty()) {
                for (attendance in studentSessionAttendances.value) {
                    studentAttendanceRepository.updateAttendance(
                        attendance.copy(
                            isPresent = isPresent
                        )
                    )
                }
                syncOrchestrator.push(it.orgSlug)
            }
        }
    }

    fun toggleMarkAllAttendance() {
        viewModelScope.launch {
            if (isAllPresent.value)  toggleAllPresent(false)
            else toggleAllPresent(true)
        }
    }

    private suspend fun createAttendanceForAllStudents(domainTeachingSession: DomainTeachingSession) {
        val classStudents = studentRepository.getDomainStudentsByClassId(domainTeachingSession.educationClassId)

        if (classStudents.isNotEmpty()) {
            for (student in classStudents) {
                val attendance = DomainStudentAttendance(
                    id = UUID.randomUUID().toString(),
                    created = LocalDateTime.now().toString(),
                    modified = LocalDateTime.now().toString(),
                    orgId = domainTeachingSession.orgId,
                    orgSlug = domainTeachingSession.orgSlug,
                    registerAt = LocalDateTime.now().toString(),
                    studentName = student.name,
                    studentId = student.id,
                    sessionId = domainTeachingSession.id,
                    isPresent = false,
                    sessionName = domainTeachingSession.course,
                    syncStatus = SyncStatus.PENDING,
                    educationClassId = domainTeachingSession.educationClassId
                )
                studentAttendanceRepository.createAttendance(attendance)
                Log.i("createAttendanceForAllStudents", "createAttendanceForAllStudents student ${attendance.studentId}")

            }
        }else {
            Log.i("createAttendanceForAllStudents", "createAttendanceForAllStudents is empty for class ${domainTeachingSession.educationClassId}")
        }
    }

    private fun setupNotificationBus(){
        viewModelScope.launch {
            notificationBus.events.collect { event ->
                _notificationState.value = event
                textToSpeechNotifier.speak(event)
                delay(6000)
                _notificationState.value = null
            }
        }
    }


    fun showNotification(event: NotificationEvent) {
        _notificationState.value = event
    }

    fun clearNotification() {
        _notificationState.value = null
    }
}