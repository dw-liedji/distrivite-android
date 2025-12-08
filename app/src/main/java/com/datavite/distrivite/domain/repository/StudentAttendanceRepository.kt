package com.datavite.distrivite.domain.repository

import com.datavite.distrivite.domain.model.DomainStudentAttendance
import kotlinx.coroutines.flow.Flow

interface StudentAttendanceRepository {

    // ----------- Local Only -----------
    suspend fun createAttendance(attendance: DomainStudentAttendance)
   suspend fun getAttendancesAsFlow(): Flow<List<DomainStudentAttendance>>
    suspend fun updateAttendance(attendance: DomainStudentAttendance)
    suspend fun deleteAttendance(attendance: DomainStudentAttendance)
    suspend fun getAttendanceByStudentIdAndSessionId(studentId: String, sessionId: String): DomainStudentAttendance?

    suspend fun getAttendancesFor(searchQuery: String): List<DomainStudentAttendance>
    suspend fun getAttendanceBySessionId(sessionId: String): List<DomainStudentAttendance>
    suspend fun fetchIfEmpty(organization: String)
}
