package com.datavite.distrivite.data.local.datasource

import FilterOption
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.datavite.distrivite.data.local.dao.LocalTeachingSessionDao
import com.datavite.distrivite.data.local.model.LocalTeachingSession
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TeachingSessionLocalDataSourceImpl @Inject constructor (
    private val localTeachingSessionDao: LocalTeachingSessionDao,
) : TeachingSessionLocalDataSource {




    override suspend fun getSearchLocalTeachingSessionsFor(searchQuery:String, filterOption: FilterOption): List<LocalTeachingSession> {
        if (searchQuery.isEmpty()) return localTeachingSessionDao.getLocalTeachingSessionsForToday()
        return when(filterOption){
            FilterOption.TODAY -> localTeachingSessionDao.getLocalTeachingSessionsForToday(searchQuery)
            FilterOption.THIS_WEEK -> localTeachingSessionDao.getLocalTeachingSessionsForThisWeek(searchQuery)
            FilterOption.NEXT_WEEK -> localTeachingSessionDao.getLocalTeachingSessionsForNextWeek(searchQuery)
            FilterOption.LAST_WEEK -> localTeachingSessionDao.getLocalTeachingSessionsForLastWeek(searchQuery)
            FilterOption.VALIDATED -> localTeachingSessionDao.getLocalTeachingSessionsWithNonNullTimes(searchQuery)
            FilterOption.NON_VALIDATED -> localTeachingSessionDao.getLocalTeachingSessionsWithNullTimes(searchQuery)
        }
    }
    override suspend fun getLocalTeachingSessionsForFilterOption(filterOption: FilterOption): List<LocalTeachingSession> {
        Log.i("distrivite-filter-option", "current option $filterOption")
        return when(filterOption){
            FilterOption.TODAY -> localTeachingSessionDao.getLocalTeachingSessionsForToday()
            FilterOption.THIS_WEEK -> localTeachingSessionDao.getLocalTeachingSessionsForThisWeek()
            FilterOption.NEXT_WEEK -> localTeachingSessionDao.getLocalTeachingSessionsForNextWeek()
            FilterOption.LAST_WEEK -> localTeachingSessionDao.getLocalTeachingSessionsForLastWeek()
            FilterOption.VALIDATED -> localTeachingSessionDao.getLocalTeachingSessionsWithNonNullTimes()
            FilterOption.NON_VALIDATED -> localTeachingSessionDao.getLocalTeachingSessionsWithNullTimes()
        }
    }

    override suspend fun getLocalTeachingSessionCount(): Int {
        return localTeachingSessionDao.getLocalTeachingSessionCount()
    }

    override suspend fun updateSyncStatus(
        id: String,
        syncStatus: SyncStatus
    ) {
        localTeachingSessionDao.updateSyncStatus(id,syncStatus)
    }

    override suspend fun clear() {
        localTeachingSessionDao.clear()
    }

    override suspend fun getLocalTeachingSessionsFlow(): Flow<List<LocalTeachingSession>> {
        return localTeachingSessionDao.getLocalTeachingSessionsFlow()
    }


    override suspend fun saveLocalTeachingSessions(localSessions: List<LocalTeachingSession>) {
        localTeachingSessionDao.insertOrUpdateLocalTeachingSessions(localSessions)
    }

    override suspend fun saveLocalTeachingSession(localTeachingSession: LocalTeachingSession) {
        localTeachingSessionDao.saveLocalTeachingSession(localTeachingSession)
    }

    override suspend fun deleteLocalTeachingSession(localTeachingSessionId: String) {
        localTeachingSessionDao.deleteLocalTeachingSession(localTeachingSessionId)
    }

    override suspend fun getLocalTeachingSessionById(sessionId: String): LocalTeachingSession? {
        return localTeachingSessionDao.getLocalTeachingSessionById(sessionId)
    }

    override suspend fun insertTeachingSession(localTeachingSession: LocalTeachingSession) {
        try {
            localTeachingSessionDao.insertTeachingSession(localTeachingSession)
        }catch (e: SQLiteConstraintException) {
            throw e
        }
    }


}