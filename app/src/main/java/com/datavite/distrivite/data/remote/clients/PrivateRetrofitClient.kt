package com.datavite.distrivite.data.remote.clients

import retrofit2.Retrofit
import javax.inject.Inject

class PrivateRetrofitClient @Inject constructor (
    private val privateOkHttpClient: PrivateOkHttpClient,
) : BaseRetrofitClient() {
    fun getRetrofit(baseUrl: String): Retrofit {
        return createRetrofit(privateOkHttpClient.createOkhttp(), baseUrl)
    }
}
