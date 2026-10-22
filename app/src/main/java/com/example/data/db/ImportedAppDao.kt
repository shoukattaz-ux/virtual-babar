package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ImportedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportedAppDao {
    @Query("SELECT * FROM imported_apps ORDER BY imported_at ASC")
    fun getAllImportedApps(): Flow<List<ImportedAppEntity>>

    @Query("SELECT * FROM imported_apps WHERE package_name = :packageName LIMIT 1")
    suspend fun getImportedAppByPackage(packageName: String): ImportedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportedApp(app: ImportedAppEntity): Long

    @Update
    suspend fun updateImportedApp(app: ImportedAppEntity)

    @Delete
    suspend fun deleteImportedApp(app: ImportedAppEntity)

    @Query("DELETE FROM imported_apps WHERE package_name = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    @Query("UPDATE imported_apps SET launch_count = launch_count + 1 WHERE package_name = :packageName")
    suspend fun incrementLaunchCount(packageName: String)

    @Query("UPDATE imported_apps SET assigned_profile_id = :newProfileId, is_custom_identity = :isCustom WHERE id = :id")
    suspend fun updateAssignedProfile(id: Long, newProfileId: Long, isCustom: Boolean)
}
