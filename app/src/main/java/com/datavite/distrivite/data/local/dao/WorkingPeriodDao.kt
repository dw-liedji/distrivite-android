package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalWorkingPeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkingPeriodDao {

    @Query("""
        SELECT * FROM working_periods 
        WHERE isActive LIKE '%' || :searchQuery || '%' 
    """)
    fun getSearchWorkingPeriodsFor(searchQuery: String): List<LocalWorkingPeriod>

    @Query("SELECT * FROM working_periods")
    fun getWorkingPeriodsFlow(): Flow<List<LocalWorkingPeriod>>

    @Query("SELECT * FROM working_periods")
    fun getAllWorkingPeriods(): List<LocalWorkingPeriod>

    @Query("SELECT * FROM working_periods WHERE id IN (:ids)")
    suspend fun getWorkingPeriodsByIds(ids: List<String>): List<LocalWorkingPeriod>

    @Query("SELECT * FROM working_periods WHERE id IN (:ids) AND dayId = :dayId")
    suspend fun getWorkingPeriodsByIdsForDay(ids: List<String>, dayId:Int): List<LocalWorkingPeriod>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWorkingPeriod(workingPeriod: LocalWorkingPeriod)

    @Query("DELETE FROM working_periods WHERE id = :workingPeriodId")
    suspend fun deleteWorkingPeriod(workingPeriodId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWorkingPeriods(workingPeriods: List<LocalWorkingPeriod>)

    @Update
    suspend fun updateWorkingPeriod(workingPeriod: LocalWorkingPeriod)

    @Query("SELECT * FROM working_periods WHERE syncType != :syncType")
    suspend fun getUnSyncedWorkingPeriods(syncType: SyncType=SyncType.SYNCED): List<LocalWorkingPeriod>

    @Query("SELECT * FROM working_periods WHERE syncType = :syncType")
    suspend fun getWorkingPeriodsBySyncType(syncType: SyncType): List<LocalWorkingPeriod>

    @Update
    suspend fun markWorkingPeriodAsSynced(workingPeriod: LocalWorkingPeriod)

    @Query("UPDATE working_periods SET syncType = :syncType WHERE id = :workingPeriodId")
    suspend fun markAsPendingDeletion(workingPeriodId: String, syncType: SyncType = SyncType.PENDING_DELETION)

    @Query("UPDATE working_periods SET syncType = :syncType WHERE id = :workingPeriodId")
    suspend fun updateWorkingPeriodSyncType(workingPeriodId: String, syncType: SyncType)
}
