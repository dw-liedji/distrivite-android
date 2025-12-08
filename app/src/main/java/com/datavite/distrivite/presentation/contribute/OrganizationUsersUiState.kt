package com.datavite.distrivite.presentation.contribute

import com.datavite.distrivite.domain.model.DomainOrganizationUser

sealed class OrganizationUsersUiState {
    data object Loading : OrganizationUsersUiState()
    data class Success(val organizationUsers: List<DomainOrganizationUser>) : OrganizationUsersUiState()
    data class Error(val message: String) : OrganizationUsersUiState()
}
