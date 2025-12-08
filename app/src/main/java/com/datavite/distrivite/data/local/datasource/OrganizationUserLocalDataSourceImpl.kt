package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.dao.OrganizationUserDao
import com.datavite.distrivite.data.local.model.LocalOrganizationUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OrganizationUserLocalDataSourceImpl @Inject constructor (
    private val organizationUserDao: OrganizationUserDao,
) : OrganizationUserLocalDataSource{
    override suspend fun getUserById(id: String): LocalOrganizationUser? {
        return organizationUserDao.getUserById(id)
    }

    override suspend fun getOrgUserByUserId(userId: String): LocalOrganizationUser? {
        return organizationUserDao.getOrgUserByUserId(userId)
    }

    override suspend fun getOrganizationUsersFlow(): Flow<List<LocalOrganizationUser>> {
        return organizationUserDao.getOrganizationUsersFlow()
    }
    override suspend fun getUnSyncedUsers(): List<LocalOrganizationUser> {
        return organizationUserDao.getUnSyncedUsers()
    }

    override suspend fun saveOrganizationUsers(localUsers: List<LocalOrganizationUser>) {
        organizationUserDao.insertOrUpdateOrganizationUsers(localUsers)
    }

    override suspend fun markAsSynced(user: LocalOrganizationUser) {
        organizationUserDao.markUserAsSynced(user)
    }

    override suspend fun saveOrganizationUser(user: LocalOrganizationUser) {
        organizationUserDao.saveOrganizationUser(user)
    }

    override suspend fun deleteOrganizationUser(id: String) {
        organizationUserDao.deleteOrganizationUser(id)
    }

    override suspend fun markAsPendingDeletion(id: String) {
        organizationUserDao.markAsPendingDeletion(id = id)
    }
}