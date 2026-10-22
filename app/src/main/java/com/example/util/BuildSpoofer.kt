package com.example.util

import android.os.Build
import java.lang.reflect.Field
import java.lang.reflect.Modifier

data class SpoofedEnvironmentState(
    val brand: String,
    val model: String,
    val serial: String,
    val androidId: String,
    val macAddress: String,
    val reflectionSuccess: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Runtime Build Property Mocking (Reflection Pattern):
 * Modifies fields in android.os.Build for the virtual machine execution scope,
 * and maintains active spoofed environment state.
 */
object BuildSpoofer {

    private var activeState: SpoofedEnvironmentState? = null
    private val originalBrand: String = Build.BRAND
    private val originalModel: String = Build.MODEL

    /**
     * Attempts to spoof Build properties via Java Reflection.
     */
    fun spoofBuildProperties(
        brand: String,
        model: String,
        serial: String,
        androidId: String = "",
        macAddress: String = ""
    ): Boolean {
        var success = true
        try {
            setField("BRAND", brand)
            setField("MODEL", model)
            setField("SERIAL", serial)
            setField("MANUFACTURER", brand)
            setField("DEVICE", model.replace(" ", "_").lowercase())
            setField("PRODUCT", model.replace(" ", "_").lowercase())
        } catch (e: Exception) {
            success = false
            e.printStackTrace()
        }

        activeState = SpoofedEnvironmentState(
            brand = brand,
            model = model,
            serial = serial,
            androidId = androidId,
            macAddress = macAddress,
            reflectionSuccess = success
        )

        return success
    }

    private fun setField(fieldName: String, value: String) {
        try {
            val field: Field = Build::class.java.getField(fieldName)
            field.isAccessible = true

            // Handle static final modifier if present on certain Android runtimes
            try {
                val modifiersField = Field::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
            } catch (_: Exception) {
                // Modifiers reflection may be restricted on Android 9+, continue with field.set
            }

            field.set(null, value)
        } catch (e: Exception) {
            // In Android 9+ hidden API restrictions, setting Build fields via reflection
            // can throw NoSuchFieldException or IllegalAccessException.
            // We catch it gracefully while keeping our active in-memory spoofed state intact.
        }
    }

    fun getActiveState(): SpoofedEnvironmentState? = activeState

    fun isSpoofingActive(): Boolean = activeState != null

    fun resetBuildProperties() {
        try {
            setField("BRAND", originalBrand)
            setField("MODEL", originalModel)
        } catch (_: Exception) {}
        activeState = null
    }
}
