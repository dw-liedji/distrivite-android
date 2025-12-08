package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.dao.HolidayDao
import com.datavite.distrivite.data.local.model.LocalHoliday
import kotlinx.coroutines.flow.Flow

class HolidayLocalDataSourceImpl(
    private val holidayDao: HolidayDao
) : HolidayLocalDataSource {

    override fun getHolidaysFlow(): Flow<List<LocalHoliday>> {
        return holidayDao.getHolidaysFlow()
    }

    override suspend fun saveHoliday(holiday: LocalHoliday) {
        holidayDao.saveHoliday(holiday)
    }

    override suspend fun deleteHoliday(id: String) {
        holidayDao.deleteHoliday(id)
    }

    override suspend fun saveHolidays(holidays: List<LocalHoliday>) {
        holidayDao.insertOrUpdateHolidays(holidays)
    }

    override suspend fun updateHoliday(holiday: LocalHoliday) {
        holidayDao.updateHoliday(holiday)
    }

    override suspend fun getHolidayById(id: String): LocalHoliday? {
        return holidayDao.getHolidayById(id)
    }

    override suspend fun getHolidayForDate(date: String): LocalHoliday? {
        return holidayDao.getHolidayForDate(date)
    }

    override suspend fun getUnSyncedHolidays(syncType: SyncType): List<LocalHoliday> {
        return holidayDao.getUnSyncedHolidays(syncType)
    }

    override suspend fun getHolidaysBySyncType(syncType: SyncType): List<LocalHoliday> {
        return holidayDao.getHolidaysBySyncType(syncType)
    }

    override suspend fun markHolidayAsSynced(holiday: LocalHoliday) {
        holidayDao.markHolidayAsSynced(holiday)
    }

    override suspend fun markAsPendingDeletion(id: String, syncType: SyncType) {
        holidayDao.markAsPendingDeletion(id, syncType)
    }

    override suspend fun updateHolidaySyncType(id: String, syncType: SyncType) {
        holidayDao.updateHolidaySyncType(id, syncType)
    }
}
