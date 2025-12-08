package com.datavite.distrivite.domain.model

data class DomainOrganizationUser(
    val id: String,
    val created: String,
    val modified: String,
    val name: String,
    val isAdmin: Boolean,
    val isActive: Boolean,
    val orgSlug: String,
    val orgId: String,
    val userId: String,
    val embeddings: List<List<Float>>
)

