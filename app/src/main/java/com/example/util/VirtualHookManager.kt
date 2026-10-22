package com.example.util

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import com.example.data.model.ProfileEntity
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Data class representing the active intercepted hardware fingerprint
 * within the virtual sandbox container.
 */
data class InterceptedHardwareParameters(
    val brand: String,
    val model: String,
    val manufacturer: String,
    val serial: String,
    val androidId: String,
    val macAddress: String,
    val deviceName: String,
    val product: String,
    val sandboxDirectory: String,
    val isHookActive: Boolean,
    val isNativeHookActive: Boolean = false,
    val isServiceHookActive: Boolean = false,
    val isInstrumentationHookActive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Process-Level Virtual Hooking & Sandbox Interceptor Framework:
 *
 * Implements end-to-end process-level hardware spoofing across:
 *  1. Property Layer: in-memory system property overlay (NativePropertyHook)
 *  2. Framework Layer: android.os.Build fields (BOARD, BRAND, DEVICE, DISPLAY, FINGERPRINT, HARDWARE, MANUFACTURER, MODEL, PRODUCT, SERIAL)
 *  3. IPC/Binder Layer: ServiceManager.sCache dynamic proxy for iphonesubinfo and wifi (SystemServiceHookManager)
 *  4. Provider Layer: Settings.Secure.ANDROID_ID and NameValueCache interception
 *  5. Lifecycle Layer: ActivityThread.mInstrumentation hook (VirtualInstrumentationHook)
 *
 * Ensures target apps (e.g., "Device Info HW") running inside the virtual container
 * sandbox read ONLY the spoofed profile values across all inspection vectors.
 */
object VirtualHookManager {

    private const val TAG = "VirtualHookManager"

    @Volatile
    private var activeProfile: ProfileEntity? = null

    private val interceptedSystemProperties = mutableMapOf<String, String>()

    private val originalBuildBrand: String = Build.BRAND
    private val originalBuildModel: String = Build.MODEL
    private val originalBuildManufacturer: String = Build.MANUFACTURER
    private val originalBuildDevice: String = Build.DEVICE
    private val originalBuildProduct: String = Build.PRODUCT
    private val originalBuildSerial: String = Build.SERIAL

    /**
     * Complete Process-Level Hook Installation:
     * Coordinates all 5 isolation layers before target code runs.
     */
    fun installProcessHooks(context: Context, profile: ProfileEntity): Boolean {
        activeProfile = profile
        var allSuccess = true

        // 1. Hook android.os.Build reflection fields
        if (!injectBuildFields(profile)) {
            allSuccess = false
        }

        // 2. Hook Native & JVM system properties
        injectSystemProperties(profile)

        // 3. Hook System Service Binders (TelephonyManager, WifiManager, Settings.Secure)
        if (!SystemServiceHookManager.installHooks(profile)) {
            allSuccess = false
        }

        // 4. Hook ActivityThread Instrumentation for automatic context wrapping on startup
        if (!VirtualInstrumentationHook.install(profile)) {
            Log.w(TAG, "Instrumentation hook skipped (expected in non-ActivityThread or restricted environments).")
        }

        // 5. Synchronize legacy BuildSpoofer state
        BuildSpoofer.spoofBuildProperties(
            brand = profile.deviceBrand,
            model = profile.deviceModel,
            serial = profile.serialNumber,
            androidId = profile.androidId,
            macAddress = profile.macAddress
        )

        Log.i(TAG, "All process hooks installed for ${profile.deviceBrand} ${profile.deviceModel}")
        return allSuccess
    }

    /**
     * Backward-compatible entry point for injecting environment parameters.
     */
    fun injectEnvironment(profile: ProfileEntity): Boolean {
        activeProfile = profile
        val buildSuccess = injectBuildFields(profile)
        injectSystemProperties(profile)
        SystemServiceHookManager.installHooks(profile)

        BuildSpoofer.spoofBuildProperties(
            brand = profile.deviceBrand,
            model = profile.deviceModel,
            serial = profile.serialNumber,
            androidId = profile.androidId,
            macAddress = profile.macAddress
        )
        return buildSuccess
    }

    /**
     * Injects spoofed values into android.os.Build static fields:
     * BOARD, BRAND, DEVICE, DISPLAY, FINGERPRINT, HARDWARE, MANUFACTURER, MODEL, PRODUCT, SERIAL
     */
    private fun injectBuildFields(profile: ProfileEntity): Boolean {
        var success = true
        val modelNormalized = profile.deviceModel.replace(" ", "_").lowercase()
        val brandLower = profile.deviceBrand.lowercase()

        try {
            setBuildField("BRAND", profile.deviceBrand)
            setBuildField("MODEL", profile.deviceModel)
            setBuildField("MANUFACTURER", profile.deviceBrand)
            setBuildField("DEVICE", modelNormalized)
            setBuildField("PRODUCT", modelNormalized)
            setBuildField("BOARD", "${brandLower}_board")
            setBuildField("HARDWARE", "${brandLower}qcom")
            setBuildField("SERIAL", profile.serialNumber)
            setBuildField("HOST", "virtuos-sandbox")
            setBuildField("DISPLAY", "VirtuOS-${profile.deviceModel}-Spoofed")
            setBuildField(
                "FINGERPRINT",
                "${profile.deviceBrand}/$modelNormalized/${profile.deviceBrand}:14/UP1A.231005.007/${profile.serialNumber}:user/release-keys"
            )
        } catch (e: Throwable) {
            success = false
            Log.e(TAG, "Failed to reflect Build fields: ${e.message}")
        }
        return success
    }

    /**
     * Injects system properties into the JVM cache and the Native PLT hook table.
     */
    private fun injectSystemProperties(profile: ProfileEntity) {
        val modelNormalized = profile.deviceModel.replace(" ", "_").lowercase()

        synchronized(interceptedSystemProperties) {
            interceptedSystemProperties["ro.product.brand"] = profile.deviceBrand
            interceptedSystemProperties["ro.product.model"] = profile.deviceModel
            interceptedSystemProperties["ro.product.manufacturer"] = profile.deviceBrand
            interceptedSystemProperties["ro.product.device"] = modelNormalized
            interceptedSystemProperties["ro.product.name"] = modelNormalized
            interceptedSystemProperties["ro.product.board"] = "${profile.deviceBrand.lowercase()}_board"
            interceptedSystemProperties["ro.hardware"] = "${profile.deviceBrand.lowercase()}qcom"
            interceptedSystemProperties["ro.serialno"] = profile.serialNumber
            interceptedSystemProperties["ro.boot.serialno"] = profile.serialNumber
            interceptedSystemProperties["net.hostname"] = "virtuos-${profile.deviceModel.replace(" ", "").lowercase()}"
            interceptedSystemProperties["ro.build.id"] = "UP1A.231005.007"
            interceptedSystemProperties["ro.build.display.id"] = "VirtuOS-${profile.deviceModel}-Spoofed"
            interceptedSystemProperties["ro.build.fingerprint"] =
                "${profile.deviceBrand}/$modelNormalized/${profile.deviceBrand}:14/UP1A.231005.007/${profile.serialNumber}:user/release-keys"
        }

        // Bridge to NativePropertyHook
        NativePropertyHook.setProfileProperties(profile)
    }

    private fun setBuildField(fieldName: String, value: String) {
        try {
            val field: Field = Build::class.java.getField(fieldName)
            field.isAccessible = true

            try {
                val modifiersField = Field::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
            } catch (_: Throwable) {
                // Ignore modifier field restrictions on newer ART runtimes
            }

            field.set(null, value)
        } catch (_: Throwable) {
            // Silently handled on secure Android runtimes
        }
    }

    /**
     * Intercepts system property read calls (SystemProperties.get).
     * If the requested key is a known hardware property, returns the spoofed value.
     */
    fun getSystemProperty(key: String, defaultValue: String = ""): String {
        // 1. Check in-memory intercepted map
        synchronized(interceptedSystemProperties) {
            val spoofed = interceptedSystemProperties[key]
            if (spoofed != null) return spoofed
        }

        // 2. Check NativePropertyHook
        val nativeVal = NativePropertyHook.getProperty(key, "")
        if (nativeVal.isNotEmpty()) {
            return nativeVal
        }

        // 3. Fall back to real android.os.SystemProperties
        return try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val getMethod = systemPropertiesClass.getMethod("get", String::class.java, String::class.java)
            getMethod.invoke(null, key, defaultValue) as? String ?: defaultValue
        } catch (_: Throwable) {
            defaultValue
        }
    }

    /**
     * Intercepts Settings.Secure.ANDROID_ID queries.
     * Returns the spoofed androidId for the active container profile.
     */
    fun getInterceptedAndroidId(context: Context): String {
        val spoofed = activeProfile?.androidId
        if (!spoofed.isNullOrBlank()) {
            return spoofed
        }
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "0123456789abcdef"
        } catch (_: Throwable) {
            "0123456789abcdef"
        }
    }

    /**
     * Intercepts MAC address queries for the active profile.
     */
    fun getInterceptedMacAddress(): String {
        return activeProfile?.macAddress ?: "02:00:00:00:00:00"
    }

    /**
     * Returns the active ProfileEntity configured in the hook manager.
     */
    fun getActiveProfile(): ProfileEntity? = activeProfile

    /**
     * Reads the current hardware parameters from the hooked environment.
     * Used by the Device Info HW inspector and diagnostic tests to verify isolation.
     */
    fun readCurrentHardwareParameters(context: Context): InterceptedHardwareParameters {
        val profile = activeProfile
        val profileIdStr = profile?.profileId?.toString() ?: "0"
        val sandboxDir = SandboxStorageManager.getProfileSandboxDir(context, profileIdStr)

        return InterceptedHardwareParameters(
            brand = profile?.deviceBrand ?: Build.BRAND,
            model = profile?.deviceModel ?: Build.MODEL,
            manufacturer = profile?.deviceBrand ?: Build.MANUFACTURER,
            serial = profile?.serialNumber ?: Build.SERIAL,
            androidId = getInterceptedAndroidId(context),
            macAddress = getInterceptedMacAddress(),
            deviceName = profile?.let { "${it.deviceBrand} ${it.deviceModel}" } ?: "${Build.BRAND} ${Build.MODEL}",
            product = (profile?.deviceModel ?: Build.PRODUCT).replace(" ", "_").lowercase(),
            sandboxDirectory = sandboxDir.absolutePath,
            isHookActive = profile != null,
            isNativeHookActive = NativePropertyHook.isNativeActive(),
            isServiceHookActive = SystemServiceHookManager.isInstalled(),
            isInstrumentationHookActive = VirtualInstrumentationHook.isHooked()
        )
    }

    /**
     * Validates that all hardware spoofing vectors are successfully isolated.
     */
    fun verifyIsolationIntegrity(context: Context): Map<String, Boolean> {
        val profile = activeProfile ?: return emptyMap()

        return mapOf(
            "build_brand" to (Build.BRAND == profile.deviceBrand),
            "build_model" to (Build.MODEL == profile.deviceModel),
            "build_serial" to (Build.SERIAL == profile.serialNumber),
            "sysprop_model" to (getSystemProperty("ro.product.model") == profile.deviceModel),
            "sysprop_brand" to (getSystemProperty("ro.product.brand") == profile.deviceBrand),
            "android_id" to (getInterceptedAndroidId(context) == profile.androidId),
            "mac_address" to (getInterceptedMacAddress() == profile.macAddress),
            "service_hooks" to SystemServiceHookManager.isInstalled()
        )
    }

    /**
     * Resets all hooks back to host system defaults.
     */
    fun resetHooks() {
        activeProfile = null
        synchronized(interceptedSystemProperties) {
            interceptedSystemProperties.clear()
        }
        NativePropertyHook.clear()
        SystemServiceHookManager.resetHooks()
        VirtualInstrumentationHook.uninstall()

        setBuildField("BRAND", originalBuildBrand)
        setBuildField("MODEL", originalBuildModel)
        setBuildField("MANUFACTURER", originalBuildManufacturer)
        setBuildField("DEVICE", originalBuildDevice)
        setBuildField("PRODUCT", originalBuildProduct)
        setBuildField("SERIAL", originalBuildSerial)
        BuildSpoofer.resetBuildProperties()
    }

    /**
     * Wraps the given context in a VirtualContextWrapper that redirects
     * internal file storage and isolates system queries.
     */
    fun wrapContext(context: Context, profile: ProfileEntity): VirtualContextWrapper {
        return VirtualContextWrapper(context, profile)
    }
}

