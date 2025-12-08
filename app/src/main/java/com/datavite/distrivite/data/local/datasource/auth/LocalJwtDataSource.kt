package com.datavite.distrivite.data.local.datasource.auth

import kotlinx.coroutines.flow.Flow

interface LocalJwtDataSource {
    suspend fun getAccessToken(): Flow<String?>
    suspend fun getRefreshToken(): Flow<String?>
    suspend fun saveRefreshToken(token: String)
    suspend fun saveAccessToken(token: String)
    suspend fun deleteAllTokens()
}