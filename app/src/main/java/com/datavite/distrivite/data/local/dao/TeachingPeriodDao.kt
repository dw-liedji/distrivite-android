package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.model.LocalTeachingPeriod
import com.datavite.distrivite.data.local.SyncType
import kotlinx.coroutines.flow.Flow

@Dao
interface TeachingPeriodDao {
    @Query("SELECT * FROM teaching_periods")
    fun getAllTeachingPeriods(): List<LocalTeachingPeriod>

    @Query("SELECT * FROM teaching_periods")
    fun getAllTeachingPeriodFlows(): Flow<List<LocalTeachingPeriod>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTeachingPeriod(period: LocalTeachingPeriod)

    @Query("DELETE FROM teaching_periods WHERE id = :periodId")
    suspend fun deleteTeachingPeriod(periodId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTeachingPeriods(periods: List<LocalTeachingPeriod>)

    @Update
    suspend fun updateTeachingPeriod(period: LocalTeachingPeriod)

    @Query("SELECT * FROM teaching_periods WHERE syncType != :syncType")
    suspend fun getUnSyncedPeriods(syncType: SyncType=SyncType.SYNCED): List<LocalTeachingPeriod>

    @Query("SELECT * FROM teaching_periods WHERE syncType = :syncType")
    suspend fun getTeachingPeriodsBySyncType(syncType: SyncType): List<LocalTeachingPeriod>

    @Update
    suspend fun markPeriodAsSynced(period: LocalTeachingPeriod)

    @Query("UPDATE teaching_periods SET syncType = :syncType WHERE id = :periodId")
    suspend fun markAsPendingDeletion(periodId: String, syncType: SyncType = SyncType.PENDING_DELETION)

    @Query("UPDATE teaching_periods SET syncType = :syncType WHERE id = :periodId")
    suspend fun updatePeriodSyncType(periodId: String, syncType: SyncType)
}
