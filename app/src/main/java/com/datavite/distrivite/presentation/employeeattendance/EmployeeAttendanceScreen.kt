package com.datavite.distrivite.presentation.employeeattendance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.datavite.distrivite.app.BottomNavigationBar
import com.datavite.distrivite.presentation.components.TiqtaqTopBar
import com.ramcosta.composedestinations.generated.destinations.EmployeeAttendanceRecognitionScreenDestination
import com.ramcosta.composedestinations.generated.destinations.EmployeeAttendanceScreenDestination
import com.datavite.distrivite.presentation.ErrorScreen
import com.datavite.distrivite.presentation.LoadingScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun EmployeeAttendanceScreen(navigator: DestinationsNavigator, viewModel: EmployeeAttendanceViewModel) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    // Access the NavController from the LocalNavController
    val route = EmployeeAttendanceScreenDestination.route

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TiqtaqTopBar(scrollBehavior = scrollBehavior, destinationsNavigator = navigator,
                onSearchQueryChanged = {},
                onSearchClosed = {},
                onSync = {

                }
            )
        },
        bottomBar = {
            BottomNavigationBar(route =route, destinationsNavigator = navigator)
        }
    ) { paddings ->
        // Initialize the ViewModel
        Box (modifier = Modifier
            .fillMaxSize()
            .padding(paddings)) {
            val attendanceUiState by viewModel.attendancesUiState.collectAsState()
            when (attendanceUiState) {
                is EmployeeAttendancesUiState.Loading ->  {
                    LoadingScreen(onCancel = {})
                }
                is EmployeeAttendancesUiState.Success -> {
                    val attendances =  (attendanceUiState as EmployeeAttendancesUiState.Success).attendances
                    AttendanceList(
                        attendanceRecords = attendances,
                        onCheckInClick = {
                            viewModel.onCheckIn()
                            navigator.navigate(EmployeeAttendanceRecognitionScreenDestination())
                        },
                        onCheckOutClick = {
                            viewModel.onCheckOut()
                            navigator.navigate(EmployeeAttendanceRecognitionScreenDestination())
                        }
                    )
                }
                is EmployeeAttendancesUiState.Error ->  {
                    ErrorScreen(retryAction = { /*TODO*/ })
                }
            }
        }
    }
}