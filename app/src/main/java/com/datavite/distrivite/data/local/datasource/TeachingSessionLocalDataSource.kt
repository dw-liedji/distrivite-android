package com.datavite.distrivite.data.local.datasource

import FilterOption
import com.datavite.distrivite.data.local.model.LocalTeachingSession
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface TeachingSessionLocalDataSource {

    suspend fun updateSyncStatus(id: String, syncStatus: SyncStatus)
    suspend fun getSearchLocalTeachingSessionsFor(searchQuery:String, filterOption: FilterOption): List<LocalTeachingSession>
    suspend fun getLocalTeachingSessionsForFilterOption(filterOption: FilterOption): List<LocalTeachingSession>
    suspend fun getLocalTeachingSessionCount(): Int
    suspend fun clear()
    suspend fun getLocalTeachingSessionsFlow(): Flow<List<LocalTeachingSession>>
    suspend fun saveLocalTeachingSessions(localSessions: List<LocalTeachingSession>)
    suspend fun saveLocalTeachingSession(localTeachingSession: LocalTeachingSession)
    suspend fun deleteLocalTeachingSession(localTeachingSessionId: String)
    suspend fun getLocalTeachingSessionById(sessionId:String): LocalTeachingSession?
    suspend fun insertTeachingSession(localTeachingSession: LocalTeachingSession)

}