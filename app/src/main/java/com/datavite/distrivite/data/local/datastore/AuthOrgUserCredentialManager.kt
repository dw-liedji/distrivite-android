package com.datavite.distrivite.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AuthOrgUserCredentialManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param: ApplicationScope private val  scope: CoroutineScope,
) {

    companion object {
        private val AUTHENTICATED_ORGANIZATION_USER_KEY = stringPreferencesKey("auth_organization_user_key")
    }

    // Expose the organization as a Flow

    private val _authOrgUserFlow: Flow<AuthOrgUser?> = dataStore.data.map { preferences ->
        preferences[AUTHENTICATED_ORGANIZATION_USER_KEY]
            ?.let { Json.decodeFromString<AuthOrgUser>(it) }
    }

    // Shared version for everyone
    val sharedAuthOrgUserFlow = _authOrgUserFlow
        .shareIn(scope, SharingStarted.Eagerly, replay = 1)

    suspend fun saveAuthOrgUser(authOrgUser: AuthOrgUser) {
        val organizationJson = Json.encodeToString(authOrgUser)
        dataStore.edit { preferences ->
            preferences[AUTHENTICATED_ORGANIZATION_USER_KEY] = organizationJson
        }
    }

    suspend fun deleteAuthOrgUser() {
        dataStore.edit { preferences ->
            preferences.remove(AUTHENTICATED_ORGANIZATION_USER_KEY)
        }
    }
}
