package com.datavite.distrivite.domain.repository

import com.datavite.distrivite.data.local.model.LocalOrganizationUser
import com.datavite.distrivite.domain.model.DomainOrganizationUser
import kotlinx.coroutines.flow.Flow

interface OrganizationUserRepository {
    suspend fun getUserById(id: String): LocalOrganizationUser?
    suspend fun getOrganizationUsersFlow(): Flow<List<DomainOrganizationUser>>
    suspend fun getOrgUserById(userId:String): DomainOrganizationUser?
    suspend fun createOrganizationUser(organization: String, user: DomainOrganizationUser)
    suspend fun updateOrganizationUser(organization: String, user: DomainOrganizationUser)
    suspend fun deleteOrganizationUser(organization: String, user: DomainOrganizationUser)
    suspend fun syncOrganizationUsers(organization: String)
}