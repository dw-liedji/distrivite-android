package com.datavite.distrivite.presentation.signOut

sealed class SignOutEvent () {
    data object SubmitButtonClicked: SignOutEvent()
}