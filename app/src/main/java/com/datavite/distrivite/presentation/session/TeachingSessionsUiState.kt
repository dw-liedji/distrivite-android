package com.datavite.distrivite.presentation.session

import com.datavite.distrivite.domain.model.DomainTeachingSession

sealed class TeachingSessionsUiState {
    data object Loading : TeachingSessionsUiState()
    data class Success(val teachingSessions: List<DomainTeachingSession>) : TeachingSessionsUiState()
    data class Error(val message: String) : TeachingSessionsUiState()
}
