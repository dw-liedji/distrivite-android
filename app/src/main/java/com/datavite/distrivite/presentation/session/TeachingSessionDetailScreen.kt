package com.datavite.distrivite.presentation.session

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.app.BottomNavigationBar
import com.datavite.distrivite.presentation.components.TiqtaqCloudTopBar
import com.ramcosta.composedestinations.generated.destinations.TeachingSessionScreenDestination
import com.datavite.distrivite.domain.model.DomainTeachingSession
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun TeachingSessionDetailScreen(
    navigator: DestinationsNavigator,
    viewModel: TeachingSessionViewModel
) {
    val currentSession by viewModel.currentDomainTeachingSession.collectAsState()
    val sessionStudents by viewModel.sessionStudents.collectAsState()
    val studentAttendances by viewModel.studentSessionAttendances.collectAsState()

    val isAllPresent by viewModel.isAllPresent.collectAsState()
    val totalStudents by viewModel.totalStudents.collectAsState()
    val presentCount by viewModel.presentCount.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val currentRoute = TeachingSessionScreenDestination.route

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TiqtaqCloudTopBar(
                scrollBehavior = scrollBehavior,
                destinationsNavigator = navigator,
                onSearchQueryChanged = {},
                onSearchClosed = {},
                onSync = { /* Add refresh logic if needed */ },
                onBackPressed = {
                    navigator.navigate(TeachingSessionScreenDestination())
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                route = currentRoute,
                destinationsNavigator = navigator
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Session Header
            SessionDetailHeader(session = currentSession)

            Spacer(modifier = Modifier.height(8.dp))

            // Attendance List
            SessionAttendanceList(
                studentSessionAttendances = studentAttendances,
                onToggle = { student ->
                    viewModel.toggleAttendance(student)
                },
                onToggleAll = {
                    viewModel.toggleMarkAllAttendance()
                },
                isAllPresent = isAllPresent,
                presentCount = presentCount,
                totalStudents = totalStudents
            )
        }

        // Handle hardware/system back button
        BackHandler {
            navigator.navigate(TeachingSessionScreenDestination())
        }
    }
}

@Composable
private fun SessionDetailHeader(session: DomainTeachingSession?) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            session?.let {
                Text(
                    text = it.instructor,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${it.klass} - ${it.course}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
