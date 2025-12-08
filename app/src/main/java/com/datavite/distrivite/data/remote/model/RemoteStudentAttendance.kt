package com.datavite.distrivite.data.remote.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class RemoteStudentAttendance(
    @SerialName("id")
    val id: String,
    @SerialName("created")
    val created: String,
    @SerialName("modified")
    val modified: String,
    @SerialName("org_id")
    val orgId: String,
    @SerialName("org_slug")
    val orgSlug: String,
    @SerialName("education_class_id")
    val educationClassId: String,
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("session_name")
    val sessionName: String,
    @SerialName("student_id")
    val studentId: String,
    @SerialName("is_present")
    val isPresent: Boolean,
    @SerialName("student_name")
    val studentName: String,
    @SerialName("register_at")
    val registerAt: String,
)
