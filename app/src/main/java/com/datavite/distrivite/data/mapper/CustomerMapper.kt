package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.model.LocalCustomer
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.remote.model.RemoteCustomer
import com.datavite.distrivite.domain.model.DomainCustomer

class CustomerMapper {

    // --- Remote → Domain ---
    fun mapRemoteToDomain(remote: RemoteCustomer): DomainCustomer {
        return DomainCustomer(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgSlug = remote.orgSlug,
            orgId = remote.orgId,
            name = remote.name,
            phoneNumber = remote.phoneNumber,
            syncStatus = SyncStatus.SYNCED
        )
    }

    // --- Domain → Remote ---
    fun mapDomainToRemote(domain: DomainCustomer): RemoteCustomer {
        return RemoteCustomer(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            name = domain.name,
            phoneNumber = domain.phoneNumber
        )
    }

    // --- Local → Domain ---
    fun mapLocalToDomain(local: LocalCustomer): DomainCustomer {
        return DomainCustomer(
            id = local.id,
            created = local.created,
            modified = local.modified,
            orgSlug = local.orgSlug,
            orgId = local.orgId,
            name = local.name,
            phoneNumber = local.phoneNumber,
            syncStatus = local.syncStatus
        )
    }

    // --- Domain → Local ---
    fun mapDomainToLocal(domain: DomainCustomer): LocalCustomer {
        return LocalCustomer(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            name = domain.name,
            phoneNumber = domain.phoneNumber,
            syncStatus = domain.syncStatus
        )
    }
}
