package com.datavite.distrivite.di
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.datavite.distrivite.BuildConfig
import com.datavite.distrivite.data.local.datasource.PreferencesJwtDataSource
import com.datavite.distrivite.data.local.datasource.auth.LocalJwtDataSource
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.location.LocationManager
import com.datavite.distrivite.data.mapper.auth.UserMapper
import com.datavite.distrivite.data.remote.clients.PrivateOkHttpClient
import com.datavite.distrivite.data.remote.clients.PrivateRetrofitClient
import com.datavite.distrivite.data.remote.clients.PublicOkHttpClient
import com.datavite.distrivite.data.remote.clients.PublicRetrofitClient
import com.datavite.distrivite.data.remote.datasource.auth.RemoteAuthDataSource
import com.datavite.distrivite.data.remote.datasource.auth.RemoteUserDataSource
import com.datavite.distrivite.data.remote.datasource.auth.RetrofitAuthDataSource
import com.datavite.distrivite.data.remote.datasource.auth.RetrofitUserDataSource
import com.datavite.distrivite.data.remote.service.auth.RetrofitPrivateAuthService
import com.datavite.distrivite.data.remote.service.auth.RetrofitPublicAuthService
import com.datavite.distrivite.data.remote.service.auth.RetrofitPublicRefreshService
import com.datavite.distrivite.data.repository.auth.AuthRepositoryImpl
import com.datavite.distrivite.data.repository.auth.JwtRepositoryImpl
import com.datavite.distrivite.data.repository.auth.UserRepositoryImpl
import com.datavite.distrivite.domain.repository.auth.AuthRepository
import com.datavite.distrivite.domain.repository.auth.JwtRepository
import com.datavite.distrivite.domain.repository.auth.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    private const val USER_PREFERENCES = "user_preferences"

    @ApplicationScope
    @Singleton
    @Provides
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideGpsLocationManager(
        @ApplicationContext context: Context
    ): LocationManager {
        return LocationManager(context)
    }

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(produceNewData = { emptyPreferences() }),
            migrations = listOf(SharedPreferencesMigration(appContext, USER_PREFERENCES)),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { appContext.preferencesDataStoreFile(USER_PREFERENCES) }
        )
    }

    @Provides
    @Singleton
    fun provideAuthOrganizationUserCredential(dataStore: DataStore<Preferences>, @ApplicationScope scope: CoroutineScope) : AuthOrgUserCredentialManager {
        return AuthOrgUserCredentialManager(dataStore, scope = scope)
    }

    @Provides
    @Singleton
    fun provideLocalJwtDataSource(dataStore: DataStore<Preferences>): LocalJwtDataSource {
        return PreferencesJwtDataSource(dataStore)
    }

    @Provides
    @Singleton
    fun provideJwtRepository(localJwtDataSource: LocalJwtDataSource): JwtRepository {
        return JwtRepositoryImpl(localJwtDataSource = localJwtDataSource)
    }


    @Provides
    @Singleton
    fun providePublicOkHttpClientService(): PublicOkHttpClient {
        return PublicOkHttpClient()
    }

    @Provides
    @Singleton
    fun providePrivateOkHttpClientService(jwtRepository: JwtRepository, retrofitPublicRefreshService: RetrofitPublicRefreshService): PrivateOkHttpClient {
        return PrivateOkHttpClient(jwtRepository, retrofitPublicRefreshService)
    }

    @Provides
    @Singleton
    fun providePublicRetrofitClientService(okHttpClient: PublicOkHttpClient): PublicRetrofitClient {
        return PublicRetrofitClient(okHttpClient)
    }

    @Provides
    @Singleton
    fun providePrivateRetrofitClientService(okHttpClient: PrivateOkHttpClient): PrivateRetrofitClient {
        return PrivateRetrofitClient(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideRetrofitPrivateAuthService(
        privateRetrofitClient: PrivateRetrofitClient
    ): RetrofitPrivateAuthService {
        return privateRetrofitClient.getRetrofit(BuildConfig.BASE_URL).create(RetrofitPrivateAuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideRetrofitPublicAuthService(
        publicRetrofitClient: PublicRetrofitClient
    ): RetrofitPublicAuthService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL).create(RetrofitPublicAuthService::class.java)
    }


    @Provides
    @Singleton
    fun provideRetrofitPublicRefreshService(
        publicRetrofitClient: PublicRetrofitClient
    ): RetrofitPublicRefreshService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL).create(
            RetrofitPublicRefreshService::class.java)
    }

    @Provides
    @Singleton
    fun provideRemoteAuthDataSource(authRetrofitService: RetrofitPublicAuthService): RemoteAuthDataSource {
        return RetrofitAuthDataSource(authRetrofitService = authRetrofitService)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(remoteAuthDataSource: RemoteAuthDataSource): AuthRepository {
        return AuthRepositoryImpl(remoteAuthDataSource = remoteAuthDataSource)
    }


    @Provides
    @Singleton
    fun provideRemoteUserDataSource(
        retrofitPrivateAuthService: RetrofitPrivateAuthService,
        ): RemoteUserDataSource {
        return RetrofitUserDataSource(
            retrofitPrivateAuthService=retrofitPrivateAuthService,
        )
    }

    @Provides
    @Singleton
    fun provideUserRepository(remoteUserDataSource: RemoteUserDataSource): UserRepository {
        return UserRepositoryImpl(remoteUserDataSource = remoteUserDataSource)
    }

    @Singleton
    @Provides
    fun provideUserMapper(): UserMapper {
        return UserMapper()
    }
}