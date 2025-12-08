package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.dao.LocalStudentDao
import com.datavite.distrivite.data.local.model.LocalStudent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StudentLocalDataSourceImpl @Inject constructor (
    private val localStudentDao: LocalStudentDao,
) : StudentLocalDataSource{
    override fun getLocalStudentsFor(searchQuery: String): List<LocalStudent> {
        if (searchQuery.isEmpty()) return localStudentDao.getAllLocalStudents()
        return localStudentDao.getLocalStudentsFor(searchQuery)
    }

    override suspend fun getLocalStudentById(id: String): LocalStudent? {
        return localStudentDao.getLocalStudentById(id)
    }

    override suspend fun getLocalStudentsFlow(): Flow<List<LocalStudent>> {
        return localStudentDao.getLocalStudentsFlow()
    }

    override suspend fun getLocalStudentsByClassId(educationClassId: String): List<LocalStudent> {
        return localStudentDao.getLocalStudentsByClassId(educationClassId)
    }

    override suspend fun getUnSyncedLocalStudents(): List<LocalStudent> {
        return localStudentDao.getUnSyncedLocalStudents()
    }

    override suspend fun saveLocalStudents(localStudents: List<LocalStudent>) {
        localStudentDao.insertOrUpdateLocalStudents(localStudents)
    }

    override suspend fun markLocalStudentAsSynced(localStudent: LocalStudent) {
        localStudentDao.markLocalStudentAsSynced(localStudent)
    }

    override suspend fun saveLocalStudent(localStudent: LocalStudent) {
        localStudentDao.saveLocalStudent(localStudent)
    }

    override suspend fun deleteLocalStudent(id: String) {
        localStudentDao.deleteLocalStudent(id)
    }

    override suspend fun markLocalStudentAsPendingDeletion(id: String) {
        localStudentDao.markLocalStudentAsPendingDeletion(id = id)
    }
}