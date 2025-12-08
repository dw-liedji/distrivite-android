package com.datavite.distrivite.presentation.logout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.database.AppDatabase
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrgSignOutViewModel @Inject constructor (
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager,
    private val pendingOperationDao: PendingOperationDao,
    private val database: AppDatabase
) : ViewModel() {
    // State for showing success alert
    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    private val __totalPendingOperation = MutableStateFlow(0)
    val  totalPendingOperation: StateFlow<Int> = __totalPendingOperation.asStateFlow()

    init {
        observePendingOperations()
    }


    fun observePendingOperations() {
        viewModelScope.launch {
            authOrgUserCredentialManager.sharedAuthOrgUserFlow.collectLatest {
                    authOrgUser ->
                pendingOperationDao.getAllPendingOperationsFlow().collect {
                        pendingOperations ->
                    __totalPendingOperation.value = pendingOperations.size
                }
            }
        }
    }


    fun signOut(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.clearAllTables()
                authOrgUserCredentialManager.deleteAuthOrgUser()
                _isLoggedOut.value = true
            }catch (e:Exception){
                _isLoggedOut.value = false
            }

        }
    }
}