package com.datavite.distrivite.data.remote.clients

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class PublicOkHttpClient (
) : BaseOkHttpClient {
    override fun createOkhttp(): OkHttpClient {
        return  OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)   // Set read timeout
            .addInterceptor(HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
            .build()
    }
}