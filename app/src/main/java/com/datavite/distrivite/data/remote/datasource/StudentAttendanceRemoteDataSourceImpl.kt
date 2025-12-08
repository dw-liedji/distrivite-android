package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteStudentAttendance
import com.datavite.distrivite.data.remote.service.StudentAttendanceService
import javax.inject.Inject

class StudentAttendanceRemoteDataSourceImpl @Inject constructor(
    private val attendanceService: StudentAttendanceService
) : StudentAttendanceRemoteDataSource {
    override suspend fun getAttendances(organization:String): List<RemoteStudentAttendance> {
        return attendanceService.getAttendances(organization)
    }

    override suspend fun createAttendance(organization:String, attendance: RemoteStudentAttendance) {
        attendanceService.createAttendance(organization, attendance)
    }

    override suspend fun updateAttendance(organization:String, attendance: RemoteStudentAttendance) {
        attendanceService.updateAttendance(organization, attendance.id, attendance)
    }

    override suspend fun deleteAttendance(organization:String, attendanceId: String) {
        attendanceService.deleteAttendance(organization,attendanceId)
    }
}