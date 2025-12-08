package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.dao.RoomDao
import com.datavite.distrivite.data.local.model.LocalRoom
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomLocalDataSourceImpl @Inject constructor(
    private val roomDao: RoomDao
) : RoomLocalDataSource {

    override suspend fun getRoomsFlow(): Flow<List<LocalRoom>> {
        return roomDao.getAllRoomFlows()
    }

    override suspend fun getAllRooms(): List<LocalRoom> {
        return roomDao.getAllRooms()
    }

    override suspend fun saveRooms(localPeriods: List<LocalRoom>) {
        for (localPeriod in localPeriods) {
            roomDao.saveRoom(localPeriod)
        }
    }

    override suspend fun getUnSyncedPeriods(): List<LocalRoom> {
        return roomDao.getUnSyncedPeriods()
    }

    override suspend fun markAsSynced(room: LocalRoom) {
        roomDao.markPeriodAsSynced(room)
    }

    override suspend fun saveRoom(room: LocalRoom) {
        roomDao.saveRoom(room)
    }

    override suspend fun deleteRoom(roomId: String) {
        roomDao.deleteRoom(roomId)
    }

    override suspend fun markAsPendingDeletion(roomId: String) {
        roomDao.markAsPendingDeletion(roomId)
    }

}