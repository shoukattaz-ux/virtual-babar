package com.example.util

import com.example.data.model.ProfileEntity
import java.security.SecureRandom

object IdentityRandomizer {

    private val random = SecureRandom()

    data class DeviceSpec(val brand: String, val model: String)

    private val DEVICE_PRESETS = listOf(
        DeviceSpec("Google", "Pixel 8 Pro"),
        DeviceSpec("Google", "Pixel 9"),
        DeviceSpec("Samsung", "Galaxy S24 Ultra"),
        DeviceSpec("Samsung", "Galaxy Z Fold 5"),
        DeviceSpec("Xiaomi", "14 Pro"),
        DeviceSpec("OnePlus", "12"),
        DeviceSpec("Nothing", "Phone (2)"),
        DeviceSpec("Sony", "Xperia 1 VI"),
        DeviceSpec("Motorola", "Edge 50 Ultra")
    )

    fun generateRandomAndroidId(): String {
        val hexChars = "0123456789abcdef"
        val sb = StringBuilder(16)
        for (i in 0 until 16) {
            sb.append(hexChars[random.nextInt(hexChars.length)])
        }
        return sb.toString()
    }

    fun generateRandomMacAddress(): String {
        val sb = StringBuilder(17)
        for (i in 0 until 6) {
            val byteVal = random.nextInt(256)
            sb.append(String.format("%02X", byteVal))
            if (i < 5) sb.append(":")
        }
        return sb.toString()
    }

    fun generateRandomSerialNumber(brand: String): String {
        val prefix = when (brand.lowercase()) {
            "samsung" -> "R5C"
            "google" -> "ZX1"
            "xiaomi" -> "XM"
            "oneplus" -> "OP"
            else -> "VM"
        }
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val sb = StringBuilder(prefix)
        for (i in 0 until 8) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        return sb.toString()
    }

    fun getRandomDevice(): DeviceSpec {
        return DEVICE_PRESETS[random.nextInt(DEVICE_PRESETS.size)]
    }

    fun createCustomProfileForApp(appName: String, customSpec: DeviceSpec? = null): ProfileEntity {
        val spec = customSpec ?: getRandomDevice()
        return ProfileEntity(
            profileName = "$appName Custom Profile",
            androidId = generateRandomAndroidId(),
            deviceModel = spec.model,
            deviceBrand = spec.brand,
            macAddress = generateRandomMacAddress(),
            serialNumber = generateRandomSerialNumber(spec.brand),
            isDefaultVm = false
        )
    }

    fun createFreshRandomizedProfile(profileName: String): ProfileEntity {
        val spec = getRandomDevice()
        return ProfileEntity(
            profileName = profileName,
            androidId = generateRandomAndroidId(),
            deviceModel = spec.model,
            deviceBrand = spec.brand,
            macAddress = generateRandomMacAddress(),
            serialNumber = generateRandomSerialNumber(spec.brand),
            isDefaultVm = false
        )
    }
}
