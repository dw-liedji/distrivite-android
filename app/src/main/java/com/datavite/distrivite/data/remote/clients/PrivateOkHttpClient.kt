package com.datavite.distrivite.data.remote.clients

import com.datavite.distrivite.data.remote.service.auth.RetrofitPublicRefreshService
import com.datavite.distrivite.domain.repository.auth.JwtRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PrivateOkHttpClient@Inject constructor (
    private val jwtRepository: JwtRepository,
    private val retrofitPublicRefreshService: RetrofitPublicRefreshService
) : BaseOkHttpClient {

    // Create an interceptor for adding authentication headers
    private val authenticator = JwtAuthenticator(jwtRepository=jwtRepository, retrofitPublicRefreshService=retrofitPublicRefreshService)
    private val interceptor = JwtInterceptor(jwtRepository=jwtRepository)

    override fun createOkhttp(): OkHttpClient {
        return  OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .authenticator(authenticator)
            .build()
    }
}