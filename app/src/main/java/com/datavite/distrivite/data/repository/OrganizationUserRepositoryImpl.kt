package com.datavite.distrivite.data.repository

import com.datavite.distrivite.data.mapper.OrganizationUserMapper
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.datasource.OrganizationUserLocalDataSource
import com.datavite.distrivite.data.local.model.LocalOrganizationUser
import com.datavite.distrivite.data.remote.datasource.OrganizationUserRemoteDataSource
import com.datavite.distrivite.domain.model.DomainOrganizationUser
import com.datavite.distrivite.domain.repository.OrganizationUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OrganizationUserRepositoryImpl @Inject constructor(
    private val localDataSource: OrganizationUserLocalDataSource,
    private val remoteDataSource: OrganizationUserRemoteDataSource,
    private val organizationUserMapper: OrganizationUserMapper
) : OrganizationUserRepository {

    override suspend fun getUserById(id: String): LocalOrganizationUser? {
        return localDataSource.getUserById(id)
    }

    override suspend fun getOrganizationUsersFlow(): Flow<List<DomainOrganizationUser>> {
        return localDataSource.getOrganizationUsersFlow().map { users ->
            users.map { organizationUserMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun getOrgUserById(userId: String): DomainOrganizationUser? {
        val orgUser = localDataSource.getOrgUserByUserId(userId)
        orgUser?.let {
            return organizationUserMapper.mapLocalToDomain(it)
        }
        return null
    }

    override suspend fun createOrganizationUser(organization: String, user: DomainOrganizationUser) {
        // Save the user locally first with pending creation status
        val localUser = organizationUserMapper.mapDomainToLocal(user, SyncType.PENDING_CREATION)
        localDataSource.saveOrganizationUser(localUser)

        // Attempt to sync the created user remotely in the background
        try {
            val remoteUser = organizationUserMapper.mapDomainToRemote(user)
            val createdRemoteUser = remoteDataSource.createOrganizationUser(organization, remoteUser)
            val syncedLocalUser = organizationUserMapper.mapRemoteToDomain(createdRemoteUser)
            localDataSource.saveOrganizationUser(organizationUserMapper.mapDomainToLocal(syncedLocalUser, SyncType.SYNCED))
        } catch (e: Exception) {
            // If sync fails, leave the user as pending creation
        }
    }

    override suspend fun updateOrganizationUser(organization: String, user: DomainOrganizationUser) {
        // Save the user locally first with pending modification status
        val localUser = organizationUserMapper.mapDomainToLocal(user, SyncType.PENDING_MODIFICATION)
        localDataSource.saveOrganizationUser(localUser)

        // Attempt to sync the updated user remotely in the background
        try {
            val remoteUser = organizationUserMapper.mapDomainToRemote(user)
            val updatedRemoteUser = remoteDataSource.updateOrganizationUser(organization, remoteUser)
            val syncedLocalUser = organizationUserMapper.mapRemoteToDomain(updatedRemoteUser)
            localDataSource.saveOrganizationUser(organizationUserMapper.mapDomainToLocal(syncedLocalUser, SyncType.SYNCED))
        } catch (e: Exception) {
            // If sync fails, leave the user as pending modification
        }
    }

    override suspend fun deleteOrganizationUser(organization: String, user: DomainOrganizationUser) {
        // Mark the user as pending deletion locally
        localDataSource.markAsPendingDeletion(user.id)

        // Attempt to delete the user remotely in the background
        try {
            val remoteUser = organizationUserMapper.mapDomainToRemote(user)
            remoteDataSource.deleteOrganizationUser(organization, remoteUser)
            localDataSource.deleteOrganizationUser(user.id)
        } catch (e: Exception) {
            // If sync fails, leave the user marked as pending deletion
        }
    }

    override suspend fun syncOrganizationUsers(organization: String) {
        // Fetch unSynced users from the local database
        val unSyncedUsers = localDataSource.getUnSyncedUsers()

        // Attempt to sync them with the remote server
        for (user in unSyncedUsers) {
            try {
                when (user.syncType) {
                    SyncType.PENDING_CREATION -> {
                        val domainUser = organizationUserMapper.mapLocalToDomain(user)
                        createOrganizationUser(organization, domainUser)
                    }
                    SyncType.PENDING_MODIFICATION -> {
                        val domainUser = organizationUserMapper.mapLocalToDomain(user)
                        updateOrganizationUser(organization, domainUser)
                    }
                    SyncType.PENDING_DELETION -> {
                        val domainUser = organizationUserMapper.mapLocalToDomain(user)
                        deleteOrganizationUser(organization, domainUser)
                    }
                    else -> { /* No action needed */ }
                }
            } catch (e: Exception) {
                // Handle sync failure (e.g., log, retry later)
            }
        }

        // Fetch and update the latest remote users
        fetchLatestRemoteOrganizationUsersAndUpdateLocalOrganizationUsers(organization)
    }

    private suspend fun fetchLatestRemoteOrganizationUsersAndUpdateLocalOrganizationUsers(organization: String) {
        try {
            val remoteUsers = remoteDataSource.getOrganizationUsers(organization)
            val domainUsers = remoteUsers.map { organizationUserMapper.mapRemoteToDomain(it) }
            val localUsers = domainUsers.map { organizationUserMapper.mapDomainToLocal(it, SyncType.SYNCED) }
            localDataSource.saveOrganizationUsers(localUsers)
        } catch (e: Exception) {
            // Handle failure to fetch from remote (fallback to local data if necessary)
        }
    }
}
