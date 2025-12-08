package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalHoliday
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {

    @Query("SELECT * FROM holidays ORDER BY created DESC")
    fun getHolidaysFlow(): Flow<List<LocalHoliday>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveHoliday(holiday: LocalHoliday)

    @Query("DELETE FROM holidays WHERE id = :id")
    suspend fun deleteHoliday(id: String)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHolidays(holidays: List<LocalHoliday>)

    @Update
    suspend fun updateHoliday(holiday: LocalHoliday)

    @Query("SELECT * FROM holidays WHERE id = :id LIMIT 1")
    suspend fun getHolidayById(id: String): LocalHoliday?
    @Query("SELECT * FROM holidays WHERE date = :date LIMIT 1")
    suspend fun getHolidayForDate(date: String): LocalHoliday?

    @Query("SELECT * FROM holidays WHERE syncType != :syncType ORDER BY created DESC")
    suspend fun getUnSyncedHolidays(syncType: SyncType=SyncType.SYNCED): List<LocalHoliday>

    @Query("SELECT * FROM holidays WHERE syncType = :syncType")
    suspend fun getHolidaysBySyncType(syncType: SyncType): List<LocalHoliday>

    @Update
    suspend fun markHolidayAsSynced(holiday: LocalHoliday)

    @Query("UPDATE holidays SET syncType = :syncType WHERE id = :id")
    suspend fun markAsPendingDeletion(id: String, syncType: SyncType = SyncType.PENDING_DELETION)

    @Query("UPDATE holidays SET syncType = :syncType WHERE id = :id")
    suspend fun updateHolidaySyncType(id: String, syncType: SyncType)
}