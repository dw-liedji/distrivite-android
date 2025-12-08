package com.datavite.distrivite.domain.repository

import com.datavite.distrivite.domain.model.DomainStudent
import kotlinx.coroutines.flow.Flow

interface StudentRepository {

    fun searchDomainStudentsFor(searchQuery: String): List<DomainStudent>
    suspend fun getDomainStudentsByClassId(educationClassId: String):  List<DomainStudent>

    suspend fun getDomainStudentById(id: String): DomainStudent?
    suspend fun getDomainStudentsFlow(): Flow<List<DomainStudent>>
    suspend fun createDomainStudent(organization: String, domainStudent: DomainStudent)
    suspend fun updateDomainStudent(organization: String, domainStudent: DomainStudent)
    suspend fun deleteDomainStudent(organization: String, domainStudent: DomainStudent)
    suspend fun syncLocalWithRemoteStudents(organization: String)
}