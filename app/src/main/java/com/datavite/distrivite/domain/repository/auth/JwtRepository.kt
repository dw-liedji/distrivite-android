package com.datavite.distrivite.domain.repository.auth

import kotlinx.coroutines.flow.Flow

interface JwtRepository {
    suspend fun getAccessToken(): Flow<String?>
    suspend fun getRefreshToken(): Flow<String?>
    suspend fun saveRefreshToken(token: String)
    suspend fun saveAccessToken(token: String)
    suspend fun deleteAllTokens()
}