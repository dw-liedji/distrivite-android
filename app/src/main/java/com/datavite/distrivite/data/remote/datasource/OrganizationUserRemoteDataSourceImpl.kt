package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteOrganizationUser
import com.datavite.distrivite.data.remote.service.OrganizationUserService
import javax.inject.Inject

class OrganizationUserRemoteDataSourceImpl @Inject constructor(
    private val organizationUserService: OrganizationUserService
) : OrganizationUserRemoteDataSource {
    override suspend fun getOrganizationUsers(organization:String): List<RemoteOrganizationUser> {
        return organizationUserService.getOrganizationUsers(organization)
    }

    override suspend fun getOrganizationUser(
        orgCredential: String,
        userId: String
    ): RemoteOrganizationUser {
        return organizationUserService.getOrganizationUser(orgCredential, userId)
    }

    override suspend fun createOrganizationUser(organization:String, user: RemoteOrganizationUser) : RemoteOrganizationUser {
        TODO("Not yet implemented")
    }

    override suspend fun updateOrganizationUser(organization:String, user: RemoteOrganizationUser) : RemoteOrganizationUser {
        return organizationUserService.updateOrganizationUser(organization=organization, user.id, user)
    }

    override suspend fun deleteOrganizationUser(organization:String, user: RemoteOrganizationUser) : RemoteOrganizationUser{
        return organizationUserService.deleteTeachingSession(organization, user.id, user)
    }
}