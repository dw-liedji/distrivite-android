package com.datavite.distrivite.presentation.student

import com.datavite.distrivite.domain.model.DomainStudent

sealed class StudentsUiState {
    data object Loading : StudentsUiState()
    data class Success(val domainStudents: List<DomainStudent>) : StudentsUiState()
    data class Error(val message: String) : StudentsUiState()
}
