package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.dao.StudentAttendanceDao
import com.datavite.distrivite.data.local.model.LocalStudentAttendance
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StudentAttendanceLocalDataSourceImpl @Inject constructor(
    private val dao: StudentAttendanceDao
) : StudentAttendanceLocalDataSource {
    override suspend fun getAttendanceCount(): Int {
        return dao.getAttendanceCount()
    }

    override suspend fun clear() {
        dao.clear()
    }

    override suspend fun updateSyncStatus(
        id: String,
        syncStatus: SyncStatus
    ) {
        dao.updateSyncStatus(id, syncStatus)
    }

    override suspend fun getAttendanceByStudentIdAndSessionId(
        orgStudentId: String,
        sessionId: String
    ): LocalStudentAttendance? {
        return dao.getAttendanceByStudentIdAndSessionId(orgStudentId, sessionId)
    }

    override suspend fun getAttendanceBySessionId(sessionId: String): List<LocalStudentAttendance> {
        return dao.getAttendanceBySessionId(sessionId)
    }

    override suspend fun getSearchAttendancesFor(searchQuery: String): List<LocalStudentAttendance> {
        return if (searchQuery.isEmpty()) dao.getAllAttendances()
        else dao.getSearchAttendancesFor(searchQuery)
    }

    override suspend fun getAttendancesAsFlow(): Flow<List<LocalStudentAttendance>> {
        return dao.getAttendancesAsFlow()
    }

    override suspend fun saveAttendances(localAttendances: List<LocalStudentAttendance>) {
        dao.insertOrUpdateAttendances(localAttendances)
    }

    override suspend fun saveAttendance(attendance: LocalStudentAttendance) {
        dao.saveAttendance(attendance)
    }

    override suspend fun deleteAttendance(attendanceId: String) {
        dao.deleteAttendance(attendanceId)
    }

}