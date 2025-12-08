package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalRoom
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

    @Query("SELECT * FROM rooms")
    fun getAllRooms(): List<LocalRoom>

    @Query("SELECT * FROM rooms")
    fun getAllRoomFlows(): Flow<List<LocalRoom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRoom(room: LocalRoom)

    @Query("DELETE FROM rooms WHERE id = :roomId")
    suspend fun deleteRoom(roomId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRooms(rooms: List<LocalRoom>)

    @Update
    suspend fun updateRoom(room: LocalRoom)

    @Query("SELECT * FROM rooms WHERE syncType != :syncType")
    suspend fun getUnSyncedPeriods(syncType: SyncType =SyncType.SYNCED): List<LocalRoom>

    @Query("SELECT * FROM rooms WHERE syncType = :syncType")
    suspend fun getRoomsBySyncType(syncType: SyncType): List<LocalRoom>

    @Update
    suspend fun markPeriodAsSynced(room: LocalRoom)

    @Query("UPDATE rooms SET syncType = :syncType WHERE id = :roomId")
    suspend fun markAsPendingDeletion(roomId: String, syncType: SyncType = SyncType.PENDING_DELETION)

    @Query("UPDATE rooms SET syncType = :syncType WHERE id = :roomId")
    suspend fun updatePeriodSyncType(roomId: String, syncType: SyncType)
}
