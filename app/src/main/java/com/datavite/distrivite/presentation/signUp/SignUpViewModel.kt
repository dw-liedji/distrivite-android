package com.datavite.distrivite.presentation.signUp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.data.remote.datasource.auth.RemoteAuthDataSource
import com.datavite.distrivite.data.remote.model.auth.AuthSignUpRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor (private val remoteAuthRepository: RemoteAuthDataSource) : ViewModel() {
    var state by mutableStateOf(SignUpState())

    private fun signUp(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = remoteAuthRepository.signUp(
                AuthSignUpRequest(
                first_name = state.firstName,
                last_name = state.lastName,
                birth_date = state.birthDate,
                email = state.email,
                password = state.password
            )
            )
            state = state.copy(isLoading = false)
        }
    }

    fun onEvent(event: SignUpEvent) {
        when(event) {
            is SignUpEvent.FirstNameChanged -> {
                state = state.copy(firstName = event.value)
            }
            is SignUpEvent.LastNameChanged -> {
                state = state.copy(lastName = event.value)
            }
            is SignUpEvent.EmailChanged -> {
                state = state.copy(email = event.value)
            }
            is SignUpEvent.BirthDateChanged -> {
                state = state.copy(birthDate = event.value)
            }
            is SignUpEvent.PasswordChanged -> {
                state = state.copy(password = event.value)
            }
            is SignUpEvent.SubmitButtonClicked -> {
                signUp()
            }
            else -> {}
        }
    }
}