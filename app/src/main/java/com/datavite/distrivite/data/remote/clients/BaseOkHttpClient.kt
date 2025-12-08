package com.datavite.distrivite.data.remote.clients

import okhttp3.OkHttpClient

interface BaseOkHttpClient {
     fun createOkhttp(): OkHttpClient
}