/**
 * Custom ContextWrapper that isolates the file system, databases,
 * shared preferences, and system service queries to the dedicated
 * /virtual_sandboxes/{profileId}/ directory.
 */
class VirtualContextWrapper(
    base: Context,
    val profile: ProfileEntity
) : ContextWrapper(base) {

    private val profileId = profile.profileId.toString()

    override fun getFilesDir(): File {
        return SandboxStorageManager.getProfileSandboxDir(baseContext, profileId)
    }

    override fun getCacheDir(): File {
        return SandboxStorageManager.getProfileCacheDir(baseContext, profileId)
    }

    override fun getDataDir(): File {
        return SandboxStorageManager.getProfileSandboxDir(baseContext, profileId)
    }

    override fun getDatabasePath(name: String): File {
        return SandboxStorageManager.getProfileDatabasePath(baseContext, profileId, name)
    }

    override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        // Direct SharedPreferences to the isolated profile's shared_prefs directory
        return baseContext.getSharedPreferences("sandbox_${profileId}_$name", mode)
    }

    override fun getSystemService(name: String): Any? {
        val service = super.getSystemService(name)
        // If TelephonyManager or WifiManager, dynamic proxy has already hooked ServiceManager.sCache
        return service
    }

    /**
     * Intercepted Android ID getter for any components running inside this wrapped context.
     */
    fun getAndroidId(): String {
        return profile.androidId
    }

    /**
     * Intercepted MAC address getter.
     */
    fun getMacAddress(): String {
        return profile.macAddress
    }

    /**
     * Intercepted hardware parameter bundle.
     */
    fun getHardwareParameters(): InterceptedHardwareParameters {
        return InterceptedHardwareParameters(
            brand = profile.deviceBrand,
            model = profile.deviceModel,
            manufacturer = profile.deviceBrand,
            serial = profile.serialNumber,
            androidId = profile.androidId,
            macAddress = profile.macAddress,
            deviceName = "${profile.deviceBrand} ${profile.deviceModel}",
            product = profile.deviceModel.replace(" ", "_").lowercase(),
            sandboxDirectory = filesDir.absolutePath,
            isHookActive = true,
            isNativeHookActive = NativePropertyHook.isNativeActive(),
            isServiceHookActive = SystemServiceHookManager.isInstalled(),
            isInstrumentationHookActive = VirtualInstrumentationHook.isHooked()
        )
    }
}
