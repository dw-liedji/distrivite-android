package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteRoom
import com.datavite.distrivite.data.remote.service.RoomService
import javax.inject.Inject

class RoomRemoteDataSourceImpl @Inject constructor(
    private val roomService: RoomService
) : RoomRemoteDataSource {

    override suspend fun getRooms(organization: String): List<RemoteRoom> {
        return roomService.getRooms(organization)
    }

    override suspend fun createRoom(organization: String, room: RemoteRoom) {
        roomService.createRoom(organization,room)
    }

    override suspend fun updateRoom(organization: String, room: RemoteRoom) {
        roomService.updateRoom(organization,room.id, room)
    }

    override suspend fun deleteRoom(organization: String, roomId: String) {
        roomService.deleteRoom(organization, roomId)
    }
}