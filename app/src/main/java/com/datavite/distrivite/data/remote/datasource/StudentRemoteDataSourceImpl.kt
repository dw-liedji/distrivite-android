package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteStudent
import com.datavite.distrivite.data.remote.service.RemoteStudentService
import javax.inject.Inject

class StudentRemoteDataSourceImpl @Inject constructor(
    private val remoteStudentService: RemoteStudentService
) : StudentRemoteDataSource {
    override suspend fun getRemoteStudents(organization:String): List<RemoteStudent> {
        return remoteStudentService.getRemoteStudents(organization)
    }

    override suspend fun createRemoteStudent(organization:String, remoteStudent: RemoteStudent) : RemoteStudent {
        TODO("Not yet implemented")
    }

    override suspend fun updateRemoteStudent(organization:String, remoteStudent: RemoteStudent) : RemoteStudent {
        return remoteStudentService.updateRemoteStudent(organization=organization, remoteStudent.id, remoteStudent)
    }

    override suspend fun deleteRemoteStudent(organization:String, remoteStudent: RemoteStudent) : RemoteStudent{
        return remoteStudentService.deleteRemoteStudent(organization, remoteStudent.id, remoteStudent)
    }
}