package com.datavite.distrivite.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.datavite.distrivite.domain.model.auth.DomainUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

class UserCredentialManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val USER_KEY = stringPreferencesKey("user_key")
    }

    // Expose the organization as a Flow
    val userFlow: Flow<DomainUser?> = dataStore.data.map { preferences ->
        preferences[USER_KEY]?.let { Json.decodeFromString<DomainUser>(it) }
    }

    suspend fun saveUser(domainUser: DomainUser) {
        val userJson = Json.encodeToString(domainUser)
        dataStore.edit { preferences ->
            preferences[USER_KEY] = userJson
        }
    }

    suspend fun deleteUser() {
        dataStore.edit { preferences ->
            preferences.remove(USER_KEY)
        }
    }
}