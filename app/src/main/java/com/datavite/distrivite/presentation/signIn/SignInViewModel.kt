package com.datavite.distrivite.presentation.signIn

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.data.remote.clients.Response
import com.datavite.distrivite.data.local.datasource.auth.LocalJwtDataSource
import com.datavite.distrivite.data.remote.datasource.auth.RemoteAuthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor (
    private val remoteAuthRepository: RemoteAuthDataSource,
    private  val localJwtRepository: LocalJwtDataSource
) : ViewModel() {
    var state: SignInState by mutableStateOf(
        SignInState(isLoading = true)
    )
        private set

    private val _authenticationState = MutableStateFlow<AuthenticationState>(AuthenticationState.AuthInitial)
    val authenticationState: StateFlow<AuthenticationState> = _authenticationState


    init {
        authenticate()
    }

    private fun signIn(){
        viewModelScope.launch {

            state = state.copy(isLoading = true)
            state = state.copy(tryCount = state.tryCount.inc())


            val result = remoteAuthRepository.signIn(
                email = state.email,
                password = state.password
            )

            when(result){
                is Response.Authorized -> {
                    result.data?.let {
                        localJwtRepository.saveAccessToken(it.access)
                        localJwtRepository.saveRefreshToken(it.refresh)
                        _authenticationState.value = AuthenticationState.Authenticated(it.access)
                    }
                }
                is Response.UnAuthorized -> {
                    _authenticationState.value =
                        AuthenticationState.UnAuthenticated("${state.tryCount}")
                }

                is Response.UnknownError -> {
                    _authenticationState.value =
                        AuthenticationState.Error("An error occurred when signing in, please try again later...${state.tryCount}")
                }
            }

            state = state.copy(isLoading = false)

        }
    }

    fun onEvent(signInEvent: SignInEvent) {
        when(signInEvent) {
            is SignInEvent.EmailChanged -> {
                state = state.copy(email = signInEvent.value)
            }
            is SignInEvent.PasswordChanged -> {
                state = state.copy(password = signInEvent.value)
            }
            is SignInEvent.SubmitButtonClicked -> {
                signIn()
            }
        }
    }

    private fun authenticate(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val access = localJwtRepository.getAccessToken().first()
            val refresh = localJwtRepository.getRefreshToken().first()

            if (!refresh.isNullOrEmpty() && !access.isNullOrEmpty()){
                _authenticationState.value = AuthenticationState.Authenticated(access)
                Log.i("LogIn", "Already Authenticated")
            }else {
                _authenticationState.value = AuthenticationState.AuthInitial
                Log.i("LogIn", "UnAuthenticated")
            }

            state = state.copy(isLoading = false)
        }

    }

}