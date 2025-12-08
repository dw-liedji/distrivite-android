package com.datavite.distrivite.presentation.instructorContract

sealed class InstructorContractValidationState {
    data object FaceScanning : InstructorContractValidationState()
    data object GPSResultProcessing : InstructorContractValidationState()
    data class GPSResultInsideOrganization(val message: String) : InstructorContractValidationState()
    data class GPSResultOutsideOrganization(val message: String, val sessionAction:InstructorContractAction) : InstructorContractValidationState()
    data class GPSResultError(val sessionAction:InstructorContractAction) : InstructorContractValidationState()
}
