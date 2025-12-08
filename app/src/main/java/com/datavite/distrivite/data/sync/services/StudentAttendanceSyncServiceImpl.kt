package com.datavite.distrivite.data.sync.services

import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.datasource.StudentAttendanceLocalDataSource
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.mapper.StudentAttendanceMapper
import com.datavite.distrivite.data.remote.datasource.StudentAttendanceRemoteDataSource
import com.datavite.distrivite.data.remote.model.RemoteStudentAttendance
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationType
import javax.inject.Inject

class StudentAttendanceSyncServiceImpl @Inject constructor (
    private val localDataSource: StudentAttendanceLocalDataSource,
    private val remoteDataSource: StudentAttendanceRemoteDataSource,
    private val studentAttendanceMapper: StudentAttendanceMapper,
    private val pendingOperationDao: PendingOperationDao,
) : StudentAttendanceSyncService {

    override suspend fun push(operations: List<PendingOperation>) {
        for (operation in operations) {
            syncOperation(operation, operations)
        }
    }

    override suspend fun pullAll(organization: String) {
        Log.e("AttendanceSync", "Full sync started")
        try {
            val remoteAttendances = remoteDataSource.getAttendances(organization)

            val domainAttendances = remoteAttendances.map { studentAttendanceMapper.mapRemoteToDomain(it) }
            val localAttendances = domainAttendances.map { studentAttendanceMapper.mapDomainToLocal(it) }
            //localDataSource.clear()
            localDataSource.saveAttendances(localAttendances)
            Log.e("AttendanceSync", "Full sync success ${localAttendances.size} sessions", )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AttendanceSync", "Full sync failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun hasCachedData(): Boolean {
        return localDataSource.getAttendanceCount() != 0
    }

    override fun getEntity(): EntityType {
        return EntityType.Attendance
    }


    private suspend fun pushCreatedAttendanceAndResolveConflicts(remoteAttendance: RemoteStudentAttendance) {
        try {

            remoteDataSource.createAttendance(remoteAttendance.orgSlug, remoteAttendance)
            val updatedAttendance = studentAttendanceMapper.mapRemoteToDomain(remoteAttendance)

            // Must implement conflict handling
            val syncedLocal = studentAttendanceMapper.mapDomainToLocal(updatedAttendance)
            localDataSource.saveAttendance(syncedLocal)
            Log.e("AttendanceSync", "Success to sync created attendance ${remoteAttendance.id}", )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AttendanceSync", "Failed to sync created attendance ${remoteAttendance.id}", e)
            throw e
        }
    }

    private suspend fun pushUpdatedAttendanceAndResolveConflicts(remoteAttendance: RemoteStudentAttendance) {
        try {

            remoteDataSource.updateAttendance(remoteAttendance.orgSlug, remoteAttendance)
            val updatedAttendance = studentAttendanceMapper.mapRemoteToDomain(remoteAttendance)

            // Must implement conflict handling
            val syncedLocal = studentAttendanceMapper.mapDomainToLocal(updatedAttendance)
            localDataSource.saveAttendance(syncedLocal)
            Log.e("AttendanceSync", "Success to sync updated attendance ${remoteAttendance.id}", )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AttendanceSync", "Failed to sync updated attendance ${remoteAttendance.id}", e)
            throw e        }
    }

    private suspend fun pushDeletedAttendanceAndResolveConflicts(remoteAttendance: RemoteStudentAttendance) {
        try {
            remoteDataSource.deleteAttendance(remoteAttendance.orgSlug, remoteAttendance.id)
            Log.e("AttendanceSync", "Success to sync deleted attendance ${remoteAttendance.id}", )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AttendanceSync", "Failed to sync deleted attendance ${remoteAttendance.id}", e)
            throw e
        }
    }

    private suspend fun syncOperation(currentOperation: PendingOperation, allOperations: List<PendingOperation>) {
        val attendance = currentOperation.parsePayload<RemoteStudentAttendance>()

        // Count all pending operations for this entity
        val totalPending = allOperations.count { it.entityId == currentOperation.entityId }

        // Mark entity as syncing before pushing changes
        localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.SYNCING)

        try {
            when (currentOperation.operationType) {
                OperationType.CREATE -> {
                    pushCreatedAttendanceAndResolveConflicts(attendance)
                }
                OperationType.UPDATE -> {
                    pushUpdatedAttendanceAndResolveConflicts(attendance)
                }
                OperationType.DELETE -> {
                    pushDeletedAttendanceAndResolveConflicts(attendance)
                }
                else -> {}
            }

            // Delete the completed operation
            pendingOperationDao.deleteByKeys(entityType = currentOperation.entityType, entityId = currentOperation.entityId, operationType = currentOperation.operationType, orgId = currentOperation.orgId)

            // If other pending operations remain, status is still pending
            if ((totalPending - 1) > 0) {
                localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.PENDING)
            } else {
                localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.SYNCED)
            }
            Log.i("AttendanceSyncOperation", "Success to AttendanceSyncOperation operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}")

        } catch (e: Exception) {
            // Increment failure count
            pendingOperationDao.incrementFailureCount(entityType = currentOperation.entityType, entityId = currentOperation.entityId, operationType = currentOperation.operationType, orgId = currentOperation.orgId)

            // Get updated failure count to decide status
            val updatedFailureCount = pendingOperationDao.getFailureCount(entityType = currentOperation.entityType, entityId = currentOperation.entityId, operationType = currentOperation.operationType, orgId = currentOperation.orgId)

            if (updatedFailureCount > 5) {
                localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.FAILED)
            } else {
                localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.PENDING)
            }
            Log.e("AttendanceSyncOperation", "Failed to AttendanceSyncOperation operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}")

            e.printStackTrace()
        }
    }
}