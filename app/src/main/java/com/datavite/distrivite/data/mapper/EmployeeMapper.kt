package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalEmployee
import com.datavite.distrivite.data.remote.model.RemoteEmployee
import com.datavite.distrivite.domain.model.DomainEmployee

class EmployeeMapper {

    // Remote to Domain
    fun mapRemoteToDomain(remote: RemoteEmployee): DomainEmployee {
        return DomainEmployee(
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
            workingDays = remote.workingDays,
            department = remote.department,
            monthlySalary = remote.monthlySalary,
            checkInLatitude = remote.checkInLatitude,
            checkInLongitude = remote.checkInLongitude,
            checkOutLatitude = remote.checkOutLatitude,
            checkOutLongitude = remote.checkOutLongitude
        )
    }

    // Domain to Remote
    fun mapDomainToRemote(domain: DomainEmployee): RemoteEmployee {
        return RemoteEmployee(
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
            workingDays = domain.workingDays,
            department = domain.department,
            monthlySalary = domain.monthlySalary,
            isActive = domain.isActive,
            checkInLatitude = domain.checkInLatitude,
            checkInLongitude = domain.checkInLongitude,
            checkOutLatitude = domain.checkOutLatitude,
            checkOutLongitude = domain.checkOutLongitude
        )
    }

    // Local to Domain
    fun mapLocalToDomain(local: LocalEmployee): DomainEmployee {
        return DomainEmployee(
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
            workingDays = local.workingDays,
            department = local.department,
            monthlySalary = local.monthlySalary,
            isActive = local.isActive,
            checkInLatitude = local.checkInLatitude,
            checkInLongitude = local.checkInLongitude,
            checkOutLatitude = local.checkOutLatitude,
            checkOutLongitude = local.checkOutLongitude
        )
    }

    // Domain to Local
    fun mapDomainToLocal(domain: DomainEmployee, syncType: SyncType): LocalEmployee {
        return LocalEmployee(
            id = domain.id,
            created = System.currentTimeMillis(),
            modified = System.currentTimeMillis(),
            name = domain.name,
            orgSlug = domain.orgSlug,
            orgId = domain.orgSlug, // Assuming orgSlug is unique for local storage
            userId = domain.id,
            embeddings = domain.embeddings,
            isManager = domain.isManager,
            department = domain.department,
            orgUserId = domain.orgUserId,
            workingDays = domain.workingDays,
            monthlySalary = domain.monthlySalary,
            isActive = domain.isActive,
            checkInLatitude = domain.checkInLatitude,
            checkInLongitude = domain.checkInLongitude,
            checkOutLatitude = domain.checkOutLatitude,
            checkOutLongitude = domain.checkOutLongitude,
            syncType = syncType
        )
    }
}
