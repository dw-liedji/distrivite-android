package com.datavite.distrivite.data.remote.datasource.auth

import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUserRequest
import com.datavite.distrivite.data.remote.model.auth.AuthUser
import com.datavite.distrivite.data.remote.clients.Response
import com.datavite.distrivite.data.remote.service.auth.RetrofitPrivateAuthService
import retrofit2.HttpException
import javax.inject.Inject

class RetrofitUserDataSource @Inject constructor (
    private val retrofitPrivateAuthService: RetrofitPrivateAuthService,
): RemoteUserDataSource {
    override suspend fun getUserProfile(): Response<AuthUser> {
        return try {
            val data = retrofitPrivateAuthService.getUser()
            Response.Authorized(data = data)
        }catch (e: HttpException){
            if (e.code() == 401) {
                e.printStackTrace()
                Response.UnAuthorized(e.message())
            }else {
                e.printStackTrace()
                //Log.i("tiq")
                Response.UnknownError(e.message())
            }
        }catch (e:Exception){
            e.printStackTrace()
            Response.UnknownError(e.message.toString())
        }
    }

    override suspend fun authOrgUser(request: AuthOrgUserRequest): Response<AuthOrgUser> {
        return try {
            val data = retrofitPrivateAuthService.authOrgUser(request)
            Response.Authorized(data = data)
        }catch (e: HttpException){
            if (e.code() == 401) {
                e.printStackTrace()
                Response.UnAuthorized(e.message())
            }else {
                e.printStackTrace()
                Response.UnknownError(e.message())
            }
        }catch (e:Exception){
            e.printStackTrace()
            Response.UnknownError("Unknown Error")
        }
    }
}