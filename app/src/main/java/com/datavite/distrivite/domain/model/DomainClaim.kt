package com.datavite.distrivite.domain.model

data class DomainClaim(
    val id: String,
    val created: String,
    val modified: String,
    val orgId: String,
    val userId: String,
    val orgUserId:String,
    val orgSlug: String,
    val employeeId: String,
    val employeeName: String,
    val type: String,
    val hourlySalary:Double,
    val claimedHours: String,
    val date: String,
    val status: String,
    val reason:String,
)
