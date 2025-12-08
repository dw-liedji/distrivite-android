package com.datavite.distrivite.domain.repository

import com.datavite.distrivite.data.local.model.LocalClaim
import com.datavite.distrivite.domain.model.DomainClaim
import kotlinx.coroutines.flow.Flow

interface ClaimRepository {

    suspend fun getClaimById(id: String): LocalClaim?
    suspend fun getClaimsFlow(): Flow<List<DomainClaim>>
    suspend fun createClaim(organization: String, claim: DomainClaim)
    suspend fun updateClaim(organization: String, claim: DomainClaim)
    suspend fun deleteClaim(organization: String, claim: DomainClaim)
    suspend fun syncClaims(organization: String)
}