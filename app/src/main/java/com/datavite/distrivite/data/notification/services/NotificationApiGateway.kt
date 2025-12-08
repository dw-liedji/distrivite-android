package com.datavite.distrivite.data.notification.services

import com.datavite.distrivite.data.remote.model.RemoteTeachingSession
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApiGateway {

    @POST("en/{organization}/api/v2/teaching-sessions/{id}/notify-parents")
    suspend fun notifyParentsWithTheCloudServer(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteTeachingSession: RemoteTeachingSession
    ): RemoteTeachingSession

}
