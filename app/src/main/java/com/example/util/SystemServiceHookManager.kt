package com.example.util

import android.content.Context
import android.net.wifi.WifiInfo
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.provider.Settings
import android.util.Log
import com.example.data.model.ProfileEntity
import java.lang.reflect.Field
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * System Service & Provider Interceptor Framework:
 *
 * Implements process-level dynamic proxy interception on Android's ServiceManager
 * cache (sCache) and Settings.Secure provider caches.
 *
 * Intercepts:
 *  1. "iphonesubinfo" / TelephonyManager: IMEI, DeviceId, Serial, IMSI
 *  2. "wifi" / WifiManager: WifiInfo, MAC Address
 *  3. Settings.Secure: ANDROID_ID via NameValueCache and Provider injection
 */
object SystemServiceHookManager {

    private const val TAG = "SystemServiceHook"

    @Volatile
    private var activeProfile: ProfileEntity? = null

    private var isHookInstalled = false

    /**
     * Installs dynamic binder proxies into ServiceManager.sCache and hooks Settings caches.
     */
    @Synchronized
    fun installHooks(profile: ProfileEntity): Boolean {
        activeProfile = profile
        var allSuccess = true

        try {
            hookServiceManagerCache("iphonesubinfo", createPhoneSubInfoProxy(profile))
        } catch (e: Throwable) {
            Log.w(TAG, "Failed hooking iphonesubinfo: ${e.message}")
            allSuccess = false
        }

        try {
            hookServiceManagerCache("wifi", createWifiManagerProxy(profile))
        } catch (e: Throwable) {
            Log.w(TAG, "Failed hooking wifi manager: ${e.message}")
            allSuccess = false
        }

        try {
            hookSettingsSecureCache(profile.androidId)
        } catch (e: Throwable) {
            Log.w(TAG, "Failed hooking Settings.Secure cache: ${e.message}")
            allSuccess = false
        }

        isHookInstalled = true
        Log.i(TAG, "System service hooks installed for profile: ${profile.profileName}")
        return allSuccess
    }

    /**
     * Intercepts ServiceManager.sCache by replacing the registered IBinder with a dynamic proxy.
     */
    @Suppress("UNCHECKED_CAST")
    private fun hookServiceManagerCache(serviceName: String, interfaceProxy: Any) {
        try {
            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val sCacheField: Field = serviceManagerClass.getDeclaredField("sCache")
            sCacheField.isAccessible = true
            val sCache = sCacheField.get(null) as? MutableMap<String, IBinder> ?: return

            // Query original binder or get fallback binder
            val getServiceMethod = serviceManagerClass.getMethod("getService", String::class.java)
            val originalBinder = getServiceMethod.invoke(null, serviceName) as? IBinder

            val proxyBinder = Proxy.newProxyInstance(
                IBinder::class.java.classLoader,
                arrayOf(IBinder::class.java),
                BinderInvocationHandler(originalBinder, interfaceProxy)
            ) as IBinder

            sCache[serviceName] = proxyBinder
            Log.d(TAG, "ServiceManager sCache hooked for service: $serviceName")
        } catch (t: Throwable) {
            Log.w(TAG, "Unable to hook ServiceManager cache for $serviceName: ${t.message}")
        }
    }

