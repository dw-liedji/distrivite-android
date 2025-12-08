package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteClaim
import com.datavite.distrivite.data.remote.service.ClaimService
import javax.inject.Inject

class ClaimRemoteDataSourceImpl @Inject constructor(
    private val claimService: ClaimService
) : ClaimRemoteDataSource {
    override suspend fun getClaims(organization:String): List<RemoteClaim> {
        return claimService.getClaims(organization)
    }

    override suspend fun createClaim(organization:String, claim: RemoteClaim) : RemoteClaim {
        return claimService.createClaim(organization, claim)
    }

    override suspend fun updateClaim(organization:String, claim: RemoteClaim) : RemoteClaim {
        return claimService.updateClaim(organization=organization, claim.id, claim)
    }

    override suspend fun deleteClaim(organization:String, claim: RemoteClaim) : RemoteClaim{
        return claimService.deleteClaim(organization, claim.id, claim)
    }
}