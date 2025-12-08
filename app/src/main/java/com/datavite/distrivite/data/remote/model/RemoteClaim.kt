package com.datavite.distrivite.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteClaim(
    @SerialName("id") val id: String,
    @SerialName("created") val created: String,
    @SerialName("modified") val modified: String,
    @SerialName("org_id") val orgId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("org_user_id") val orgUserId: String,
    @SerialName("org_slug") val orgSlug: String,
    @SerialName("employee_id") val employeeId: String,
    @SerialName("employee_name") val employeeName: String,
    @SerialName("type") val type: String,
    @SerialName("hourly_salary") val hourlySalary:Double,
    @SerialName("claimed_hours") val claimedHours: String,
    @SerialName("date") val date: String,
    @SerialName("status") val status: String,
    @SerialName("reason") val reason: String,
)
