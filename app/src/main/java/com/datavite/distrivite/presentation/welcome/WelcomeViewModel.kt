package com.datavite.distrivite.presentation.welcome

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.data.local.datasource.auth.LocalJwtDataSource
import com.datavite.distrivite.data.remote.datasource.auth.RemoteAuthDataSource
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUserRequest
import com.datavite.distrivite.domain.repository.auth.UserRepository
import com.datavite.distrivite.data.remote.clients.Response
import com.datavite.distrivite.presentation.signIn.AuthenticationState
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.location.geofence.GeofenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val remoteAuthRepository: RemoteAuthDataSource,
    private  val localJwtRepository: LocalJwtDataSource,
    private val userRepository: UserRepository,
    private val geofenceRepository: GeofenceRepository,
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager
) : ViewModel() {

    private val _welcomeUiState = MutableStateFlow(WelcomeUiState("", "", organizationCredential = ""))
    val welcomeUiState: StateFlow<WelcomeUiState> = _welcomeUiState

    val authUserFlow = authOrgUserCredentialManager.sharedAuthOrgUserFlow

    private val _authenticationState = MutableStateFlow<AuthenticationState>(AuthenticationState.Welcome)
    val authenticationState: StateFlow<AuthenticationState> = _authenticationState

    // State for showing success alert
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            authUserFlow
                .collectLatest {
                authOrgUser -> if (authOrgUser != null) {
                    updateAuthOrgUser(authOrgUser.userId, authOrgUser.orgCredential)
                    _isLoggedIn.value = true
                    /*
                    geofenceRepository.addGeofence(
                        it.id,
                        Location("manuel").apply {
                            latitude = 4.063232
                            longitude = 9.732096
                        },
                        radiusInMeters = 5000f,
                        expirationTimeInMillis = 1000 * 60 * 3
                    )
                    geofenceRepository.registerAllGeofence()
                    */
                    _authenticationState.value =   AuthenticationState.Authenticated("Connected Successfully")
                }else {
                    _authenticationState.value = AuthenticationState.AuthInitial
                }
            }
        }
    }

    fun updateEmailCredential(emailCredential: String) {
        _welcomeUiState.value = _welcomeUiState.value.copy(emailCredential = emailCredential)
    }

    fun updateOrganizationCredential(organizationCredential: String) {
        _welcomeUiState.value = _welcomeUiState.value.copy(organizationCredential = organizationCredential)
    }

    fun updatePasswordCredential(passwordCredential: String) {
        _welcomeUiState.value = _welcomeUiState.value.copy(passwordCredential = passwordCredential)
    }

    fun signIn() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO){

                _authenticationState.value = AuthenticationState.Loading

                val result = remoteAuthRepository.signIn(
                    email = _welcomeUiState.value.emailCredential,
                    password = _welcomeUiState.value.passwordCredential
                )

                when(result){
                    is Response.Authorized -> {
                        result.data.let { auth ->

                            localJwtRepository.saveAccessToken(auth.access)
                            localJwtRepository.saveRefreshToken(auth.refresh)

                            when(val userResponse = userRepository.getUserProfile()) {
                                is Response.Authorized -> {
                                    updateAuthOrgUser(userResponse.data.id, _welcomeUiState.value.organizationCredential)
                                }
                                else ->  {

                                }
                            }
                        }

                    }
                    is Response.UnAuthorized -> {
                        _authenticationState.value =
                            AuthenticationState.UnAuthenticated("Failed to authenticate")
                    }
                    is Response.UnknownError -> {
                        _authenticationState.value =
                            AuthenticationState.Error("An error occurred when signing in, please try again later..")
                    }
                }


            }
        }
    }

    private suspend fun updateAuthOrgUser(userId:String, orgCredential:String) {
        when(val authOrgUser= userRepository.authOrgUser(
            AuthOrgUserRequest(
                credential = orgCredential,
                userId = userId
            )
        )) {
            is Response.Authorized -> {
                authOrgUser.data.let {
                    authOrgUserCredentialManager.saveAuthOrgUser(it)
                    _authenticationState.value= AuthenticationState.Authenticated("Connected Successfully")
                }
            }
            is Response.UnknownError -> {
                Log.i("tiqtaq-token","Something when wrong! ${authOrgUser.errorMsg}")

            }
            is Response.UnAuthorized -> {
                Log.i("tiqtaq-token","Your credential are wrong! ${authOrgUser.errorMsg}")

            }
        }

    }
}