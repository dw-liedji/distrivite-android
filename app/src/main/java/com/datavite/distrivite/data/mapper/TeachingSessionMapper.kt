package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.remote.model.RemoteTeachingSession
import com.datavite.distrivite.data.local.model.LocalTeachingSession
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.domain.model.DomainTeachingSession

class TeachingSessionMapper {

    // Remote to Domain
    fun mapRemoteToDomain(remote: RemoteTeachingSession): DomainTeachingSession {
        return DomainTeachingSession(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgSlug = remote.orgSlug,
            course = remote.course,
            orgId = remote.orgId,
            room = remote.room,
            start = remote.start,
            end = remote.end,
            rStart = remote.rStart, // nullable local for null value
            rEnd = remote.rEnd, // nullable local for null value
            day = remote.day,
            option = remote.option,
            level = remote.level,
            cursus = remote.cursus,
            instructor = remote.instructor,
            instructorId = remote.instructorId,
            status = remote.status,
            scenario = remote.scenario,
            userId = remote.userId,
            orgUserId = remote.orgUserId,
            courseId = remote.courseId,
            roomId = remote.roomId,
            teachingPeriodId = remote.teachingPeriodId,
            hourlyRemuneration = remote.hourlyRemuneration,
            instructorContractId = remote.instructorContractId,
            klass = remote.klass,
            syncStatus = SyncStatus.SYNCED,
            educationClassId = remote.educationClassId,
            parentsNotified = remote.parentsNotified
        )
    }

    // Domain to Remote
    fun mapDomainToRemote(domain: DomainTeachingSession): RemoteTeachingSession {
        return RemoteTeachingSession(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            course = domain.course,
            orgId = domain.orgId,
            room = domain.room,
            start = domain.start,
            end = domain.end,
            rStart = domain.rStart, // nullable domain for null value
            rEnd = domain.rEnd, // nullable domain for null value
            day = domain.day,
            option = domain.option,
            level = domain.level,
            cursus = domain.cursus,
            instructor = domain.instructor,
            instructorId = domain.instructorId,
            status = domain.status,
            scenario = domain.scenario,
            userId = domain.userId,
            orgUserId = domain.orgUserId,
            courseId = domain.courseId,
            roomId = domain.roomId,
            teachingPeriodId = domain.teachingPeriodId,
            hourlyRemuneration = domain.hourlyRemuneration,
            instructorContractId = domain.instructorContractId,
            klass = domain.klass,
            educationClassId = domain.educationClassId,
            parentsNotified = domain.parentsNotified

        )
    }

    // Local to Domain
    fun mapLocalToDomain(local: LocalTeachingSession): DomainTeachingSession {
        return DomainTeachingSession(
            id = local.id,
            created = local.created.toString(),
            modified = local.modified.toString(),
            orgSlug = local.orgSlug,
            course = local.course,
            orgId = local.orgId,
            room = local.room,
            start = local.start,
            end = local.end,
            rStart = local.rStart, // nullable local for null value
            rEnd = local.rEnd, // nullable local for null value
            day = local.day,
            option = local.option,
            level = local.level,
            cursus = local.cursus,
            instructor = local.instructor,
            instructorId = local.instructorId,
            status = local.status,
            scenario = local.scenario,
            userId = local.userId,
            orgUserId = local.orgUserId,
            courseId = local.courseId,
            roomId = local.roomId,
            teachingPeriodId = local.teachingPeriodId,
            hourlyRemuneration = local.hourlyRemuneration,
            instructorContractId = local.instructorContractId,
            klass = local.klass,
            educationClassId = local.educationClassId,
            syncStatus = local.syncStatus,
            parentsNotified = local.parentsNotified

        )
    }

    // Domain to Local
    fun mapDomainToLocal(domain: DomainTeachingSession): LocalTeachingSession {
        return LocalTeachingSession(
            created = domain.created,
            modified = domain.modified,
            id = domain.id,
            orgSlug = domain.orgSlug,
            course = domain.course,
            orgId = domain.orgId,
            room = domain.room,
            start = domain.start,
            end = domain.end,
            rStart = domain.rStart, // nullable domain for null value
            rEnd = domain.rEnd, // nullable domain for null value
            day = domain.day,
            option = domain.option,
            level = domain.level,
            cursus = domain.cursus,
            instructor = domain.instructor,
            instructorId = domain.instructorId,
            status = domain.status,
            scenario = domain.scenario,
            userId = domain.userId,
            orgUserId = domain.orgUserId,
            klass = domain.klass,
            courseId = domain.courseId,
            roomId = domain.roomId,
            teachingPeriodId = domain.teachingPeriodId,
            hourlyRemuneration = domain.hourlyRemuneration,
            instructorContractId = domain.instructorContractId,
            educationClassId = domain.educationClassId,
            syncStatus = domain.syncStatus,
            parentsNotified = domain.parentsNotified
        )
    }

}

