package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.model.LocalTeachingPeriod
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.remote.model.RemoteTeachingPeriod
import com.datavite.distrivite.domain.model.DomainTeachingPeriod

class TeachingPeriodMapper {
    // Remote to Domain
    fun mapRemoteToDomain(remote: RemoteTeachingPeriod): DomainTeachingPeriod {
        return DomainTeachingPeriod(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgSlug = remote.orgSlug,
            orgId = remote.orgId,
            start = remote.start,
            end = remote.end,
        )
    }

    // Domain to Remote
    fun mapDomainToRemote(domain: DomainTeachingPeriod): RemoteTeachingPeriod {
        return RemoteTeachingPeriod(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            start = domain.start,
            end = domain.end,
        )
    }

    // Local to Domain
    fun mapLocalToDomain(local: LocalTeachingPeriod): DomainTeachingPeriod {
        return DomainTeachingPeriod(
            id = local.id,
            created = local.created,
            modified = local.modified,
            orgSlug = local.orgSlug,
            orgId = local.orgId,
            start = local.start,
            end = local.end,
        )
    }

    // Domain to Local
    fun mapDomainToLocal(domain: DomainTeachingPeriod, syncType: SyncType): LocalTeachingPeriod {
        return LocalTeachingPeriod(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            start = domain.start,
            end = domain.end,
            syncType = syncType
        )
    }
}

