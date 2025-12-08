package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteRoom


interface RoomRemoteDataSource {
    suspend fun getRooms(organization:String): List<RemoteRoom>
    suspend fun createRoom(organization:String, room: RemoteRoom)
    suspend fun updateRoom(organization:String, room: RemoteRoom)
    suspend fun deleteRoom(organization:String, roomId: String)
}