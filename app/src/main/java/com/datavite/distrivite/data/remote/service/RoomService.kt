package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteRoom
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface RoomService {
    @GET("en/{organization}/api/v2/rooms/")
    suspend fun getRooms(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
     ): List<RemoteRoom>

    @POST("en/{organization}/api/v2/rooms/")
    suspend fun createRoom(
        @Path("organization") organization: String,
        @Body remoteRoom: RemoteRoom
    ): RemoteRoom

    @POST("en/{organization}/api/v2/rooms/{id}/edit")
    suspend fun updateRoom(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body period: RemoteRoom
    ): RemoteRoom

    @DELETE("en/{organization}/api/v2/rooms/{id}")
    suspend fun deleteRoom(
        @Path("organization") organization: String,
        @Path("id") id: String
    )
}
