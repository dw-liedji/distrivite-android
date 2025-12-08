package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalLeave
import com.datavite.distrivite.data.remote.model.RemoteLeave
import com.datavite.distrivite.domain.model.DomainLeave

class LeaveMapper {

    // Convert RemoteLeave to DomainLeave
    fun mapRemoteToDomain(remoteLeave: RemoteLeave): DomainLeave {
        return DomainLeave(
            id = remoteLeave.id,
            created = remoteLeave.created,
            modified = remoteLeave.modified,
            orgId = remoteLeave.orgId,
            userId = remoteLeave.userId,
            orgUserId = remoteLeave.orgUserId,
            orgSlug = remoteLeave.orgSlug,
            employeeId = remoteLeave.employeeId,
            type = remoteLeave.type,
            startDate = remoteLeave.startDate,
            endDate = remoteLeave.endDate,
            status = remoteLeave.status,
            reason = remoteLeave.reason,
            employeeName = remoteLeave.employeeName,
            hourlySalary = remoteLeave.hourlySalary
        )
    }

    // Convert LocalLeave to DomainLeave
    fun mapLocalToDomain(localLeave: LocalLeave): DomainLeave {
        return DomainLeave(
            id = localLeave.id,
            created = localLeave.created,
            modified = localLeave.modified,
            orgId = localLeave.orgId,
            userId = localLeave.userId,
            orgUserId = localLeave.orgUserId,
            orgSlug = localLeave.orgSlug,
            employeeId = localLeave.employeeId,
            type = localLeave.type,
            startDate = localLeave.startDate,
            endDate = localLeave.endDate,
            status = localLeave.status,
            reason = localLeave.reason,
            employeeName = localLeave.employeeName,
            hourlySalary = localLeave.hourlySalary
        )
    }

    // Convert DomainLeave to RemoteLeave
    fun mapDomainToRemote(domainLeave: DomainLeave): RemoteLeave {
        return RemoteLeave(
            id = domainLeave.id,
            created = domainLeave.created,
            modified = domainLeave.modified,
            orgId = domainLeave.orgId,
            userId = domainLeave.userId,
            orgUserId = domainLeave.orgUserId,
            orgSlug = domainLeave.orgSlug,
            employeeId = domainLeave.employeeId,
            type = domainLeave.type,
            startDate = domainLeave.startDate,
            endDate = domainLeave.endDate,
            status = domainLeave.status,
            reason = domainLeave.reason,
            employeeName = domainLeave.employeeName,
            hourlySalary = domainLeave.hourlySalary
        )
    }

    // Convert DomainLeave to LocalLeave
    fun mapDomainToLocal(domainLeave: DomainLeave, syncType: SyncType): LocalLeave {
        return LocalLeave(
            id = domainLeave.id,
            created = domainLeave.created,
            modified = domainLeave.modified,
            orgSlug = domainLeave.orgSlug,
            orgId = domainLeave.orgId,
            userId = domainLeave.userId,
            orgUserId = domainLeave.orgUserId,
            employeeId = domainLeave.employeeId,
            type = domainLeave.type,
            startDate = domainLeave.startDate,
            endDate = domainLeave.endDate,
            status = domainLeave.status,
            reason = domainLeave.reason,
            hourlySalary = domainLeave.hourlySalary,
            employeeName = domainLeave.employeeName,
            syncType = syncType
        )
    }
}
