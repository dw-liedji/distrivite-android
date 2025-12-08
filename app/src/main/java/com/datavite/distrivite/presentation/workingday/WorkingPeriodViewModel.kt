package com.datavite.distrivite.presentation.workingday

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.network.NetworkStatusMonitor
import com.datavite.distrivite.domain.repository.WorkingPeriodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkingPeriodViewModel @Inject constructor(
    private val workingPeriodRepository: WorkingPeriodRepository,
    private val networkStatusMonitor: NetworkStatusMonitor,
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager
): ViewModel(){

    private val _workingPeriodsUiState = MutableStateFlow<WorkingPeriodsUiState>(
        WorkingPeriodsUiState.Loading)
    val workingPeriodsUiState: StateFlow<WorkingPeriodsUiState> = _workingPeriodsUiState

    val authUserFlow = authOrgUserCredentialManager.sharedAuthOrgUserFlow

    // State to hold the currently selected user for facial recognition
    private val _organization = MutableStateFlow<AuthOrgUser?>(null)
    val organization: StateFlow<AuthOrgUser?> = _organization

    init {
        observeOrganization()
        observeLocalWorkingPeriodsData()
    }

    private fun observeOrganization() = viewModelScope.launch(Dispatchers.IO) {
        authUserFlow.collectLatest { organization ->
            organization?.let {
                _organization.value = it
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

    private fun observeLocalWorkingPeriodsData() {
        viewModelScope.launch {
            workingPeriodRepository.getWorkingPeriodsFlow()
                .catch { error ->
                    _workingPeriodsUiState.value = WorkingPeriodsUiState.Error(error.message ?: "Unknown Error")
                }
                .collect { workingPeriods ->
                    Log.i("workingPeriods", workingPeriods.toString())
                    _workingPeriodsUiState.value = WorkingPeriodsUiState.Success(workingPeriods = workingPeriods)
                }
        }
    }

    private fun syncLocalDataWithServer(organization: String) {
        viewModelScope.launch(Dispatchers.IO) {
            //_teachingSessionsUiState.value = OrganizationUsersUiState.Loading
            try {
                workingPeriodRepository.syncWorkingPeriods(organization)
            } catch (e: Exception) {
                // _teachingSessionsUiState.value = OrganizationUsersUiState.Error(e.message ?: "Sync Failed")
            }
        }
    }

}