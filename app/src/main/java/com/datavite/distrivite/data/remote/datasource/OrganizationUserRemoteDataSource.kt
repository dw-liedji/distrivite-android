package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteOrganizationUser

interface OrganizationUserRemoteDataSource {
    suspend fun getOrganizationUsers(organization:String): List<RemoteOrganizationUser>
    suspend fun getOrganizationUser(orgCredential:String, userId:String): RemoteOrganizationUser
    suspend fun createOrganizationUser(organization:String, user: RemoteOrganizationUser): RemoteOrganizationUser
    suspend fun updateOrganizationUser(organization:String, user: RemoteOrganizationUser) : RemoteOrganizationUser
    suspend fun deleteOrganizationUser(organization:String, user: RemoteOrganizationUser) : RemoteOrganizationUser
}