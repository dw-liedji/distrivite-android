package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.feature.cameis.data.remote.model.RemoteTeachingCourse
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalTeachingCourse
import com.datavite.distrivite.domain.model.DomainTeachingCourse

class TeachingCourseMapper {

    // Remote to Domain
    fun mapRemoteToDomain(remote: RemoteTeachingCourse): DomainTeachingCourse {
        return DomainTeachingCourse(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgSlug = remote.orgSlug,
            course = remote.course,
            credit = remote.credit.toIntOrNull() ?: 0, // Safely convert to Int, default to 0 if parsing fails
            educationTerm = remote.educationTerm,
            module = remote.module,
            code = remote.code,
            orgId = remote.orgId,
            userId = remote.userId,
            orgUserId = remote.orgUserId,
            option = remote.option,
            level = remote.level,
            cursus = remote.cursus,
            klass = remote.klass,
            instructor = remote.instructor,
            instructorId = remote.instructorId,
            hourlyRemuneration = remote.hourlyRemuneration,
            educationClassId = remote.educationClassId,
            durationInHours = remote.durationInHours.toFloatOrNull() ?: 0f, // Safely convert to Float
            progression = remote.progression.toFloatOrNull() ?: 0f // Safely convert to Float
        )
    }

    // Domain to Local
    fun mapDomainToLocal(domain: DomainTeachingCourse, syncType: SyncType): LocalTeachingCourse {
        return LocalTeachingCourse(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            course = domain.course,
            credit = domain.credit,
            educationTerm = domain.educationTerm,
            hourlyRemuneration = domain.hourlyRemuneration,
            educationClassId = domain.educationClassId,
            module = domain.module,
            code = domain.code,
            orgId = domain.orgId,
            userId = domain.userId,
            orgUserId = domain.orgUserId,
            option = domain.option,
            level = domain.level,
            cursus = domain.cursus,
            klass = domain.klass,
            instructor = domain.instructor,
            instructorId = domain.instructorId,
            durationInHours = domain.durationInHours,
            progression = domain.progression,
            syncType = SyncType.UNDEFINED // Assuming a default SyncType, adjust as needed
        )
    }

    // Local to Domain
    fun mapLocalToDomain(local: LocalTeachingCourse): DomainTeachingCourse {
        return DomainTeachingCourse(
            id = local.id,
            created = local.created,
            modified = local.modified,
            orgSlug = local.orgSlug,
            course = local.course,
            credit = local.credit,
            educationTerm = local.educationTerm,
            hourlyRemuneration = local.hourlyRemuneration,
            module = local.module,
            code = local.code,
            orgId = local.orgId,
            userId = local.userId,
            orgUserId = local.orgUserId,
            option = local.option,
            level = local.level,
            cursus = local.cursus,
            klass = local.klass,
            instructor = local.instructor,
            instructorId = local.instructorId,
            durationInHours = local.durationInHours,
            progression = local.progression,
            educationClassId = local.educationClassId,
            )
    }

    // Remote to Local (optional)
    fun mapRemoteToLocal(remote: RemoteTeachingCourse): LocalTeachingCourse {
        return LocalTeachingCourse(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgSlug = remote.orgSlug,
            course = remote.course,
            credit = remote.credit.toIntOrNull() ?: 0,
            educationTerm = remote.educationTerm,
            module = remote.module,
            code = remote.code,
            orgId = remote.orgId,
            userId = remote.userId,
            orgUserId = remote.orgUserId,
            option = remote.option,
            level = remote.level,
            cursus = remote.cursus,
            klass = remote.klass,
            instructor = remote.instructor,
            instructorId = remote.instructorId,
            hourlyRemuneration = remote.hourlyRemuneration,
            educationClassId = remote.educationClassId,
            durationInHours = remote.durationInHours.toFloatOrNull() ?: 0f,
            progression = remote.progression.toFloatOrNull() ?: 0f,
            syncType = SyncType.UNDEFINED // Adjust as needed
        )
    }

    // Domain to Remote (optional)
    fun mapDomainToRemote(domain: DomainTeachingCourse): RemoteTeachingCourse {
        return RemoteTeachingCourse(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            course = domain.course,
            credit = domain.credit.toString(), // Convert Int to String
            educationTerm = domain.educationTerm,
            module = domain.module,
            code = domain.code,
            orgId = domain.orgId,
            userId = domain.userId,
            orgUserId = domain.orgUserId,
            option = domain.option,
            level = domain.level,
            cursus = domain.cursus,
            klass = domain.klass,
            instructor = domain.instructor,
            instructorId = domain.instructorId,
            educationClassId = domain.educationClassId,
            hourlyRemuneration = domain.hourlyRemuneration,
            durationInHours = domain.durationInHours.toString(), // Convert Float to String
            progression = domain.progression.toString() // Convert Float to String
        )
    }
}
