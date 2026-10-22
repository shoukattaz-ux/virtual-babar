package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "imported_apps",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["profile_id"],
            childColumns = ["assigned_profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["package_name"], unique = true),
        Index(value = ["assigned_profile_id"])
    ]
)
data class ImportedAppEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "app_name")
    val appName: String,

    @ColumnInfo(name = "assigned_profile_id")
    val assignedProfileId: Long,

    @ColumnInfo(name = "is_custom_identity")
    val isCustomIdentity: Boolean = false,

    @ColumnInfo(name = "imported_at")
    val importedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "launch_count")
    val launchCount: Int = 0,

    @ColumnInfo(name = "is_simulated")
    val isSimulated: Boolean = false
)
