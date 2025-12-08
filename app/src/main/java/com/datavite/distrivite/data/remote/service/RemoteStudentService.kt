package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteStudent
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface RemoteStudentService {
    @GET("en/{organization}/api/v2/students2/")
    suspend fun getRemoteStudents(
        @Path("organization") organization: String,
     ): List<RemoteStudent>

    @POST("en/{organization}/api/v2/students/create")
    suspend fun createRemoteStudent(
        @Path("organization") organization: String,
        @Body remoteStudent: RemoteStudent
    ): RemoteStudent

    @PUT("en/{organization}/api/v2/students/{id}/edit")
    suspend fun updateRemoteStudent(
            @Path("organization") organization: String,
            @Path("id") id: String,
            @Body remoteStudent: RemoteStudent
    ): RemoteStudent

    @DELETE("en/{organization}/api/v2/students/{id}/delete")
    suspend fun deleteRemoteStudent(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteStudent: RemoteStudent
    ): RemoteStudent
}
