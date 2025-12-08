package com.datavite.distrivite.presentation.leave

import com.datavite.distrivite.domain.model.DomainLeave

enum class LEAVE_ACTIONS {
    CREATE,
    APPROVE,
    REJECT,
}
sealed class LeavesUiState {
    data object Loading : LeavesUiState()
    data class Success(val leaves: List<DomainLeave>) : LeavesUiState()
    data class Error(val message: String) : LeavesUiState()
}
