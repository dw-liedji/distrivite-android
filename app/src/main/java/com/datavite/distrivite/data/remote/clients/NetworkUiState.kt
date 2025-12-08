package com.datavite.distrivite.data.remote.clients

sealed class NetworkUiState<T>{
    class Success<T>(val data:T): NetworkUiState<T>()
    class Loading<T>: NetworkUiState<T>()
    class Empty<T>(val errorMsg:String): NetworkUiState<T>()
    class Error<T>(val errorMsg:String): NetworkUiState<T>()
}