package com.datavite.distrivite.presentation.instructorContract

import com.datavite.distrivite.domain.model.DomainInstructorContract

sealed class InstructorContractsUiState {
    data object Loading : InstructorContractsUiState()
    data class Success(val domainInstructorContracts: List<DomainInstructorContract>) : InstructorContractsUiState()
    data class Error(val message: String) : InstructorContractsUiState()
}
