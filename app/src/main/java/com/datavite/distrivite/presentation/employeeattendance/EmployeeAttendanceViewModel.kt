package com.datavite.distrivite.presentation.employeeattendance
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
import com.datavite.distrivite.data.mapper.StudentAttendanceMapper
import com.datavite.distrivite.data.notification.TextToSpeechNotifier
import com.datavite.distrivite.domain.model.DomainEmployee
import com.datavite.distrivite.domain.notification.NotificationEvent
import com.datavite.distrivite.domain.repository.StudentAttendanceRepository
import com.datavite.distrivite.domain.repository.EmployeeRepository
import com.datavite.distrivite.domain.repository.HolidayRepository
import com.datavite.distrivite.domain.repository.LeaveRepository
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class EmployeeAttendanceViewModel @Inject constructor(
    private val textToSpeechNotifier: TextToSpeechNotifier,
    private val attendanceRepository: StudentAttendanceRepository,
    private val employeeRepository: EmployeeRepository,
    private val holidayRepository: HolidayRepository,
    private val leaveRepository: LeaveRepository,
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager,
    private val workingPeriodRepository: WorkingPeriodRepository,
    private val networkStatusMonitor: NetworkStatusMonitor,
    private val attendanceMapper: StudentAttendanceMapper,
    private val locationManager: LocationManager,
    private val faceDetectorStreamAnalyser: FaceDetectorStreamAnalyser,
    private val faceRecognitionSecurePipeLine: FaceRecognitionSecurePipeLine,
    ): ViewModel() {
    private val _attendancesUiState = MutableStateFlow<EmployeeAttendancesUiState>(
        EmployeeAttendancesUiState.Loading)
    val attendancesUiState: StateFlow<EmployeeAttendancesUiState> = _attendancesUiState

    // State to hold the currently selected user for facial recognition
    private val _authOrgUser = MutableStateFlow<AuthOrgUser?>(null)
    val authOrgUser: StateFlow<AuthOrgUser?> = _authOrgUser

    val authUserFlow = authOrgUserCredentialManager.sharedAuthOrgUserFlow


    private val _attendanceAction = MutableStateFlow<ATTENDANCE_ACTIONS?>(null)
    val attendanceAction: StateFlow<ATTENDANCE_ACTIONS?> = _attendanceAction

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
        _attendanceAction.value?.let {
            // update teaching session on the server
            //updateTeachingSessionForSelected()
            withContext(Dispatchers.IO){
                when (it) {
                    ATTENDANCE_ACTIONS.CHECK_IN -> {

                        val recognitionResult = getFaceRecognitionResultForFrame(faceFrame)
                        val faceRecognitionSecureResults = recognitionResult.faceRecognitionSecureResults
                        if (faceRecognitionSecureResults.isNotEmpty()) {
                            val result = faceRecognitionSecureResults[0]
                            _faceFrameRecognitionSecureResult.value = recognitionResult

                            if (!result.faceRecognitionResult.isUnknownFace) {

                                _showSuccessResult.value = true
                                checkInEmployee(result.faceRecognitionResult.id)
                                withContext(Dispatchers.IO){
                                    textToSpeechNotifier.speak(NotificationEvent.Success("Welcome ${result.faceRecognitionResult.name.split(" ")[0]}"))
                                    delay(500)
                                }
                                _attendanceAction.value = null
                                _showSuccessResult.value = false
                                _recognitionFinished.value = true
                                Log.i("distrivite-ai-gps", "Success: You are in the organization")
                            }
                        }

                    }
                    ATTENDANCE_ACTIONS.CHECK_OUT -> {
                        val recognitionResult = getFaceRecognitionResultForFrame(faceFrame)
                        val faceRecognitionSecureResults = recognitionResult.faceRecognitionSecureResults
                        if (faceRecognitionSecureResults.isNotEmpty()) {
                            val result = faceRecognitionSecureResults[0]
                            _faceFrameRecognitionSecureResult.value = recognitionResult

                            if (!result.faceRecognitionResult.isUnknownFace) {

                                _showSuccessResult.value = true
                                checkOutEmployee(result.faceRecognitionResult.id)
                                withContext(Dispatchers.IO){
                                    textToSpeechNotifier.speak(NotificationEvent.Success("Bye Bye ${result.faceRecognitionResult.name.split(" ")[0]}"))
                                    delay(500)
                                }
                                _attendanceAction.value = null
                                _showSuccessResult.value = false
                                _recognitionFinished.value = true
                                Log.i("distrivite-ai-gps", "Success: You are in the organization")
                            }
                        }
                    }
                    //ATTENDANCE_ACTIONS.REJECT -> {}
                    //LEAVE_ACTIONS.EDIT -> {
                    // updateClaim()
                    //}
                }
            }
        }
    }

    fun onCheckIn(){
        _recognitionFinished.value = false
        _attendanceAction.value = ATTENDANCE_ACTIONS.CHECK_IN
    }

    fun onCheckOut(){
        _recognitionFinished.value = false
        _attendanceAction.value = ATTENDANCE_ACTIONS.CHECK_OUT
    }

    private fun checkInEmployee(id: String) {

        _authOrgUser.value?.let { authOrgUser ->
            Log.i("distrivite-ati", "check in one time $id")
            viewModelScope.launch {
                val employee = employeeRepository.getEmployeeById(id)
                employee?.let {

                    val dayOfWeek = LocalDateTime.now().toLocalDate().dayOfWeek.toString()
                    val date = LocalDateTime.now().toLocalDate().toString()
                    val holiday = holidayRepository.getHolidayForDate(date)
                    val leave = leaveRepository.getLeaveForEmployeeOnDate(employee.id, date)

                    if (holiday == null) {
                        if (leave == null) {
                            val workingPeriods = workingPeriodRepository.getWorkingPeriodsByIdsForDay(it.workingDays, getIdDayName(dayOfWeek))
                            val totalWorkingHoursInMonth = totalWorkingHoursInMonth(it)

                            if (workingPeriods.isNotEmpty()) {
                                //val attendance = attendanceRepository.getAttendanceByEmployeeIdAndDay(employeeId =id, day = LocalDateTime.now().toLocalDate().toString())
                                //Log.i("holiday_distrivite", "date ${LocalDateTime.now().toLocalDate()} ${attendance?.day} working hours: $totalWorkingHoursInMonth")
                                /*if (attendance != null){
                                    val employeeAtt = attendanceMapper.mapLocalToDomain(attendance.copy(
                                        checkInTime = LocalDateTime.now().toLocalTime().toString(),
                                        modified = LocalDateTime.now().toString()
                                    ))
                                    attendanceRepository.updateAttendance(authOrgUser.orgSlug, employeeAtt)
                                }else {
                                    Log.i("distrivite-ati", "employee ${it.id} day is ${workingPeriods[0].startTime}")

                                    /*attendanceRepository.createAttendance(authOrgUser.orgSlug,
                                        DomainStudentAttendance(
                                            id=  UUID.randomUUID().toString(),
                                            orgId = authOrgUser.id,
                                            orgSlug = authOrgUser.orgSlug,
                                            employeeId = it.id,
                                            userId = it.userId,
                                            name = it.name,
                                            created = LocalDateTime.now().toString(),
                                            modified = LocalDateTime.now().toString(),
                                            day = LocalDateTime.now().toLocalDate().toString(),
                                            checkInTime = LocalDateTime.now().toLocalTime().toString(),
                                            checkOutTime = "None",
                                            startTime = workingPeriods[0].startTime,
                                            endTime = workingPeriods[0].endTime,
                                            hourlySalary = it.monthlySalary / totalWorkingHoursInMonth
                                        )
                                    )*/
                                }*/
                            }else {
                                Log.i("holiday_distrivite", "Today is not a working day for you")
                            }
                        }else {
                            Log.i("holiday_distrivite", "You left for ${leave.type}")
                        }
                    } else {
                        Log.i("holiday_distrivite", "this is a holyday")
                    }
                }
            }
        }
        Log.i("distrivite-attendance","checkIn success for $id")
    }


    private fun checkOutEmployee(id: String) {
        _authOrgUser.value?.let {authOrgUser ->
            Log.i("distrivite-ati", "check in one time ${id}")

            viewModelScope.launch {
                /*val attendance = attendanceRepository.getAttendanceByEmployeeIdAndDay(id, LocalDateTime.now().toLocalDate().toString())
                attendance?.let {
                    val userAtt = attendanceMapper.mapLocalToDomain(it.copy(checkOutTime = LocalDateTime.now().toLocalTime().toString()))
                    attendanceRepository.updateAttendance(authOrgUser.orgSlug, userAtt)
                }*/

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
        observeLocalEmployeeAttendancesData()
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
        authUserFlow.collectLatest { collectedAuthUser ->
            collectedAuthUser?.let {
                Log.i("distrivite_first_comsummer","token changed from cosumer attendance ${it.orgSlug}")
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

    private fun observeLocalEmployeeAttendancesData() {
        viewModelScope.launch {
            attendanceRepository.getAttendancesAsFlow()
                .catch { error ->
                    _attendancesUiState.value = EmployeeAttendancesUiState.Error(error.message ?: "Unknown Error")
                }
                .collect { users ->
                    _attendancesUiState.value = EmployeeAttendancesUiState.Success(attendances = users)
                }
        }
    }

    private fun syncLocalDataWithServer(organization: String) {
        viewModelScope.launch(Dispatchers.IO) {
            //_teachingSessionsUiState.value = EmployeesUiState.Loading
            try {
               // attendanceRepository.syncAttendances(organization)
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

    private fun getIdDayName(day: String): Int {
        return when (day) {
            "MONDAY" -> 0
            "TUESDAY" -> 1
            "WEDNESDAY" -> 2
            "THURSDAY" -> 3
            "FRIDAY" -> 4
            "SATURDAY" -> 5
            "SUNDAY" -> 6
            else -> throw IllegalArgumentException("Unknown day: $day")
        }
    }

    private suspend fun totalWorkingHoursInMonth(domainEmployee: DomainEmployee, year: Int? = null, month: Int? = null): Double {
        // Use current year and month as defaults if not provided
        val currentDate = LocalDate.now()
        val effectiveYear = year ?: currentDate.year
        val effectiveMonth = month ?: currentDate.monthValue

        // Get the total number of days in the month
        val numDaysInMonth = YearMonth.of(effectiveYear, effectiveMonth).lengthOfMonth()

        // Create a map to store the total hours for each weekday
        val hoursPerWeekday = mutableMapOf<Int, Double>().apply {
            for (i in 1..7) {
                this[i] = 0.0
            }
        }

        val workingDays = workingPeriodRepository.getWorkingPeriodsByIds(domainEmployee.workingDays)


        // Calculate hours per weekday
        for (workingPeriod in workingDays) {
            // Parse the ISO 8601 strings to LocalDateTime
            val startTime = LocalTime.parse(workingPeriod.startTime)
            val endTime = LocalTime.parse(workingPeriod.endTime)

            Log.i("distrivite_hour", "day :${workingPeriod.dayId}  ${workingPeriod.day} starttime: ${startTime} and endtime: ${endTime}")

            val totalMinutes = ChronoUnit.MINUTES.between(startTime, endTime)

            val totalHours = totalMinutes / 60.0

            // Store total hours for this weekday
            hoursPerWeekday[workingPeriod.dayId+1] = hoursPerWeekday[workingPeriod.dayId+1]?.plus(totalHours) ?: totalHours
        }

        // Calculate total working hours in the month
        var totalHoursInMonth = 0.0

        for (day in 1..numDaysInMonth) {
            // Determine the day of the week for each day in the month
            val date = LocalDate.of(effectiveYear, effectiveMonth, day)
            val dayOfWeek = date.dayOfWeek.value // 1 = Monday, 7 = Sunday

            // Add the corresponding hours for that weekday
            totalHoursInMonth += hoursPerWeekday[dayOfWeek] ?: 0.0
        }

        return totalHoursInMonth
    }




    private fun stopSpeak(){
       textToSpeechNotifier.shutdown()
    }

    override fun onCleared() {
        super.onCleared()
        stopSpeak()
    }
}