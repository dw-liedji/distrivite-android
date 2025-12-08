package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.model.LocalStudentAttendance
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface StudentAttendanceLocalDataSource {

    suspend fun getAttendanceCount(): Int
    suspend fun clear()
    suspend fun updateSyncStatus(id: String,  syncStatus: SyncStatus)
    suspend fun getAttendanceByStudentIdAndSessionId(orgStudentId: String, sessionId:String): LocalStudentAttendance?
    suspend fun getAttendanceBySessionId(sessionId:String): List<LocalStudentAttendance>
    suspend fun getSearchAttendancesFor(searchQuery: String): List<LocalStudentAttendance>
    suspend fun getAttendancesAsFlow(): Flow<List<LocalStudentAttendance>>
    suspend fun saveAttendances(localAttendances: List<LocalStudentAttendance>)
    suspend fun saveAttendance(attendance: LocalStudentAttendance)
    suspend fun deleteAttendance(attendanceId: String)
}
