package com.datavite.distrivite.presentation.workingday

import com.datavite.distrivite.domain.model.DomainWorkingPeriod

sealed class WorkingPeriodsUiState {
    data object Loading : WorkingPeriodsUiState()
    data class Success(val workingPeriods: List<DomainWorkingPeriod>) : WorkingPeriodsUiState()
    data class Error(val message: String) : WorkingPeriodsUiState()
}
