package com.datavite.distrivite.presentation.signIn

sealed class AuthenticationState {
    object AuthInitial : AuthenticationState()
    object Loading : AuthenticationState()
    object Welcome : AuthenticationState()
    data class UnAuthenticated(val unAuthenticatedMessage: String) : AuthenticationState()
    data class Authenticated(val authMessage: String) : AuthenticationState()
    data class Error(val errorMessage: String) : AuthenticationState()
}
