package com.example.util

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.data.model.ProfileEntity
import java.lang.reflect.Field

/**
 * Complete Sandbox Environment Startup Injection:
 *
 * Replaces ActivityThread.mInstrumentation to intercept the Android application lifecycle
 * before ANY target Activity or Application code executes.
 *
 * Guarantees:
 *  1. Target Activity context (mBase) is wrapped with VirtualContextWrapper prior to onCreate.
 *  2. Hardware parameters (Build, SystemProperties, Settings.Secure) are locked to the spoofed profile.
 *  3. File system and database storage directories are isolated to /virtual_sandboxes/{profileId}/.
 */
class VirtualInstrumentation(
    private val baseInstrumentation: Instrumentation,
    private val profile: ProfileEntity
) : Instrumentation() {

    override fun newActivity(
        cl: ClassLoader?,
        className: String?,
        intent: Intent?
    ): Activity {
        // Step 1: Pre-execution hook - install environment before class initialization
        VirtualHookManager.injectEnvironment(profile)
        SystemServiceHookManager.installHooks(profile)
        NativePropertyHook.setProfileProperties(profile)

        val activity = baseInstrumentation.newActivity(cl, className, intent)
        Log.i(TAG, "VirtualInstrumentation instantiated activity: $className under profile: ${profile.deviceModel}")
        return activity
    }

    override fun callActivityOnCreate(activity: Activity, icicle: Bundle?) {
        // Step 2: Critical Context Wrapping BEFORE Activity.onCreate()
        try {
            wrapActivityContext(activity, profile)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to wrap Activity baseContext: ${e.message}")
        }

        // Re-affirm process hooks in case of lazy runtime initializations
        VirtualHookManager.injectEnvironment(profile)
        SystemServiceHookManager.installHooks(profile)

        // Step 3: Invoke the real Activity.onCreate() inside the protected sandbox
        baseInstrumentation.callActivityOnCreate(activity, icicle)
    }

    override fun callApplicationOnCreate(app: Application) {
        try {
            wrapApplicationContext(app, profile)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to wrap Application baseContext: ${e.message}")
        }
        VirtualHookManager.injectEnvironment(profile)
        baseInstrumentation.callApplicationOnCreate(app)
    }

    private fun wrapActivityContext(activity: Activity, profile: ProfileEntity) {
        val baseContext = activity.baseContext
        if (baseContext !is VirtualContextWrapper) {
            val wrapped = VirtualContextWrapper(baseContext, profile)
            setBaseContext(activity, wrapped)
            Log.d(TAG, "Activity context wrapped with VirtualContextWrapper for ${activity.componentName}")
        }
    }

    private fun wrapApplicationContext(app: Application, profile: ProfileEntity) {
        val baseContext = app.baseContext
        if (baseContext !is VirtualContextWrapper) {
            val wrapped = VirtualContextWrapper(baseContext, profile)
            setBaseContext(app, wrapped)
            Log.d(TAG, "Application context wrapped with VirtualContextWrapper for ${app.packageName}")
        }
    }

    private fun setBaseContext(target: ContextWrapper, newBase: Context) {
        var currentClass: Class<*>? = target.javaClass
        var mBaseField: Field? = null

        while (currentClass != null && currentClass != Any::class.java) {
            try {
                mBaseField = currentClass.getDeclaredField("mBase")
                break
            } catch (_: NoSuchFieldException) {
                currentClass = currentClass.superclass
            }
        }

        mBaseField?.let {
            it.isAccessible = true
            it.set(target, newBase)
        }
    }

    companion object {
        private const val TAG = "VirtualInstrumentation"
    }
}

/**
 * Controller to install and manage the VirtualInstrumentation hook on ActivityThread.
 */
object VirtualInstrumentationHook {

    private const val TAG = "VirtualInstrumentationHook"
    private var originalInstrumentation: Instrumentation? = null
    private var isInstalled = false

    /**
     * Intercepts ActivityThread.mInstrumentation and replaces it with VirtualInstrumentation.
     */
    @Synchronized
    fun install(profile: ProfileEntity): Boolean {
        try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread")
            val currentActivityThread = currentActivityThreadMethod.invoke(null) ?: return false

            val mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation")
            mInstrumentationField.isAccessible = true

            val currentInstrumentation = mInstrumentationField.get(currentActivityThread) as? Instrumentation
            if (currentInstrumentation !is VirtualInstrumentation) {
                originalInstrumentation = currentInstrumentation ?: Instrumentation()
            }

            val virtualInstrumentation = VirtualInstrumentation(originalInstrumentation ?: Instrumentation(), profile)
            mInstrumentationField.set(currentActivityThread, virtualInstrumentation)

            isInstalled = true
            Log.i(TAG, "Successfully injected VirtualInstrumentation into ActivityThread for ${profile.profileName}")
            return true
        } catch (e: Throwable) {
            Log.w(TAG, "Unable to hook ActivityThread.mInstrumentation: ${e.message}")
            return false
        }
    }

    /**
     * Restores original instrumentation upon exiting sandbox.
     */
    @Synchronized
    fun uninstall() {
        if (!isInstalled) return
        try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread")
            val currentActivityThread = currentActivityThreadMethod.invoke(null) ?: return

            val mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation")
            mInstrumentationField.isAccessible = true

            if (originalInstrumentation != null) {
                mInstrumentationField.set(currentActivityThread, originalInstrumentation)
            }
            isInstalled = false
            Log.i(TAG, "Restored original Instrumentation.")
        } catch (e: Throwable) {
            Log.w(TAG, "Error restoring Instrumentation: ${e.message}")
        }
    }

    fun isHooked(): Boolean = isInstalled
}
