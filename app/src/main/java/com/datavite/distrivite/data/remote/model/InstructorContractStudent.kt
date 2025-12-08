package com.datavite.distrivite.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstructorContractStudent(
    @SerialName("id") val id: String,
    @SerialName("created") val created: String,
    @SerialName("modified") val modified: String,
    @SerialName("org_id") val orgId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("org_user_id") val orgUserId:String,
    @SerialName("monthly_salary") val monthlySalary:Double,
    @SerialName("org_slug") val orgSlug: String,
    @SerialName("name") val name: String,
    @SerialName("department") val department: String,
    @SerialName("is_manager") val isManager: Boolean,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("check_in_latitude") val checkInLatitude:Double,
    @SerialName("check_in_longitude") val checkInLongitude:Double,
    @SerialName("check_out_latitude") val checkOutLatitude:Double,
    @SerialName("check_out_longitude") val checkOutLongitude:Double,
    @SerialName("working_days") val workingDays: List<String>,
    @SerialName("embeddings") val embeddings: List<List<Float>>,
)
