package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.model.LocalOrganizationUser
import kotlinx.coroutines.flow.Flow

interface OrganizationUserLocalDataSource {
    suspend fun getUserById(id: String): LocalOrganizationUser?
    suspend fun getOrgUserByUserId(userId: String): LocalOrganizationUser?
    suspend fun getOrganizationUsersFlow(): Flow<List<LocalOrganizationUser>>
    suspend fun getUnSyncedUsers(): List<LocalOrganizationUser>
    suspend fun saveOrganizationUsers(localUsers: List<LocalOrganizationUser>)
    suspend fun markAsSynced(user: LocalOrganizationUser)
    suspend fun saveOrganizationUser(user: LocalOrganizationUser)
    suspend fun deleteOrganizationUser(id: String)
    suspend fun markAsPendingDeletion(id: String)
}