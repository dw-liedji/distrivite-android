package com.datavite.distrivite.data.repository

import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.datasource.StudentAttendanceLocalDataSource
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.mapper.StudentAttendanceMapper
import com.datavite.distrivite.data.remote.datasource.StudentAttendanceRemoteDataSource
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationScope
import com.datavite.distrivite.data.sync.OperationType
import com.datavite.distrivite.domain.model.DomainStudentAttendance
import com.datavite.distrivite.domain.repository.StudentAttendanceRepository
import com.datavite.distrivite.utils.JsonConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StudentAttendanceRepositoryImpl @Inject constructor (
    private val localDataSource: StudentAttendanceLocalDataSource,
    private val remoteDataSource: StudentAttendanceRemoteDataSource,
    private val studentAttendanceMapper: StudentAttendanceMapper,
    private val pendingOperationDao: PendingOperationDao,
    ) : StudentAttendanceRepository {

    // ✅ Local create (no sync)
    override suspend fun createAttendance(attendance: DomainStudentAttendance) {
        val pendingDomainStudentAttendance = attendance.copy(syncStatus = SyncStatus.PENDING)
        val local = studentAttendanceMapper.mapDomainToLocal(pendingDomainStudentAttendance)
        val remote = studentAttendanceMapper.mapDomainToRemote(pendingDomainStudentAttendance)

        val operation = PendingOperation(
            orgSlug = attendance.orgSlug,
            orgId = attendance.orgId,
            entityId = attendance.id,
            entityType = EntityType.Attendance,
            operationType = OperationType.CREATE,
            operationScope = OperationScope.STATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        localDataSource.saveAttendance(local)
        pendingOperationDao.upsertPendingOperation(operation)
    }

    // ✅ Local update (no sync)
    override suspend fun updateAttendance(attendance: DomainStudentAttendance) {
        val pendingDomainStudentAttendance = attendance.copy(syncStatus = SyncStatus.PENDING)
        val local = studentAttendanceMapper.mapDomainToLocal(pendingDomainStudentAttendance)
        val remote = studentAttendanceMapper.mapDomainToRemote(pendingDomainStudentAttendance)

        val operation = PendingOperation(
            orgSlug = attendance.orgSlug,
            orgId = attendance.orgId,
            entityId = attendance.id,
            entityType = EntityType.Attendance,
            operationType = OperationType.UPDATE,
            operationScope = OperationScope.STATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        localDataSource.saveAttendance(local)
        pendingOperationDao.upsertPendingOperation(operation)
    }

    // ✅ Local delete (no sync)
    override suspend fun deleteAttendance(attendance: DomainStudentAttendance) {
        val pendingDomainStudentAttendance = attendance.copy(syncStatus = SyncStatus.PENDING)
        val local = studentAttendanceMapper.mapDomainToLocal(pendingDomainStudentAttendance)
        val remote = studentAttendanceMapper.mapDomainToRemote(pendingDomainStudentAttendance)

        val operation = PendingOperation(
            orgSlug = attendance.orgSlug,
            orgId = attendance.orgId,
            entityId = attendance.id,
            entityType = EntityType.Attendance,
            operationType = OperationType.DELETE,
            operationScope = OperationScope.STATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        localDataSource.deleteAttendance(attendance.id)
        pendingOperationDao.upsertPendingOperation(operation)
    }

    // ✅ Queries (all local)
    override suspend fun getAttendanceByStudentIdAndSessionId(studentId: String, sessionId:String): DomainStudentAttendance? {
        return  localDataSource.getAttendanceByStudentIdAndSessionId(studentId, sessionId)?.let {
            studentAttendanceMapper.mapLocalToDomain(it)
        }
    }

    override suspend fun getAttendancesAsFlow(): Flow<List<DomainStudentAttendance>> {
        return localDataSource.getAttendancesAsFlow().map { attendances ->
            attendances.map { studentAttendanceMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun getAttendancesFor(searchQuery: String): List<DomainStudentAttendance> {
        return localDataSource.getSearchAttendancesFor(searchQuery).map { studentAttendanceMapper.mapLocalToDomain(it) }
    }

    override suspend fun getAttendanceBySessionId(sessionId: String): List<DomainStudentAttendance> {
        return localDataSource.getAttendanceBySessionId(sessionId).map { studentAttendanceMapper.mapLocalToDomain(it) }
    }

    override suspend fun fetchIfEmpty(organization: String) {
        try {
            if (localDataSource.getAttendanceCount() == 0){
                val remoteSessions = remoteDataSource.getAttendances(organization)
                val domainSessions = remoteSessions.map { studentAttendanceMapper.mapRemoteToDomain(it) }
                val localSessions = domainSessions.map { studentAttendanceMapper.mapDomainToLocal(it) }
                localDataSource.clear()
                localDataSource.saveAttendances(localSessions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TeachingSessionRepository", " error ${e.message}")
        }
    }
}