package com.datavite.distrivite.domain.model

data class DomainLeave(
    val id: String,
    val created: String,
    val modified: String,
    val orgId: String,
    val orgSlug: String,
    val userId: String,
    val orgUserId: String,
    val employeeId: String,
    val employeeName: String,
    val type: String,
    val hourlySalary: Double,
    val startDate: String,
    val endDate: String,
    val status: String,
    val reason: String
)
