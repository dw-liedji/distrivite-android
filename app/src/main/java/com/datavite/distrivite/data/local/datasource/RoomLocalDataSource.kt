package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.model.LocalRoom
import kotlinx.coroutines.flow.Flow

interface RoomLocalDataSource {
    suspend fun getRoomsFlow(): Flow<List<LocalRoom>>
    suspend fun getAllRooms(): List<LocalRoom>
    suspend fun saveRooms(localPeriods: List<LocalRoom>)
    suspend fun getUnSyncedPeriods(): List<LocalRoom>
    suspend fun markAsSynced(room: LocalRoom)
    suspend fun saveRoom(room: LocalRoom)
    suspend fun deleteRoom(roomId: String)
    suspend fun markAsPendingDeletion(roomId: String)
}