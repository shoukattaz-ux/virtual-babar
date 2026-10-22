package com.example.util

import com.example.data.model.ProfileEntity
import java.util.UUID
import kotlin.random.Random

/**
 * Data class representing the device identity profile model.
 */
data class ProfileModel(
    val profileId: String,
    val profileName: String,
    val androidId: String,
    val deviceBrand: String,
    val deviceModel: String,
    val serialNumber: String,
    val macAddress: String
)

/**
 * Utility object to generate randomized, realistic-looking hardware identifiers
 * for the virtual machine and isolated custom app profiles.
 */
object DeviceProfileGenerator {

    val brands = listOf("Google", "Samsung", "OnePlus", "Xiaomi", "Motorola", "Nothing", "Sony")

    val models = mapOf(
        "Google" to listOf("Pixel 7", "Pixel 6 Pro", "Pixel 8", "Pixel 8 Pro", "Pixel 9"),
        "Samsung" to listOf("Galaxy S23", "Galaxy S22", "Galaxy Z Flip5", "Galaxy S24 Ultra", "Galaxy Z Fold 5"),
        "OnePlus" to listOf("OnePlus 11", "OnePlus 10T", "OnePlus 12"),
        "Xiaomi" to listOf("Xiaomi 13", "Redmi Note 12", "Xiaomi 14 Pro"),
        "Motorola" to listOf("Edge 40", "Edge 50 Ultra", "Razr 40 Ultra"),
        "Nothing" to listOf("Phone (1)", "Phone (2)", "Phone (2a)"),
        "Sony" to listOf("Xperia 1 VI", "Xperia 5 V")
    )

    fun generateRandomProfile(profileName: String): ProfileModel {
        val randomBrand = brands.random()
        val randomModel = models[randomBrand]?.random() ?: "Generic Device"

        return ProfileModel(
            profileId = UUID.randomUUID().toString(),
            profileName = profileName,
            androidId = UUID.randomUUID().toString().replace("-", "").substring(0, 16),
            deviceBrand = randomBrand,
            deviceModel = randomModel,
            serialNumber = "RND" + Random.nextLong(1000000000L, 9999999999L),
            macAddress = generateRandomMac()
        )
    }

    fun generateRandomProfileEntity(
        profileName: String,
        isDefaultVm: Boolean = false,
        profileId: Long = 0
    ): ProfileEntity {
        val model = generateRandomProfile(profileName)
        return ProfileEntity(
            profileId = profileId,
            profileName = profileName,
            androidId = model.androidId,
            deviceBrand = model.deviceBrand,
            deviceModel = model.deviceModel,
            serialNumber = model.serialNumber,
            macAddress = model.macAddress,
            isDefaultVm = isDefaultVm
        )
    }

    fun generateRandomMac(): String {
        val hexChars = "0123456789ABCDEF"
        return (1..6).map {
            "${hexChars[Random.nextInt(16)]}${hexChars[Random.nextInt(16)]}"
        }.joinToString(":")
    }

    fun generateRandomAndroidId(): String {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16)
    }

    fun generateRandomSerialNumber(brand: String = "Generic"): String {
        val prefix = when (brand.lowercase()) {
            "samsung" -> "R5C"
            "google" -> "ZX1"
            "xiaomi" -> "XM"
            "oneplus" -> "OP"
            else -> "RND"
        }
        return prefix + Random.nextLong(1000000000L, 9999999999L)
    }
}

fun ProfileModel.toEntity(isDefaultVm: Boolean = false, profileId: Long = 0): ProfileEntity {
    return ProfileEntity(
        profileId = profileId,
        profileName = this.profileName,
        androidId = this.androidId,
        deviceBrand = this.deviceBrand,
        deviceModel = this.deviceModel,
        serialNumber = this.serialNumber,
        macAddress = this.macAddress,
        isDefaultVm = isDefaultVm
    )
}

fun ProfileEntity.toModel(): ProfileModel {
    return ProfileModel(
        profileId = this.profileId.toString(),
        profileName = this.profileName,
        androidId = this.androidId,
        deviceBrand = this.deviceBrand,
        deviceModel = this.deviceModel,
        serialNumber = this.serialNumber,
        macAddress = this.macAddress
    )
}