    /**
     * Dynamic proxy invocation handler for IBinder tokens.
     * When queryLocalInterface(...) or asInterface(...) is called,
     * returns our spoofed interface proxy.
     */
    private class BinderInvocationHandler(
        private val originalBinder: IBinder?,
        private val customInterfaceProxy: Any
    ) : InvocationHandler {

        override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
            if (method.name == "queryLocalInterface") {
                return customInterfaceProxy
            }
            if (method.name == "getInterfaceDescriptor") {
                return originalBinder?.interfaceDescriptor ?: ""
            }
            if (method.name == "pingBinder") {
                return true
            }
            if (method.name == "isBinderAlive") {
                return true
            }

            // Forward remaining calls (like transact) safely to original binder if present
            return try {
                if (originalBinder != null && args != null) {
                    method.invoke(originalBinder, *args)
                } else if (originalBinder != null) {
                    method.invoke(originalBinder)
                } else {
                    getDefaultReturnValue(method.returnType)
                }
            } catch (_: Throwable) {
                getDefaultReturnValue(method.returnType)
            }
        }
    }

    /**
     * Creates dynamic proxy for IPhoneSubInfo AIDL interface.
     */
    private fun createPhoneSubInfoProxy(profile: ProfileEntity): Any {
        val interfaceName = "com.android.internal.telephony.IPhoneSubInfo"
        val interfaceClass = try {
            Class.forName(interfaceName)
        } catch (_: Throwable) {
            IInterface::class.java
        }

        return Proxy.newProxyInstance(
            interfaceClass.classLoader,
            arrayOf(interfaceClass, IInterface::class.java),
            InvocationHandler { _, method, _ ->
                val name = method.name
                val fakeImei = if (profile.serialNumber.length >= 15) {
                    profile.serialNumber.take(15)
                } else {
                    (profile.serialNumber + "000000000000000").take(15)
                }

                when {
                    name.startsWith("getDeviceId") || name.startsWith("getImei") || name.startsWith("getMeid") -> {
                        fakeImei
                    }
                    name.startsWith("getSubscriberId") -> {
                        "310260" + profile.androidId.take(9)
                    }
                    name.startsWith("getIccSerialNumber") -> {
                        "8901410321111" + profile.androidId.take(6)
                    }
                    name.startsWith("getDeviceSvn") -> {
                        "01"
                    }
                    name == "asBinder" -> {
                        null
                    }
                    else -> getDefaultReturnValue(method.returnType)
                }
            }
        )
    }

    /**
     * Creates dynamic proxy for IWifiManager AIDL interface.
     */
    private fun createWifiManagerProxy(profile: ProfileEntity): Any {
        val interfaceName = "android.net.wifi.IWifiManager"
        val interfaceClass = try {
            Class.forName(interfaceName)
        } catch (_: Throwable) {
            IInterface::class.java
        }

        return Proxy.newProxyInstance(
            interfaceClass.classLoader,
            arrayOf(interfaceClass, IInterface::class.java),
            InvocationHandler { _, method, _ ->
                val name = method.name
                when {
                    name == "getConnectionInfo" -> {
                        createSpoofedWifiInfo(profile.macAddress)
                    }
                    name.contains("MacAddress") -> {
                        profile.macAddress
                    }
                    name == "getFactoryMacAddresses" -> {
                        arrayOf(profile.macAddress)
                    }
                    name == "asBinder" -> {
                        null
                    }
                    else -> getDefaultReturnValue(method.returnType)
                }
            }
        )
    }

    /**
     * Instantiates and reflects a WifiInfo object with spoofed MAC address.
     */
    fun createSpoofedWifiInfo(macAddress: String): WifiInfo? {
        return try {
            val constructor = WifiInfo::class.java.getDeclaredConstructor()
            constructor.isAccessible = true
            val wifiInfo = constructor.newInstance()

            // Set mMacAddress field via reflection
            val macField = WifiInfo::class.java.getDeclaredField("mMacAddress")
            macField.isAccessible = true
            macField.set(wifiInfo, macAddress)
            wifiInfo
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Injects the spoofed Android ID into Settings.Secure internal cache table.
     */
    @Suppress("UNCHECKED_CAST")
    fun hookSettingsSecureCache(spoofedAndroidId: String) {
        try {
            val secureClass = Settings.Secure::class.java
            val fields = secureClass.declaredFields

            for (field in fields) {
                if (field.name == "sNameValueCache" || field.type.name.contains("NameValueCache")) {
                    field.isAccessible = true
                    val cacheObj = field.get(null) ?: continue
                    val cacheClass = cacheObj.javaClass

                    // Try to inject into internal Map/Bundle
                    for (innerField in cacheClass.declaredFields) {
                        if (Map::class.java.isAssignableFrom(innerField.type)) {
                            innerField.isAccessible = true
                            val map = innerField.get(cacheObj) as? MutableMap<String, String>
                            map?.put(Settings.Secure.ANDROID_ID, spoofedAndroidId)
                            map?.put("android_id", spoofedAndroidId)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            Log.d(TAG, "Settings.Secure cache reflection: ${e.message}")
        }
    }

    /**
     * Intercepts Settings.Secure.getString queries dynamically.
     */
    fun getSpoofedSecureSetting(name: String, fallback: String = ""): String {
        if (name == Settings.Secure.ANDROID_ID || name == "android_id") {
            return activeProfile?.androidId ?: fallback
        }
        return fallback
    }

    /**
     * Returns true if dynamic proxy hooks are installed.
     */
    fun isInstalled(): Boolean = isHookInstalled

    /**
     * Resets service manager proxy hooks.
     */
    fun resetHooks() {
        activeProfile = null
        isHookInstalled = false
    }

    private fun getDefaultReturnValue(returnType: Class<*>): Any? {
        return when {
            returnType == Boolean::class.javaPrimitiveType -> false
            returnType == Int::class.javaPrimitiveType -> 0
            returnType == Long::class.javaPrimitiveType -> 0L
            returnType == Double::class.javaPrimitiveType -> 0.0
            returnType == Float::class.javaPrimitiveType -> 0.0f
            returnType == String::class.java -> ""
            returnType.isArray -> java.lang.reflect.Array.newInstance(returnType.componentType!!, 0)
            else -> null
        }
    }
}
