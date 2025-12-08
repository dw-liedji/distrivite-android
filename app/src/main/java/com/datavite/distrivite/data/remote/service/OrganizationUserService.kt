package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteOrganizationUser
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface OrganizationUserService {
    @GET("en/{organization}/api/v2/users/")
    suspend fun getOrganizationUsers(
        @Path("organization") organization: String,
     ): List<RemoteOrganizationUser>

    @POST("en/{organization}/api/v2/users/create")
    suspend fun createUser(
        @Path("organization") organization: String,
        @Body remoteOrganizationUser: RemoteOrganizationUser
    ): RemoteOrganizationUser

    @PUT("en/{organization}/api/v2/users/{id}/edit")
    suspend fun updateOrganizationUser(
            @Path("organization") organization: String,
            @Path("id") id: String,
            @Body remoteOrganizationUser: RemoteOrganizationUser
    ): RemoteOrganizationUser


    @GET("en/users/{id}/organizations/{org_credential}")
    suspend fun getOrganizationUser(
        @Path("org_credential") orgCredential: String,
        @Path("id") id: String,
    ): RemoteOrganizationUser


    @DELETE("en/{organization}/api/v2/users/{id}/delete")
    suspend fun deleteTeachingSession(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteOrganizationUser: RemoteOrganizationUser
    ): RemoteOrganizationUser
}
