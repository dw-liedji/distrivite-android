package com.datavite.distrivite.presentation.student

sealed class StudentRegistrationState {
    data object ScanningFace : StudentRegistrationState()
    data object SavingSingleFace : StudentRegistrationState()
    data class SavingAllFaces(val message: String) : StudentRegistrationState()
    data class AllFacesSaved(val message: String) : StudentRegistrationState()
    data object RegistrationCompleted : StudentRegistrationState()
}
