package com.datavite.distrivite.data.repository

import com.datavite.distrivite.data.mapper.StudentMapper
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.datasource.StudentLocalDataSource
import com.datavite.distrivite.data.remote.datasource.StudentRemoteDataSource
import com.datavite.distrivite.domain.model.DomainStudent
import com.datavite.distrivite.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StudentRepositoryImpl @Inject constructor(
    private val localDataSource: StudentLocalDataSource,
    private val remoteDataSource: StudentRemoteDataSource,
    private val studentMapper: StudentMapper
) : StudentRepository {

    override fun searchDomainStudentsFor(searchQuery: String): List<DomainStudent> {
        return localDataSource.getLocalStudentsFor(searchQuery).map { studentMapper.mapLocalToDomain(it) }
    }

    override suspend fun getDomainStudentsByClassId(educationClassId: String): List<DomainStudent> {
        return localDataSource.getLocalStudentsByClassId(educationClassId).map { studentMapper.mapLocalToDomain(it) }
    }

    override suspend fun getDomainStudentById(id: String): DomainStudent? {
        return localDataSource.getLocalStudentById(id)?.let {
            studentMapper.mapLocalToDomain(it)
        }
    }

    override suspend fun getDomainStudentsFlow(): Flow<List<DomainStudent>> {
        return localDataSource.getLocalStudentsFlow().map { students ->
            students.map { studentMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun createDomainStudent(organization: String, domainStudent: DomainStudent) {
        // Save locally first with a pending sync status
        val localStudent = studentMapper.mapDomainToLocal(domainStudent, SyncType.PENDING_CREATION)
        localDataSource.saveLocalStudent(localStudent)

        // Attempt to sync remotely
        try {
            val remoteStudent = studentMapper.mapDomainToRemote(domainStudent)
            val createdRemoteStudent = remoteDataSource.createRemoteStudent(organization, remoteStudent)
            val createdDomainStudent = studentMapper.mapRemoteToDomain(createdRemoteStudent)
            val syncedLocalStudent = studentMapper.mapDomainToLocal(createdDomainStudent, SyncType.SYNCED)
            localDataSource.saveLocalStudent(syncedLocalStudent)
        } catch (e: Exception) {
            // Sync failure; the local data will remain pending for future sync
        }
    }

    override suspend fun updateDomainStudent(organization: String, domainStudent: DomainStudent) {
        // Update locally first with a pending sync status
        val localStudent = studentMapper.mapDomainToLocal(domainStudent, SyncType.PENDING_MODIFICATION)
        localDataSource.saveLocalStudent(localStudent)

        // Attempt to sync remotely
        try {
            val remoteStudent = studentMapper.mapDomainToRemote(domainStudent)
            val updatedRemoteStudent = remoteDataSource.updateRemoteStudent(organization, remoteStudent)
            val updatedDomainStudent = studentMapper.mapRemoteToDomain(updatedRemoteStudent)
            val syncedLocalStudent = studentMapper.mapDomainToLocal(updatedDomainStudent, SyncType.SYNCED)
            localDataSource.saveLocalStudent(syncedLocalStudent)
        } catch (e: Exception) {
            // Sync failure; the local data will remain pending for future sync
        }
    }

    override suspend fun deleteDomainStudent(organization: String, domainStudent: DomainStudent) {
        // Mark as pending deletion locally first
        localDataSource.markLocalStudentAsPendingDeletion(domainStudent.id)

        // Attempt to delete remotely
        try {
            val remoteStudent = studentMapper.mapDomainToRemote(domainStudent)
            remoteDataSource.deleteRemoteStudent(organization, remoteStudent)
            localDataSource.deleteLocalStudent(domainStudent.id)
        } catch (e: Exception) {
            // Sync failure; deletion will remain pending
        }
    }

    override suspend fun syncLocalWithRemoteStudents(organization: String) {
        // Fetch unsynced students from the local database
        val unSyncedStudents = localDataSource.getUnSyncedLocalStudents()

        // Try to sync them with the server
        for (student in unSyncedStudents) {
            try {
                when (student.syncType) {
                    SyncType.PENDING_CREATION -> createDomainStudent(organization, studentMapper.mapLocalToDomain(student))
                    SyncType.PENDING_MODIFICATION -> {
                        val domainStudent = studentMapper.mapLocalToDomain(student)
                        updateDomainStudent(organization, domainStudent)
                    }
                    SyncType.PENDING_DELETION -> {
                        val domainStudent = studentMapper.mapLocalToDomain(student)
                        deleteDomainStudent(organization, domainStudent)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                // Handle sync failure (e.g., log error, retry later)
            }
        }

        fetchLatestRemoteStudentsAndUpdateLocalStudents(organization)
    }

    private suspend fun fetchLatestRemoteStudentsAndUpdateLocalStudents(organization: String) {
        try {
            val remoteStudents = remoteDataSource.getRemoteStudents(organization)
            val domainStudents = remoteStudents.map { studentMapper.mapRemoteToDomain(it) }
            val localStudents = domainStudents.map { studentMapper.mapDomainToLocal(it, SyncType.SYNCED) }
            localDataSource.saveLocalStudents(localStudents)
        } catch (e: Exception) {
            // If fetching from remote fails, fallback to local
        }
    }
}
