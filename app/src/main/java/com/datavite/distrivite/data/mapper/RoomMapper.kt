package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalRoom
import com.datavite.distrivite.data.remote.model.RemoteRoom
import com.datavite.distrivite.domain.model.DomainRoom

class RoomMapper {
    // Remote to Domain
    fun mapRemoteToDomain(remote: RemoteRoom): DomainRoom {
        return DomainRoom(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgSlug = remote.orgSlug,
            orgId = remote.orgId,
            name = remote.name,
        )
    }

    // Domain to Remote
    fun mapDomainToRemote(domain: DomainRoom): RemoteRoom {
        return RemoteRoom(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            name = domain.name,
        )
    }

    // Local to Domain
    fun mapLocalToDomain(local: LocalRoom): DomainRoom {
        return DomainRoom(
            id = local.id,
            created = local.created,
            modified = local.modified,
            orgSlug = local.orgSlug,
            orgId = local.orgId,
            name = local.name,
        )
    }

    // Domain to Local
    fun mapDomainToLocal(domain: DomainRoom, syncType: SyncType): LocalRoom {
        return LocalRoom(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            name = domain.name,
            syncType = syncType
        )
    }
}

