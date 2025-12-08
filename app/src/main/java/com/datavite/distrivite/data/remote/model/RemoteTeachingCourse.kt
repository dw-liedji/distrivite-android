package com.datavite.distrivite.feature.cameis.data.remote.model
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class RemoteTeachingCourse(
    @SerialName("id")
    val id: String,

    @SerialName("created")
    val created: String,

    @SerialName("modified")
    val modified: String,

    @SerialName("org_slug")
    val orgSlug: String,

    @SerialName("course")
    val course: String,

    @SerialName("credit")
    val credit: String,

    @SerialName("education_term")
    val educationTerm: String,

    @SerialName("module")
    val module: String,

    @SerialName("code")
    val code: String,

    @SerialName("org_id")
    val orgId: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("org_user_id")
    val orgUserId: String,

    @SerialName("hourly_remuneration") val hourlyRemuneration: String,

    @SerialName("option")
    val option: String,

    @SerialName("level")
    val level: String,

    @SerialName("cursus")
    val cursus: String,

    @SerialName("klass")
    val klass: String,

    @SerialName("instructor")
    val instructor: String,

    @SerialName("instructor_id")
    val instructorId: String,

    @SerialName("duration_in_hours")
    val durationInHours: String,
    @SerialName("education_class_id") val educationClassId: String,
    @SerialName("progression")
    val progression: String
)

