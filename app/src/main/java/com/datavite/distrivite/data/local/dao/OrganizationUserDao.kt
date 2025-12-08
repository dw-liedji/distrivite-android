package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalOrganizationUser
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizationUserDao {

    @Query("SELECT * FROM organization_users")
    fun getOrganizationUsersFlow(): Flow<List<LocalOrganizationUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOrganizationUser(user: LocalOrganizationUser)

    @Query("DELETE FROM organization_users WHERE id = :id")
    suspend fun deleteOrganizationUser(id: String)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOrganizationUsers(users: List<LocalOrganizationUser>)

    @Update
    suspend fun updateUser(user: LocalOrganizationUser)

    @Query("SELECT * FROM organization_users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): LocalOrganizationUser?

    @Query("SELECT * FROM organization_users WHERE userId = :userId LIMIT 1")
    suspend fun getOrgUserByUserId(userId: String): LocalOrganizationUser?

    @Query("SELECT * FROM organization_users WHERE syncType != :syncType")
    suspend fun getUnSyncedUsers(syncType: SyncType=SyncType.SYNCED): List<LocalOrganizationUser>

    @Query("SELECT * FROM organization_users WHERE syncType = :syncType")
    suspend fun getUsersBySyncType(syncType: SyncType): List<LocalOrganizationUser>

    @Update
    suspend fun markUserAsSynced(user: LocalOrganizationUser)

    @Query("UPDATE organization_users SET syncType = :syncType WHERE id = :id")
    suspend fun markAsPendingDeletion(id: String, syncType: SyncType = SyncType.PENDING_DELETION)

    @Query("UPDATE organization_users SET syncType = :syncType WHERE id = :id")
    suspend fun updateUserSyncType(id: String, syncType: SyncType)
}