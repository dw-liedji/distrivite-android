package com.datavite.distrivite.presentation.signOut

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.domain.repository.auth.JwtRepository
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignOutViewModel @Inject constructor (
    private  val jwtRepository: JwtRepository,
    private val authenticatedOrgUserCredentialManager: AuthOrgUserCredentialManager
) : ViewModel() {
    var state: SignOutState by mutableStateOf(
        SignOutState()
    )
        private set


    private fun signOut(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            try {
                jwtRepository.deleteAllTokens()
                state = state.copy(isLoading = false)
                state = state.copy(isSignedOut = true)

            }catch (e:Exception){
                state = state.copy(isLoading = false)
                state = state.copy(isSignedOut = false)
            }

        }
    }

    fun onEvent(signOutEvent: SignOutEvent) {
        when(signOutEvent) {
          is SignOutEvent.SubmitButtonClicked -> {
              signOut()
          }
        }
    }


}