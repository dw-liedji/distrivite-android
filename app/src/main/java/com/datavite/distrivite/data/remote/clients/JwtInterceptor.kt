package com.datavite.distrivite.data.remote.clients

import com.datavite.distrivite.domain.repository.auth.JwtRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class JwtInterceptor @Inject constructor (
    private val jwtRepository: JwtRepository,
): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val access = runBlocking {
            jwtRepository.getAccessToken().firstOrNull()
        }

        val newRequest = if (!access.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "JWT $access")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
