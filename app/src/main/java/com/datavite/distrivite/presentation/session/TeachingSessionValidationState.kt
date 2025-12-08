package com.datavite.distrivite.presentation.session

sealed class TeachingSessionValidationState {
    data object FaceScanning : TeachingSessionValidationState()
    data object GPSResultProcessing : TeachingSessionValidationState()
    data class GPSResultInsideOrganization(val message: String) : TeachingSessionValidationState()
    data class GPSResultOutsideOrganization(val message: String, val sessionAction:TeachingSessionAction) : TeachingSessionValidationState()
    data class GPSResultError(val sessionAction:TeachingSessionAction) : TeachingSessionValidationState()
}
