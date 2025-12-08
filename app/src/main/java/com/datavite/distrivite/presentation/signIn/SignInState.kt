package com.datavite.distrivite.presentation.signIn

data class SignInState (
    val isLoading: Boolean = false,
    val tryCount: Int = 0,
    val email: String = "",
    val password: String = ""
)