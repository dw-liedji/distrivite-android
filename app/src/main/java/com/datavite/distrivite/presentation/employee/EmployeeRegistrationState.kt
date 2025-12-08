package com.datavite.distrivite.presentation.employee


sealed class EmployeeRegistrationState {
    data object ScanningFace : EmployeeRegistrationState()
    data object SavingSingleFace : EmployeeRegistrationState()
    data class SavingAllFaces(val message: String) : EmployeeRegistrationState()
    data class AllFacesSaved(val message: String) : EmployeeRegistrationState()
    data object RegistrationCompleted : EmployeeRegistrationState()
}
