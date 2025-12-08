package com.datavite.distrivite.presentation.instructorContract

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
import com.datavite.distrivite.presentation.ErrorScreen
import com.datavite.distrivite.presentation.LoadingScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.InstructorContractRecognitionScreenDestination
import com.ramcosta.composedestinations.generated.destinations.InstructorContractRegisterFaceScreenDestination
import com.ramcosta.composedestinations.generated.destinations.InstructorContractScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

//@ContributeNavGraph(start = true)
@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun InstructorContractScreen(
    navigator: DestinationsNavigator,
    viewModel: InstructorContractViewModel
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    // Access the NavController from the LocalNavController
    val route = InstructorContractScreenDestination.route
    val context = LocalContext.current

    val instructorContractsUiState by viewModel.instructorContractsUiState.collectAsState()
    val selectedInstructorContract by viewModel.selectedDomainInstructorContract.collectAsState()
    val showSuccessAlert by viewModel.showSuccessAlert.collectAsState()
    val authOrgUser by viewModel.authOrgUser.collectAsState()
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

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                // Trigger reload when pulling to refresh
                viewModel.onRefresh()
            },

            modifier = Modifier.padding(paddings)
        ) {

            // Initialize the ViewModel
            Box (modifier = Modifier
                .fillMaxSize()
                ) {


                when (instructorContractsUiState) {
                    is InstructorContractsUiState.Loading -> {
                        LoadingScreen(onCancel = {})
                    }

                    is InstructorContractsUiState.Success -> {
                        val successInstructorContractsUiState = (instructorContractsUiState as InstructorContractsUiState.Success)
                        authOrgUser?.let { orgUser  ->
                            InstructorContractList(
                                authOrgUser = orgUser,
                                domainInstructorContracts = successInstructorContractsUiState.domainInstructorContracts,
                                onClickDeleteEmbeddings = {
                                    viewModel.onDeleteInstructorContractEmbeddings(it)
                                },
                                onClickRegister = {
                                    viewModel.onRegisterFace(it)
                                    navigator.navigate(InstructorContractRegisterFaceScreenDestination())
                                },
                                onClickReport = {
                                    viewModel.onLoadSessionReportForInstructorContract(it)
                                    navigator.navigate(InstructorContractRecognitionScreenDestination())
                                }
                            )
                        }
                    }

                    is InstructorContractsUiState.Error -> {
                        val errorInstructorContractsUiState = (instructorContractsUiState as InstructorContractsUiState.Error)
                        val errorMessage = errorInstructorContractsUiState.message
                        ErrorScreen(retryAction = { /*TODO*/ })
                    }

                    else -> {}
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