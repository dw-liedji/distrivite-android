package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalClaim
import com.datavite.distrivite.data.remote.model.RemoteClaim
import com.datavite.distrivite.domain.model.DomainClaim

class ClaimMapper {

    // Convert RemoteClaim to DomainClaim
    fun mapRemoteToDomain(remoteClaim: RemoteClaim): DomainClaim {
        return DomainClaim(
            id = remoteClaim.id,
            created = remoteClaim.created,
            modified = remoteClaim.modified,
            orgId = remoteClaim.orgId,
            userId = remoteClaim.userId,
            orgUserId = remoteClaim.orgUserId,
            orgSlug = remoteClaim.orgSlug,
            employeeId = remoteClaim.employeeId,
            type = remoteClaim.type,
            claimedHours = remoteClaim.claimedHours,
            date = remoteClaim.date,
            status = remoteClaim.status,
            reason = remoteClaim.reason,
            employeeName = remoteClaim.employeeName,
            hourlySalary = remoteClaim.hourlySalary
        )
    }

    // Convert LocalClaim to DomainClaim
    fun mapLocalToDomain(localClaim: LocalClaim): DomainClaim {
        return DomainClaim(
            id = localClaim.id,
            created = localClaim.created,
            modified = localClaim.modified,
            orgId = localClaim.orgId,
            userId = localClaim.userId,
            orgUserId = localClaim.orgUserId,
            orgSlug = localClaim.orgSlug,
            employeeId = localClaim.employeeId,
            type = localClaim.type,
            claimedHours = localClaim.claimedHours,
            date = localClaim.date,
            status = localClaim.status,
            reason = localClaim.reason,
            employeeName = localClaim.employeeName,
            hourlySalary = localClaim.hourlySalary
        )
    }

    // Convert DomainClaim to RemoteClaim
    fun mapDomainToRemote(domainClaim: DomainClaim): RemoteClaim {
        return RemoteClaim(
            id = domainClaim.id,
            created = domainClaim.created,
            modified = domainClaim.modified,
            orgId = domainClaim.orgId,
            userId = domainClaim.userId,
            orgUserId = domainClaim.orgUserId,
            orgSlug = domainClaim.orgSlug,
            employeeId = domainClaim.employeeId,
            type = domainClaim.type,
            claimedHours = domainClaim.claimedHours,
            date = domainClaim.date,
            status = domainClaim.status,
            reason = domainClaim.reason,
            employeeName = domainClaim.employeeName,
            hourlySalary = domainClaim.hourlySalary
        )
    }

    // Convert DomainClaim to LocalClaim
    fun mapDomainToLocal(domainClaim: DomainClaim, syncType: SyncType): LocalClaim {
        return LocalClaim(
            id = domainClaim.id,
            created = domainClaim.created,
            modified = domainClaim.modified,
            orgSlug = domainClaim.orgSlug,
            orgId = domainClaim.orgId,
            userId = domainClaim.userId,
            orgUserId = domainClaim.orgUserId,
            employeeId = domainClaim.employeeId,
            type = domainClaim.type,
            claimedHours = domainClaim.claimedHours,
            date = domainClaim.date,
            status = domainClaim.status,
            reason = domainClaim.reason,
            employeeName = domainClaim.employeeName,
            hourlySalary = domainClaim.hourlySalary,
            syncType = syncType
        )
    }
}
