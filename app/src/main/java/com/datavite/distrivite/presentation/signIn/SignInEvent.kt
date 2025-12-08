package com.datavite.distrivite.presentation.signIn

sealed class SignInEvent {
    data class EmailChanged(val value: String): SignInEvent()
    data class PasswordChanged(val value: String): SignInEvent()
    data object SubmitButtonClicked: SignInEvent()
}