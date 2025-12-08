package com.datavite.distrivite.presentation.student

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import com.datavite.distrivite.app.BottomNavigationBar
import com.datavite.distrivite.presentation.components.PullToRefreshBox
import com.datavite.distrivite.presentation.components.TiqtaqTopBar
import com.ramcosta.composedestinations.generated.destinations.StudentRegisterFaceScreenDestination
import com.ramcosta.composedestinations.generated.destinations.StudentScreenDestination
import com.datavite.distrivite.presentation.ErrorScreen
import com.datavite.distrivite.presentation.LoadingScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

//@ContributeNavGraph(start = true)
@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun StudentScreen(
    navigator: DestinationsNavigator,
    viewModel: StudentViewModel
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    // Access the NavController from the LocalNavController
    val route = StudentScreenDestination.route
    val context = LocalContext.current

    val studentsUiState by viewModel.studentsUiState.collectAsState()
    val selectedStudent by viewModel.selectedDomainStudent.collectAsState()
    val showSuccessAlert by viewModel.showSuccessAlert.collectAsState()
    val authOrgUser by viewModel.authOrgUser.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            // Trigger reload when pulling to refresh
            viewModel.onRefresh()
        },

    ) {
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
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://m.facebook.com/profile.php?id=61555380762150")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                )
            },
            bottomBar = {
                BottomNavigationBar(route =route, destinationsNavigator = navigator)
            }
        ) { paddings ->

            // Initialize the ViewModel
            Box (modifier = Modifier
                .fillMaxSize().padding(paddings)
            ) {

                when (studentsUiState) {
                    is StudentsUiState.Loading -> {
                        LoadingScreen(onCancel = {})
                    }

                    is StudentsUiState.Success -> {
                        val successStudentsUiState = (studentsUiState as StudentsUiState.Success)
                        authOrgUser?.let { orgUser  ->
                            StudentList(
                                authOrgUser = orgUser,
                                domainStudents = successStudentsUiState.domainStudents,
                                onClickDeleteEmbeddings = {
                                    viewModel.onDeleteStudentEmbeddings(it)
                                },
                                onClickLearn = {
                                    viewModel.onRegisterFace(it)
                                    navigator.navigate(StudentRegisterFaceScreenDestination())
                                }
                            )
                        }
                    }

                    is StudentsUiState.Error -> {
                        val errorStudentsUiState = (studentsUiState as StudentsUiState.Error)
                        val errorMessage = errorStudentsUiState.message
                        ErrorScreen(retryAction = { /*TODO*/ })
                    }

                }

                if (showSuccessAlert) {
                    DeleteFaceSuccessAlert()
                }
            }
        }
    }
}


@Composable
fun DeleteFaceSuccessAlert() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Face removed successfully!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}