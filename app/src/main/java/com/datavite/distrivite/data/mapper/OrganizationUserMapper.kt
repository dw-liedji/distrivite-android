package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalOrganizationUser
import com.datavite.distrivite.data.remote.model.RemoteOrganizationUser
import com.datavite.distrivite.domain.model.DomainOrganizationUser

class OrganizationUserMapper {

    // Remote to Domain
    fun mapRemoteToDomain(remote: RemoteOrganizationUser): DomainOrganizationUser {
        return DomainOrganizationUser(
            id = remote.id,
            orgId = remote.orgId,
            userId = remote.userId,
            orgSlug = remote.orgSlug,
            name = remote.name,
            modified = remote.modified,
            created = remote.created,
            isActive = remote.isActive,
            isAdmin = remote.isAdmin,
            embeddings = remote.embeddings
        )
    }

    // Domain to Remote
    fun mapDomainToRemote(domain: DomainOrganizationUser): RemoteOrganizationUser {
        return RemoteOrganizationUser(
            id = domain.id,
            orgId = domain.orgId,
            userId = domain.userId,
            orgSlug = domain.orgSlug,
            name = domain.name,
            modified = domain.modified,
            created = domain.created,
            isActive = domain.isActive,
            isAdmin = domain.isAdmin,
            embeddings = domain.embeddings
        )
    }

    // Local to Domain
    fun mapLocalToDomain(local: LocalOrganizationUser): DomainOrganizationUser {
        return DomainOrganizationUser(
            id = local.id,
            orgId = local.orgId,
            userId = local.userId,
            orgSlug = local.orgSlug,
            name = local.name,
            modified = local.modified.toString(),
            created = local.created.toString(),
            isActive = local.isActive,
            isAdmin = local.isAdmin,
            embeddings = local.embeddings
        )
    }

    // Domain to Local
    fun mapDomainToLocal(domain: DomainOrganizationUser, syncType: SyncType): LocalOrganizationUser {
        return LocalOrganizationUser(
            id = domain.id,
            created = System.currentTimeMillis(),
            modified = System.currentTimeMillis(),
            name = domain.name,
            orgSlug = domain.orgSlug,
            orgId = domain.orgSlug, // Assuming orgSlug is unique for local storage
            userId = domain.id,
            isActive = domain.isActive,
            isAdmin = domain.isAdmin,
            embeddings = domain.embeddings,
            syncType = syncType
        )
    }
}
