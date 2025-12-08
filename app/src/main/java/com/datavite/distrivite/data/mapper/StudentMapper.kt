package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalStudent
import com.datavite.distrivite.data.remote.model.RemoteStudent
import com.datavite.distrivite.domain.model.DomainStudent

class StudentMapper {

    // Remote to Domain
    fun mapRemoteToDomain(remote: RemoteStudent): DomainStudent {
        return DomainStudent(
            id = remote.id,
            orgId = remote.orgId,
            userId = remote.userId,
            orgSlug = remote.orgSlug,
            name = remote.name,
            modified = remote.modified,
            created = remote.created,
            isActive = remote.isActive,
            embeddings = remote.embeddings,
            educationClass = remote.educationClass,
            educationClassId = remote.educationClassId,
            hasMailingTracking = remote.hasMailingTracking,
            hasSmsTracking = remote.hasSmsTracking,
            isDelegate = remote.isDelegate
        )
    }

    // Domain to Remote
    fun mapDomainToRemote(domain: DomainStudent): RemoteStudent {
        return RemoteStudent(
            id = domain.id,
            orgId = domain.orgId,
            userId = domain.userId,
            orgSlug = domain.orgSlug,
            name = domain.name,
            modified = domain.modified,
            created = domain.created,
            isActive = domain.isActive,
            embeddings = domain.embeddings,
            educationClass = domain.educationClass,
            educationClassId = domain.educationClassId,
            hasMailingTracking = domain.hasMailingTracking,
            hasSmsTracking = domain.hasSmsTracking,
            isDelegate = domain.isDelegate
        )
    }

    // Local to Domain
    fun mapLocalToDomain(local: LocalStudent): DomainStudent {
        return DomainStudent(
            id = local.id,
            orgId = local.orgId,
            userId = local.userId,
            orgSlug = local.orgSlug,
            name = local.name,
            modified = local.modified,
            created = local.created,
            isActive = local.isActive,
            embeddings = local.embeddings,
            educationClass = local.educationClass,
            educationClassId = local.educationClassId,
            hasMailingTracking = local.hasMailingTracking,
            hasSmsTracking = local.hasSmsTracking,
            isDelegate = local.isDelegate)
    }

    // Domain to Local
    fun mapDomainToLocal(domain: DomainStudent, syncType: SyncType): LocalStudent {
        return LocalStudent(
            id = domain.id,
            orgId = domain.orgId,
            userId = domain.userId,
            orgSlug = domain.orgSlug,
            name = domain.name,
            modified = domain.modified,
            created = domain.created,
            embeddings = domain.embeddings,
            isActive = domain.isActive,
            educationClass = domain.educationClass,
            educationClassId = domain.educationClassId,
            hasMailingTracking = domain.hasMailingTracking,
            hasSmsTracking = domain.hasSmsTracking,
            isDelegate = domain.isDelegate,
            syncType = syncType
        )
    }
}
