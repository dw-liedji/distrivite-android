package com.datavite.distrivite.presentation.signUp

sealed class SignUpEvent () {
    data class FirstNameChanged(val value: String): SignUpEvent()
    data class LastNameChanged(val value: String): SignUpEvent()
    data class BirthDateChanged(val value: String): SignUpEvent()
    data class EmailChanged(val value: String): SignUpEvent()
    data class PasswordChanged(val value: String): SignUpEvent()
    data object SubmitButtonClicked: SignUpEvent()
}