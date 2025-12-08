package com.datavite.distrivite.presentation.instructorContract

sealed class InstructorContractRegistrationState {
    data object ScanningFace : InstructorContractRegistrationState()
    data object SavingSingleFace : InstructorContractRegistrationState()
    data class SavingAllFaces(val message: String) : InstructorContractRegistrationState()
    data class AllFacesSaved(val message: String) : InstructorContractRegistrationState()
    data object RegistrationCompleted : InstructorContractRegistrationState()
}
