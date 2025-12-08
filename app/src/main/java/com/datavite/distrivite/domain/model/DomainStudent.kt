package com.datavite.distrivite.domain.model

data class DomainStudent(
    val id: String,
    val created: String,
    val modified: String,
    val name: String,
    val orgSlug: String,
    val orgId: String,
    val userId: String,
    val hasMailingTracking:Boolean,
    val hasSmsTracking:Boolean,
    val isDelegate:Boolean,
    val isActive:Boolean,
    val educationClass: String,
    val educationClassId: String,
    val embeddings: List<List<Float>>,
)

