package com.datavite.distrivite.presentation.employee

import com.datavite.distrivite.domain.model.DomainEmployee

sealed class EmployeesUiState {
    data object Loading : EmployeesUiState()
    data class Success(val employees: List<DomainEmployee>) : EmployeesUiState()
    data class Error(val message: String) : EmployeesUiState()
}
