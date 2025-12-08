package com.datavite.distrivite.presentation.session

import FilterOption
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.datavite.distrivite.app.BottomNavigationBar
import com.datavite.distrivite.presentation.components.PullToRefreshBox
import com.datavite.distrivite.presentation.components.TiqtaqTopBar
import com.datavite.distrivite.presentation.ErrorScreen
import com.datavite.distrivite.presentation.LoadingScreen
import com.datavite.distrivite.presentation.components.NotificationHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AddSessionScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TeachingSessionRecognitionScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TeachingSessionScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun TeachingSessionScreen(
    navigator: DestinationsNavigator,
    viewModel: TeachingSessionViewModel
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val context = LocalContext.current
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val notification by viewModel.notificationState

    val route = TeachingSessionScreenDestination.route
    val selectedOption = remember { mutableStateOf(FilterOption.TODAY) }
    var searchQuery by remember { mutableStateOf("") }

    val authOrgUser by viewModel.authOrgUser.collectAsState()
    val teachingSessionsUiState by viewModel.teachingSessionsUiState.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TiqtaqTopBar(
                scrollBehavior = scrollBehavior,
                destinationsNavigator = navigator,
                onSearchQueryChanged = {
                    searchQuery = it
                    viewModel.onSearchQueryChanged(it, selectedOption.value)
                },
                onSearchClosed = {
                    searchQuery = it
                    selectedOption.value = FilterOption.TODAY
                    viewModel.onSelectFilterOption(selectedOption.value)
                },
                onSync = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://m.facebook.com/profile.php?id=61555380762150".toUri()
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(route = route, destinationsNavigator = navigator)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigator.navigate(AddSessionScreenDestination())
                }
            ) {
                Text("+") // Replace with Icon if preferred
            }
        }
    ) { paddings ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.onRefresh()
            },
            modifier = Modifier.fillMaxSize().padding(paddings)
        ) {

            Box(modifier = Modifier.fillMaxSize()) {
                when (teachingSessionsUiState) {
                    is TeachingSessionsUiState.Loading -> {
                        LoadingScreen(onCancel = {})
                    }
                    is TeachingSessionsUiState.Success -> {
                        authOrgUser?.let { authOrgUser ->
                            val teachingSessions = (teachingSessionsUiState as TeachingSessionsUiState.Success).teachingSessions
                            DomainTeachingSessionList(
                                onSelectFilterOption = {
                                    selectedOption.value = it
                                    if (searchQuery.isEmpty()) {
                                        viewModel.onSelectFilterOption(it)
                                    } else viewModel.onSearchQueryChanged(searchQuery, it)
                                },
                                authOrgUser = authOrgUser,
                                currentSelectedFilterOption = selectedOption.value,
                                teachingSessions = teachingSessions,
                                onTeachingStart = {
                                    viewModel.onDomainTeachingSessionStart(it)
                                    navigator.navigate(TeachingSessionRecognitionScreenDestination())
                                },
                                onTeachingEnd = {
                                    viewModel.onDomainTeachingSessionEnd(it)
                                    navigator.navigate(TeachingSessionRecognitionScreenDestination())
                                },
                                onAttend = {
                                    viewModel.onDomainTeachingSessionAttend(it)
                                    navigator.navigate(TeachingSessionRecognitionScreenDestination())
                                },
                                onTeachingApprove = {
                                    viewModel.onDomainTeachingSessionApprove(it)
                                    navigator.navigate(TeachingSessionRecognitionScreenDestination())
                                },
                                onClick = {
                                    viewModel.onViewDetails(it)
                                    navigator.navigate(TeachingSessionRecognitionScreenDestination())
                                    //navigator.navigate(TeachingSessionDetailScreenDestination())
                                }
                            )
                        }
                    }
                    is TeachingSessionsUiState.Error -> {
                        ErrorScreen(retryAction = { /* TODO */ })
                    }
                }
            }

            // Notification overlay floats on top of everything
            NotificationHost(
                notificationEvent = notification,
                onDismiss = { viewModel.clearNotification() },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    authOrgUser?.let {
        if (it.isGPSActive) {
            EnsureGPSIsEnabled(
                onGPSEnabled = {
                    // TODO: Handle GPS enabled
                },
                onGPSDisabled = { errorMessage ->
                    // TODO: Handle GPS disabled
                },
                context = context
            )
        }
    }
}
