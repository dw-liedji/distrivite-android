package com.datavite.distrivite.data.remote.model

import com.datavite.distrivite.data.remote.serializers.NoneToNullStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteTeachingSession(
    @SerialName("id") val id: String, // Assuming UUID is represented as a String
    @SerialName("created") val created: String, // ISO 8601 date-time string
    @SerialName("modified") val modified: String, // ISO 8601 date-time string
    @SerialName("org_slug") val orgSlug: String,
    @SerialName("org_user_id") val orgUserId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("scenario") val scenario: String,
    @SerialName("course") val course: String,
    @SerialName("course_id") val courseId: String,
    @SerialName("org_id") val orgId: String,
    @SerialName("room") val room: String,
    @SerialName("room_id") val roomId: String,
    @SerialName("start") val start: String, // ISO 8601 date-time string
    @SerialName("end") val end: String, // ISO 8601 date-time string
    @Serializable(with = NoneToNullStringSerializer::class)
    @SerialName("r_start") val rStart: String?, // Nullable ISO 8601 date-time string
    @Serializable(with = NoneToNullStringSerializer::class)
    @SerialName("r_end") val rEnd: String?, // Nullable ISO 8601 date-time string
    @SerialName("day") val day: String,
    @SerialName("option") val option: String,
    @SerialName("parents_notified")  val parentsNotified: Boolean,
    @SerialName("level") val level: String,
    @SerialName("hourly_remuneration") val hourlyRemuneration: String,
    @SerialName("cursus") val cursus: String,
    @SerialName("klass") val klass: String,
    @SerialName("education_class_id") val educationClassId: String,
    @SerialName("instructor") val instructor: String,
    @SerialName("instructor_id") val instructorId:String,
    @SerialName("instructor_contract_id") val instructorContractId:String,
    @SerialName("teaching_period_id") val teachingPeriodId: String,
    @SerialName("status") val status: String
)

