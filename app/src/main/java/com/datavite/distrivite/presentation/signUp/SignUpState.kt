package com.datavite.distrivite.presentation.signUp

data class SignUpState (
    val isLoading: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val email: String = "",
    val password: String = "",
)