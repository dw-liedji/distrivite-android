package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteTeachingSession
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RemoteTeachingSessionService {
    @GET("en/{organization}/api/v2/teaching-sessions/")
    suspend fun getRemoteTeachingSessions(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
     ): List<RemoteTeachingSession>


    @POST("en/{organization}/api/v2/teaching-sessions/create")
    suspend fun createRemoteTeachingSession(
        @Path("organization") organization: String,
        @Body remoteRemoteTeachingSession: RemoteTeachingSession
    ): RemoteTeachingSession

    @POST("en/{organization}/api/v2/teaching-sessions/{id}/start")
    suspend fun startRemoteTeachingSession(
            @Path("organization") organization: String,
            @Path("id") id: String,
            @Body remoteTeachingSession: RemoteTeachingSession
    ): RemoteTeachingSession

    @POST("en/{organization}/api/v2/teaching-sessions/{id}/end")
    suspend fun endRemoteTeachingSession(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteTeachingSession: RemoteTeachingSession
    ): RemoteTeachingSession

    @POST("en/{organization}/api/v2/teaching-sessions/{id}/approve")
    suspend fun approveRemoteTeachingSession(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteTeachingSession: RemoteTeachingSession
    ): RemoteTeachingSession

    @POST("en/{organization}/api/v2/teaching-sessions/{id}/edit")
    suspend fun updateRemoteTeachingSession(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body session: RemoteTeachingSession
    ): RemoteTeachingSession

    @PUT("en/{organization}/api/v2/teaching-sessions/{id}/edit/scenario")
    suspend fun updateRemoteTeachingSessionScenario(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteTeachingSession: RemoteTeachingSession
    ): RemoteTeachingSession

    @DELETE("en/{organization}/api/v2/teaching-sessions/{id}")
    suspend fun deleteRemoteTeachingSession(
        @Path("organization") organization: String,
        @Path("id") id: Long
    )
}
