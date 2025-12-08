package com.datavite.distrivite.domain.model


data class DomainTeachingCourse(
    val id: String,
    val created: String,
    val modified: String,
    val orgSlug: String,
    val course: String,
    val credit: Int,
    val educationTerm: String,
    val hourlyRemuneration: String,
    val educationClassId: String,
    val module: String,
    val code: String,
    val orgId: String,
    val userId: String,
    val orgUserId: String,
    val option: String,
    val level: String,
    val cursus: String,
    val klass: String,
    val instructor: String,
    val instructorId: String,
    val durationInHours: Float,
    val progression: Float
)
