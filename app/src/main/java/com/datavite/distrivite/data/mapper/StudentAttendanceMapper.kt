package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.model.LocalStudentAttendance
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.remote.model.RemoteStudentAttendance
import com.datavite.distrivite.domain.model.DomainStudentAttendance

/**
 * A mapper class to convert between RemoteStudentAttendance, LocalStudentAttendance, and DomainStudentAttendance.
 */
class StudentAttendanceMapper {

    /**
     * Converts a [RemoteStudentAttendance] object to a [DomainStudentAttendance] object.
     * @param remote The remote attendance record to convert.
     * @return The corresponding domain attendance record.
     */
    fun mapRemoteToDomain(remote: RemoteStudentAttendance): DomainStudentAttendance {
        return DomainStudentAttendance(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgId = remote.orgId,
            orgSlug = remote.orgSlug,
            sessionName = remote.sessionName,
            sessionId = remote.sessionId,
            studentName = remote.studentName,
            studentId = remote.studentId,
            isPresent = remote.isPresent,
            registerAt = remote.registerAt,
            syncStatus = SyncStatus.SYNCED,
            educationClassId = remote.educationClassId
        )
    }

    /**
     * Converts a [DomainStudentAttendance] object to a [LocalStudentAttendance] object.
     * @param domain The domain attendance record to convert.
     * @return The corresponding local attendance record.
     */
    fun mapDomainToLocal(domain: DomainStudentAttendance): LocalStudentAttendance {
        return LocalStudentAttendance(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgId = domain.orgId,
            orgSlug = domain.orgSlug,
            sessionName = domain.sessionName,
            sessionId = domain.sessionId,
            studentName = domain.studentName,
            studentId = domain.studentId,
            registerAt = domain.registerAt,
            isPresent = domain.isPresent,
            syncStatus = domain.syncStatus,
            educationClassId = domain.educationClassId
        )
    }

    /**
     * Converts a [LocalStudentAttendance] object to a [DomainStudentAttendance] object.
     * @param local The local attendance record to convert.
     * @return The corresponding domain attendance record.
     */
    fun mapLocalToDomain(local: LocalStudentAttendance): DomainStudentAttendance {
        return DomainStudentAttendance(
            id = local.id,
            created = local.created,
            modified = local.modified,
            orgId = local.orgId,
            orgSlug = local.orgSlug,
            sessionName = local.sessionName,
            sessionId = local.sessionId,
            studentName = local.studentName,
            studentId = local.studentId,
            isPresent = local.isPresent,
            registerAt = local.registerAt,
            syncStatus = local.syncStatus,
            educationClassId = local.educationClassId
        )
    }

    /**
     * Converts a [DomainStudentAttendance] object to a [RemoteStudentAttendance] object.
     * @param domain The domain attendance record to convert.
     * @return The corresponding remote attendance record.
     */
    fun mapDomainToRemote(domain: DomainStudentAttendance): RemoteStudentAttendance {
        return RemoteStudentAttendance(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgId = domain.orgId,
            orgSlug = domain.orgSlug,
            sessionName = domain.sessionName,
            sessionId = domain.sessionId,
            studentName = domain.studentName,
            studentId = domain.studentId,
            registerAt = domain.registerAt,
            isPresent = domain.isPresent,
            educationClassId = domain.educationClassId
        )
    }
}
