package com.datavite.distrivite.data.repository.auth

import com.datavite.distrivite.data.local.datasource.auth.LocalJwtDataSource
import com.datavite.distrivite.domain.repository.auth.JwtRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class JwtRepositoryImpl @Inject constructor (private val localJwtDataSource: LocalJwtDataSource) :
    JwtRepository {

    override suspend fun getAccessToken(): Flow<String?> {
        return localJwtDataSource.getAccessToken()
    }

    override suspend fun getRefreshToken(): Flow<String?> {
        return localJwtDataSource.getRefreshToken()
    }

    override suspend fun saveRefreshToken(token: String) {
        localJwtDataSource.saveRefreshToken(token)
    }

    override suspend fun saveAccessToken(token: String) {
        localJwtDataSource.saveAccessToken(token)
    }

    override suspend fun deleteAllTokens() {
        localJwtDataSource.deleteAllTokens()
    }
}