package com.datavite.distrivite.presentation.teachingcourse

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.datavite.distrivite.app.BottomNavigationBar
import com.datavite.distrivite.presentation.components.PullToRefreshBox
import com.datavite.distrivite.presentation.components.TiqtaqTopBar
import com.ramcosta.composedestinations.generated.destinations.TeachingCourseScreenDestination
import com.datavite.distrivite.presentation.ErrorScreen
import com.datavite.distrivite.presentation.LoadingScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun TeachingCourseScreen(navigator: DestinationsNavigator, viewModel: TeachingCourseViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    // Access the NavController from the LocalNavController
    val route = TeachingCourseScreenDestination.route
    val authOrgUser by viewModel.authOrgUser.collectAsState()
    val teachingCourseUiState by viewModel.teachingCoursesUiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TiqtaqTopBar(scrollBehavior = scrollBehavior, destinationsNavigator = navigator,
                onSearchQueryChanged = {
                    viewModel.onSearchQueryChanged(it)
                },
                onSearchClosed = {
                    viewModel.onSearchQueryChanged("")
                },
                onSync = {
                    viewModel.onRefresh()
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(route =route, destinationsNavigator = navigator)
        },
        floatingActionButton = {
        }
    ) { paddings ->
        // Initialize the ViewModel
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                // Trigger reload when pulling to refresh
                viewModel.onRefresh()
            },

            modifier = Modifier.padding(paddings)
        ) {
            Box (modifier = Modifier
                .fillMaxSize()
                ) {
                when (teachingCourseUiState) {
                    is TeachingCoursesUiState.Loading ->  {
                        LoadingScreen(onCancel = {})
                    }
                    is TeachingCoursesUiState.Success -> {
                        authOrgUser?.let {
                            val teachingCourses =  (teachingCourseUiState as TeachingCoursesUiState.Success).teachingCourses
                            TeachingCourseList(
                                teachingCourses = teachingCourses,
                                authOrgUser= it,
                            )
                        }
                    }
                    is TeachingCoursesUiState.Error ->  {
                        ErrorScreen(retryAction = { /*TODO*/ })
                    }
                }
            }
        }
    }
}
