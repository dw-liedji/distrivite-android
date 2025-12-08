package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.model.LocalStudent
import kotlinx.coroutines.flow.Flow

interface StudentLocalDataSource {

    fun getLocalStudentsFor(searchQuery: String): List<LocalStudent>

    suspend fun getLocalStudentById(id: String): LocalStudent?
    suspend fun getLocalStudentsFlow(): Flow<List<LocalStudent>>
    suspend fun getLocalStudentsByClassId(educationClassId: String):  List<LocalStudent>

    suspend fun getUnSyncedLocalStudents(): List<LocalStudent>
    suspend fun saveLocalStudents(localStudents: List<LocalStudent>)
    suspend fun markLocalStudentAsSynced(localStudent: LocalStudent)
    suspend fun saveLocalStudent(localStudent: LocalStudent)
    suspend fun deleteLocalStudent(id: String)
    suspend fun markLocalStudentAsPendingDeletion(id: String)
}