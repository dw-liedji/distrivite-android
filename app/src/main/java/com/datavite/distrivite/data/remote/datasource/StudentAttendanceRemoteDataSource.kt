package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteStudentAttendance

interface StudentAttendanceRemoteDataSource {
    suspend fun getAttendances(organization:String): List<RemoteStudentAttendance>
    suspend fun createAttendance(organization:String, attendance: RemoteStudentAttendance)
    suspend fun updateAttendance(organization:String, attendance: RemoteStudentAttendance)
    suspend fun deleteAttendance(organization:String, attendanceId: String)
}