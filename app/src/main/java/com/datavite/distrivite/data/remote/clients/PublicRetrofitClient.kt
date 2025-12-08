package com.datavite.distrivite.data.remote.clients

import retrofit2.Retrofit

class PublicRetrofitClient (
    private val publicOkHttpClient: PublicOkHttpClient
) : BaseRetrofitClient() {
    fun getRetrofit(baseUrl: String): Retrofit {
        // Use the base class method to create an OkHttpClient
        return createRetrofit(publicOkHttpClient.createOkhttp(), baseUrl)
    }
}
