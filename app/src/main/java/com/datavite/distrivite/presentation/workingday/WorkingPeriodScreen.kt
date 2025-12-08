package com.datavite.distrivite.presentation.workingday

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.ramcosta.composedestinations.generated.destinations.WorkingPeriodScreenDestination
import com.datavite.distrivite.domain.model.DomainWorkingPeriod
import com.datavite.distrivite.presentation.ErrorScreen
import com.datavite.distrivite.presentation.LoadingScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun WorkingPeriodScreen(navigator: DestinationsNavigator, viewModel: WorkingPeriodViewModel) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    // Access the NavController from the LocalNavController
    val route = WorkingPeriodScreenDestination.route

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TiqtaqTopBar(scrollBehavior = scrollBehavior, destinationsNavigator = navigator,
                onSearchQueryChanged = {},
                onSearchClosed = {},
                onSync = {}
            )
        },
        bottomBar = {
            BottomNavigationBar(route =route, destinationsNavigator = navigator)
        }
    ) { paddings ->
        // Initialize the ViewModel
        Box (modifier = Modifier.fillMaxSize().padding(paddings)) {
            val workingPeriodUiState by viewModel.workingPeriodsUiState.collectAsState()
            when (workingPeriodUiState) {
                is WorkingPeriodsUiState.Loading ->  {
                    LoadingScreen(onCancel = {})
                }
                is WorkingPeriodsUiState.Success -> {
                    val workingPeriods =  (workingPeriodUiState as WorkingPeriodsUiState.Success).workingPeriods
                    DomainWorkingPeriodList(
                        workingPeriods = workingPeriods
                    )
                }
                is WorkingPeriodsUiState.Error ->  {
                    ErrorScreen(retryAction = { /*TODO*/ })
                }
            }

        }
    }
}

@Composable
fun DomainWorkingPeriodList(workingPeriods: List<DomainWorkingPeriod>, modifier:Modifier=Modifier) {
    LazyColumn(
        modifier = modifier
    ) {

        items(
            items =workingPeriods,
            key = { domainWorkingPeriod -> domainWorkingPeriod.id }) {
                domainWorkingPeriod -> WorkingPeriodCard(
                id = domainWorkingPeriod.id,
                created = domainWorkingPeriod.created,
                modified = domainWorkingPeriod.modified,
                orgSlug = domainWorkingPeriod.orgSlug,
                day = domainWorkingPeriod.day,
                startTime = domainWorkingPeriod.startTime,
                endTime = domainWorkingPeriod.endTime,
                isActive = domainWorkingPeriod.isActive
                )
        }
    }
}
