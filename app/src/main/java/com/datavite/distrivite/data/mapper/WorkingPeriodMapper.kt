package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalWorkingPeriod
import com.datavite.distrivite.data.remote.model.RemoteWorkingPeriod
import com.datavite.distrivite.domain.model.DomainWorkingPeriod

class WorkingPeriodMapper {

    // Remote to Domain
    fun mapRemoteToDomain(remote: RemoteWorkingPeriod): DomainWorkingPeriod {
        return DomainWorkingPeriod(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgSlug = remote.orgSlug,
            orgId = remote.orgId,
            day = remote.day,
            startTime = remote.startTime,
            endTime = remote.endTime,
            isActive = remote.isActive,
            dayId = remote.dayId
        )
    }

    // Domain to Local
    fun mapDomainToLocal(domain: DomainWorkingPeriod, syncType: SyncType): LocalWorkingPeriod {
        return LocalWorkingPeriod(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            day = domain.day,
            startTime = domain.startTime,
            endTime = domain.endTime,
            isActive = domain.isActive,
            dayId = domain.dayId,
            syncType = SyncType.UNDEFINED // Assuming a default SyncType, adjust as needed
        )
    }

    // Local to Domain
    fun mapLocalToDomain(local: LocalWorkingPeriod): DomainWorkingPeriod {
        return DomainWorkingPeriod(
            id = local.id,
            created = local.created,
            modified = local.modified,
            orgSlug = local.orgSlug,
            orgId = local.orgId,
            day = local.day,
            startTime = local.startTime,
            endTime = local.endTime,
            dayId = local.dayId,
            isActive = local.isActive,
        )
    }

    // Remote to Local (optional)
    fun mapRemoteToLocal(remote: RemoteWorkingPeriod): LocalWorkingPeriod {
        return LocalWorkingPeriod(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgSlug = remote.orgSlug,
            orgId = remote.orgId,
            day = remote.day,
            startTime = remote.startTime,
            endTime = remote.endTime,
            isActive = remote.isActive,
            dayId = remote.dayId,
            syncType = SyncType.UNDEFINED // Adjust as needed
        )
    }

    // Domain to Remote (optional)
    fun mapDomainToRemote(domain: DomainWorkingPeriod): RemoteWorkingPeriod {
        return RemoteWorkingPeriod(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            day = domain.day,
            startTime = domain.startTime,
            endTime = domain.endTime,
            dayId = domain.dayId,
            isActive = domain.isActive
        )
    }
}
