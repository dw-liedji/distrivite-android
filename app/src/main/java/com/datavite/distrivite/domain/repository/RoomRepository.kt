package com.datavite.distrivite.feature.cameis.domain.repository

import com.datavite.distrivite.domain.model.DomainRoom
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    suspend fun getRoomsFlow(): Flow<List<DomainRoom>>
    suspend fun getAllRooms(): List<DomainRoom>
    suspend fun createRoom(organization: String, room: DomainRoom)
    suspend fun updateRoom(organization: String, room: DomainRoom)
    suspend fun deleteRoom(organization: String, roomId: String)
    suspend fun syncRooms(organization: String)
}