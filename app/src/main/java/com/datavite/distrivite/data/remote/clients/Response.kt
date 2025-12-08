package com.datavite.distrivite.data.remote.clients

sealed class Response<T>{
    class Authorized<T>(val data:T): Response<T>()
    class UnAuthorized<T>(val errorMsg:String): Response<T>()
    class UnknownError<T>(val errorMsg:String): Response<T>()
}