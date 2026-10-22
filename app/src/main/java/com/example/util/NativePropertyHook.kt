package com.example.util

import com.example.data.model.ProfileEntity
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory system property overlay for the active container profile.
 *
 * Values set here are only visible to code in this app that reads properties through
 * [VirtualHookManager.getSystemProperty]. Other apps always read the real device properties.
 */
object NativePropertyHook {

    private val memoryPropertyStore = ConcurrentHashMap<String, String>()

    /**
     * Stores the full set of profile system properties for the given ProfileEntity.
     */
    fun setProfileProperties(profile: ProfileEntity) {
        val modelNormalized = profile.deviceModel.replace(" ", "_").lowercase()
        val brandLower = profile.deviceBrand.lowercase()

        val props = mapOf(
            "ro.product.brand" to profile.deviceBrand,
            "ro.product.model" to profile.deviceModel,
            "ro.product.manufacturer" to profile.deviceBrand,
            "ro.product.device" to modelNormalized,
            "ro.product.name" to modelNormalized,
            "ro.product.board" to "${brandLower}_board",
            "ro.hardware" to "${brandLower}qcom",
            "ro.serialno" to profile.serialNumber,
            "ro.boot.serialno" to profile.serialNumber,
            "net.hostname" to "virtuos-${profile.deviceModel.replace(" ", "").lowercase()}",
            "ro.build.id" to "UP1A.231005.007",
            "ro.build.display.id" to "VirtuOS-${profile.deviceModel}-Spoofed",
            "ro.build.fingerprint" to "${profile.deviceBrand}/$modelNormalized/${profile.deviceBrand}:14/UP1A.231005.007/${profile.serialNumber}:user/release-keys",
            "ro.build.version.release" to "14",
            "ro.build.version.sdk" to "34"
        )

        props.forEach { (key, value) ->
            setProperty(key, value)
        }
    }

    /**
     * Sets an individual profile property.
     */
    fun setProperty(key: String, value: String) {
        memoryPropertyStore[key] = value
    }

    /**
     * Reads a profile property, returning [default] if it is not set.
     */
    fun getProperty(key: String, default: String = ""): String =
        memoryPropertyStore[key] ?: default

    /**
     * Clears all profile properties.
     */
    fun clear() {
        memoryPropertyStore.clear()
    }

    /**
     * There is no native hook; kept so diagnostics can report the layer as inactive.
     */
    fun isNativeActive(): Boolean = false

    /**
     * Returns an unmodifiable copy of the current profile properties map.
     */
    fun getActiveProperties(): Map<String, String> = HashMap(memoryPropertyStore)
}
