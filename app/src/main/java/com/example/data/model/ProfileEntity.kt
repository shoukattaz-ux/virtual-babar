package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "profile_id")
    val profileId: Long = 0,

    @ColumnInfo(name = "profile_name")
    val profileName: String,

    @ColumnInfo(name = "android_id")
    val androidId: String,

    @ColumnInfo(name = "device_model")
    val deviceModel: String,

    @ColumnInfo(name = "device_brand")
    val deviceBrand: String,

    @ColumnInfo(name = "mac_address")
    val macAddress: String,

    @ColumnInfo(name = "serial_number")
    val serialNumber: String,

    @ColumnInfo(name = "is_default_vm")
    val isDefaultVm: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
