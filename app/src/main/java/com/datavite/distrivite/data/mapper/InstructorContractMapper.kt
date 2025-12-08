package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalInstructorContract
import com.datavite.distrivite.data.remote.model.RemoteInstructorContract
import com.datavite.distrivite.domain.model.DomainInstructorContract

class InstructorContractMapper {

    // Remote to Domain
    fun mapRemoteToDomain(remote: RemoteInstructorContract): DomainInstructorContract {
        return DomainInstructorContract(
            id = remote.id,
            orgId = remote.orgId,
            userId = remote.userId,
            orgSlug = remote.orgSlug,
            name = remote.name,
            modified = remote.modified,
            created = remote.created,
            embeddings = remote.embeddings,
            isManager = remote.isManager,
            isActive = remote.isActive,
            orgUserId = remote.orgUserId,
            instructorId = remote.instructorId,
            contract = remote.contract,
            checkInLatitude = remote.checkInLatitude,
            checkInLongitude = remote.checkInLongitude,
            checkOutLatitude = remote.checkOutLatitude,
            checkOutLongitude = remote.checkOutLongitude
        )
    }

    // Domain to Remote
    fun mapDomainToRemote(domain: DomainInstructorContract): RemoteInstructorContract {
        return RemoteInstructorContract(
            id = domain.id,
            orgId = domain.orgId,
            userId = domain.userId,
            orgSlug = domain.orgSlug,
            name = domain.name,
            modified = domain.modified,
            created = domain.created,
            embeddings = domain.embeddings,
            isManager = domain.isManager,
            orgUserId = domain.orgUserId,
            instructorId = domain.instructorId,
            contract = domain.contract,
            isActive = domain.isActive,
            checkInLatitude = domain.checkInLatitude,
            checkInLongitude = domain.checkInLongitude,
            checkOutLatitude = domain.checkOutLatitude,
            checkOutLongitude = domain.checkOutLongitude
        )
    }

    // Local to Domain
    fun mapLocalToDomain(local: LocalInstructorContract): DomainInstructorContract {
        return DomainInstructorContract(
            id = local.id,
            orgId = local.orgId,
            userId = local.userId,
            orgSlug = local.orgSlug,
            name = local.name,
            modified = local.modified.toString(),
            created = local.created.toString(),
            embeddings = local.embeddings,
            isManager = local.isManager,
            orgUserId = local.orgUserId,
            contract = local.contract,
            instructorId = local.instructorId,
            isActive = local.isActive,
            checkInLatitude = local.checkInLatitude,
            checkInLongitude = local.checkInLongitude,
            checkOutLatitude = local.checkOutLatitude,
            checkOutLongitude = local.checkOutLongitude
        )
    }

    // Domain to Local
    fun mapDomainToLocal(domain: DomainInstructorContract, syncType: SyncType): LocalInstructorContract {
        return LocalInstructorContract(
            id = domain.id,
            created = System.currentTimeMillis(),
            modified = System.currentTimeMillis(),
            name = domain.name,
            orgSlug = domain.orgSlug,
            orgId = domain.orgSlug, // Assuming orgSlug is unique for local storage
            userId = domain.id,
            instructorId = domain.instructorId,
            embeddings = domain.embeddings,
            isManager = domain.isManager,
            contract = domain.contract,
            orgUserId = domain.orgUserId,
            isActive = domain.isActive,
            checkInLatitude = domain.checkInLatitude,
            checkInLongitude = domain.checkInLongitude,
            checkOutLatitude = domain.checkOutLatitude,
            checkOutLongitude = domain.checkOutLongitude,
            syncType = syncType
        )
    }
}
