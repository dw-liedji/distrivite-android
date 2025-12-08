package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalHoliday
import kotlinx.coroutines.flow.Flow

interface HolidayLocalDataSource {

    fun getHolidaysFlow(): Flow<List<LocalHoliday>>

    suspend fun saveHoliday(holiday: LocalHoliday)

    suspend fun deleteHoliday(id: String)

    suspend fun saveHolidays(holidays: List<LocalHoliday>)

    suspend fun updateHoliday(holiday: LocalHoliday)

    suspend fun getHolidayById(id: String): LocalHoliday?

    suspend fun getHolidayForDate(date: String): LocalHoliday?

    suspend fun getUnSyncedHolidays(syncType: SyncType = SyncType.SYNCED): List<LocalHoliday>

    suspend fun getHolidaysBySyncType(syncType: SyncType): List<LocalHoliday>

    suspend fun markHolidayAsSynced(holiday: LocalHoliday)

    suspend fun markAsPendingDeletion(id: String, syncType: SyncType = SyncType.PENDING_DELETION)

    suspend fun updateHolidaySyncType(id: String, syncType: SyncType)

}
