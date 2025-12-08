package com.datavite.distrivite.domain.model

data class DomainEmployee(
    val id: String,
    val created: String,
    val modified: String,
    val name: String,
    val department: String,
    val isManager: Boolean,
    val isActive: Boolean,
    val orgUserId:String,
    val orgSlug: String,
    val orgId: String,
    val monthlySalary:Double,
    val userId: String,
    val checkInLatitude:Double,
    val checkInLongitude:Double,
    val checkOutLatitude:Double,
    val checkOutLongitude:Double,
    val workingDays: List<String>,
    val embeddings: List<List<Float>>
)

