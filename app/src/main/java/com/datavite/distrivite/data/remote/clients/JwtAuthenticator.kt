package com.datavite.distrivite.data.remote.clients

import com.datavite.distrivite.data.remote.model.auth.AuthRefreshRequest
import com.datavite.distrivite.data.remote.model.auth.AuthRefreshResponse
import com.datavite.distrivite.data.remote.service.auth.RetrofitPublicRefreshService
import com.datavite.distrivite.domain.repository.auth.JwtRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject


class JwtAuthenticator @Inject constructor (
    private val jwtRepository: JwtRepository,
    private val retrofitPublicRefreshService: RetrofitPublicRefreshService
): Authenticator {


    private suspend fun refreshToken(refresh: String): AuthRefreshResponse {
        return retrofitPublicRefreshService.refresh(request = AuthRefreshRequest(refresh="JWT $refresh"))
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            try {
                val refresh = jwtRepository.getRefreshToken().firstOrNull()
                val access = refresh?.let { refreshToken(refresh) }?.access

                if (!refresh.isNullOrEmpty() && !access.isNullOrEmpty()){

                    access.let { jwtRepository.saveAccessToken(it) }

                    response.request.newBuilder()
                        .header("Authorization", "JWT $access")
                        .build()
                }else {
                    //Couldn't refresh the token, so restart the login process
                    jwtRepository.deleteAllTokens()
                    null
                }
            }catch (e:Exception){
                e.printStackTrace()
                null
            }
        }
    }

}