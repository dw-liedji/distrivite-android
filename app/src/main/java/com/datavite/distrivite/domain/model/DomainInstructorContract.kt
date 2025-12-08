package com.datavite.distrivite.domain.model

data class DomainInstructorContract(
    val id: String,
    val created: String,
    val modified: String,
    val name: String,
    val contract: String,
    val isManager: Boolean,
    val isActive: Boolean,
    val instructorId:String,
    val orgUserId:String,
    val orgSlug: String,
    val orgId: String,
    val userId: String,
    val checkInLatitude:Double,
    val checkInLongitude:Double,
    val checkOutLatitude:Double,
    val checkOutLongitude:Double,
    val embeddings: List<List<Float>>
)

