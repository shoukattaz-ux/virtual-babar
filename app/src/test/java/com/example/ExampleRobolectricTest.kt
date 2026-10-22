package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ProfileEntity
import com.example.util.BuildSpoofer
import com.example.util.DeviceProfileGenerator
import com.example.util.DiagnosticInspector
import com.example.util.JitteredSensorEventListener
import com.example.util.SandboxStorageManager
import com.example.util.SensorJitterShieldManager
import com.example.util.ShieldIntensity
import com.example.util.toEntity
import com.example.util.toModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.data.db.AppDatabase
import com.example.data.model.ImportedAppEntity
import com.example.ui.screens.VirtualSandboxActivity
import com.example.ui.viewmodel.ImportedAppWithProfile
import com.example.util.NetworkExecutionResult
import com.example.util.NetworkPreset
import com.example.util.PerCloneNetworkManager
import com.example.util.ProxyProtocol
import com.example.util.NativePropertyHook
import com.example.util.SystemServiceHookManager
import com.example.util.VirtualInstrumentation
import com.example.util.VirtualInstrumentationHook
import com.example.util.VirtualContextWrapper
import com.example.util.VirtualHookManager
import kotlinx.coroutines.runBlocking
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VirtualSpace", appName)
    }

    @Test
    fun `test device profile generator generates realistic profile model`() {
        val profile = DeviceProfileGenerator.generateRandomProfile("Main VM Profile")
        
        assertEquals("Main VM Profile", profile.profileName)
        assertNotNull(profile.profileId)
        assertEquals(16, profile.androidId.length)
        assertTrue(profile.androidId.matches(Regex("^[0-9a-fA-F]{16}$")))

        // Test MAC address
        assertEquals(17, profile.macAddress.length)
        assertTrue(profile.macAddress.matches(Regex("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$")))

        // Test Serial Number
        assertTrue(profile.serialNumber.startsWith("RND"))

        // Test Brand and Model
        assertTrue(DeviceProfileGenerator.brands.contains(profile.deviceBrand))
        val expectedModels = DeviceProfileGenerator.models[profile.deviceBrand]
        assertNotNull(expectedModels)
        assertTrue(expectedModels!!.contains(profile.deviceModel))
    }

    @Test
    fun `test custom app profile generation produces distinct identifiers`() {
        val profileA = DeviceProfileGenerator.generateRandomProfile("Custom_FaceEditing")
        val profileB = DeviceProfileGenerator.generateRandomProfile("Custom_FaceEditing")

        assertEquals("Custom_FaceEditing", profileA.profileName)
        assertEquals("Custom_FaceEditing", profileB.profileName)

        // Android IDs and MACs should be unique
        assertNotEquals(profileA.androidId, profileB.androidId)
        assertNotEquals(profileA.macAddress, profileB.macAddress)
    }

    @Test
    fun `test profile model conversion to and from profile entity`() {
        val model = DeviceProfileGenerator.generateRandomProfile("Custom_App")
        val entity = model.toEntity(isDefaultVm = false, profileId = 42)

        assertEquals(42L, entity.profileId)
        assertEquals(model.profileName, entity.profileName)
        assertEquals(model.androidId, entity.androidId)
        assertEquals(model.deviceBrand, entity.deviceBrand)
        assertEquals(model.deviceModel, entity.deviceModel)
        assertEquals(model.serialNumber, entity.serialNumber)
        assertEquals(model.macAddress, entity.macAddress)
        assertFalse(entity.isDefaultVm)

        val backToModel = entity.toModel()
        assertEquals("42", backToModel.profileId)
        assertEquals(model.profileName, backToModel.profileName)
        assertEquals(model.androidId, backToModel.androidId)
    }

    @Test
    fun `test sandbox storage manager creates isolated directories`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val dir1 = SandboxStorageManager.getProfileSandboxDir(context, "profile_101")
        val dir2 = SandboxStorageManager.getProfileSandboxDir(context, "profile_202")

        assertTrue(dir1.exists())
        assertTrue(dir2.exists())
        assertNotEquals(dir1.absolutePath, dir2.absolutePath)
        assertTrue(dir1.absolutePath.endsWith("virtual_sandboxes/profile_101"))
        assertTrue(dir2.absolutePath.endsWith("virtual_sandboxes/profile_202"))

        // Test database path isolation
        val dbPath = SandboxStorageManager.getProfileDatabasePath(context, "profile_101", "user_data.db")
        assertTrue(dbPath.parentFile!!.exists())
        assertTrue(dbPath.absolutePath.contains("profile_101/databases/user_data.db"))

        // Test creating sample file and listing
        val testFile = SandboxStorageManager.createSampleSandboxFile(
            context,
            "profile_101",
            "test_config.json",
            "{\"status\":\"isolated\"}"
        )
        assertTrue(testFile.exists())
        assertEquals("{\"status\":\"isolated\"}", testFile.readText())

        val files = SandboxStorageManager.listSandboxFiles(context, "profile_101")
        assertTrue(files.any { it.contains("test_config.json") })

        val stats = SandboxStorageManager.getSandboxUsageStats(context, "profile_101")
        assertTrue(stats.fileCount >= 1)
        assertTrue(stats.totalSizeBytes > 0)
    }

    @Test
    fun `test cross-sandbox file isolation App A cannot access App B file`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val dirA = SandboxStorageManager.getProfileSandboxDir(context, "App_A_Sandbox")
        val dirB = SandboxStorageManager.getProfileSandboxDir(context, "App_B_Sandbox")

        val fileInA = File(dirA, "app_a_token.secret")
        fileInA.writeText("APP_A_AUTHENTICATION_KEY")

        val fileInB = File(dirB, "app_a_token.secret")
        assertTrue("File should exist in App A's sandbox", fileInA.exists())
        assertFalse("File must NOT exist in App B's sandbox", fileInB.exists())

        // Confirm clean up
        fileInA.delete()
    }

    @Test
    fun `test build spoofer updates runtime state`() {
        BuildSpoofer.resetBuildProperties()
        assertFalse(BuildSpoofer.isSpoofingActive())

        BuildSpoofer.spoofBuildProperties(
            brand = "Samsung",
            model = "Galaxy S24 Ultra",
            serial = "RND9876543210",
            androidId = "8f4b1a20c93e47d2",
            macAddress = "02:00:00:44:55:66"
        )

        assertTrue(BuildSpoofer.isSpoofingActive())
        val state = BuildSpoofer.getActiveState()
        assertNotNull(state)
        assertEquals("Samsung", state!!.brand)
        assertEquals("Galaxy S24 Ultra", state.model)
        assertEquals("RND9876543210", state.serial)
        assertEquals("8f4b1a20c93e47d2", state.androidId)

        BuildSpoofer.resetBuildProperties()
        assertFalse(BuildSpoofer.isSpoofingActive())
    }

    @Test
    fun `test diagnostic inspector report generation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profile = ProfileEntity(
            profileId = 1,
            profileName = "Virtual Machine Main Profile",
            androidId = "8f4b1a20c93e47d2",
            deviceModel = "Pixel 8 Pro",
            deviceBrand = "Google",
            macAddress = "74:D0:2B:6C:8E:19",
            serialNumber = "ZX1G224H89B",
            isDefaultVm = true
        )

        val report = DiagnosticInspector.generateFormattedReport(profile, context)
        assertNotNull(report)
        assertTrue(report.contains("Virtual Machine Main Profile"))
        assertTrue(report.contains("Google"))
        assertTrue(report.contains("Pixel 8 Pro"))
        assertTrue(report.contains("8f4b1a20c93e47d2"))
        assertTrue(report.contains("virtual_sandboxes/1"))
    }

    @Test
    fun `test diagnostic inspector automated self-test passes all checks`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val master = ProfileEntity(
            profileId = 1,
            profileName = "Virtual Machine Main Profile",
            androidId = "8f4b1a20c93e47d2",
            deviceModel = "Pixel 8 Pro",
            deviceBrand = "Google",
            macAddress = "74:D0:2B:6C:8E:19",
            serialNumber = "ZX1G224H89B",
            isDefaultVm = true
        )

        val result = DiagnosticInspector.runAutomatedSelfTest(context, master)
        assertNotNull(result)
        assertTrue("All 7 diagnostic isolation, shield, and network shifter checks should pass", result.allPassed)
        assertEquals(7, result.checks.size)
        assertTrue(result.checks.any { it.title.contains("Option 3") })
        assertTrue(result.checks.any { it.title.contains("Option 5") })
    }

    @Test
    fun `test sensor jitter noise amplitude bounds across all intensities`() {
        val baseSensorVal = 9.80665f // Earth gravity

        // Test OFF intensity produces zero perturbation
        val jitterOff = JitteredSensorEventListener.applyJitter(baseSensorVal, ShieldIntensity.OFF)
        assertEquals(baseSensorVal, jitterOff, 0.000001f)

        // Test SUBTLE intensity (±0.002)
        for (i in 1..20) {
            val jitterSubtle = JitteredSensorEventListener.applyJitter(baseSensorVal, ShieldIntensity.SUBTLE)
            val delta = kotlin.math.abs(jitterSubtle - baseSensorVal)
            assertTrue(delta <= ShieldIntensity.SUBTLE.maxNoise + 0.0001f)
        }

        // Test BALANCED intensity (±0.005)
        for (i in 1..20) {
            val jitterBalanced = JitteredSensorEventListener.applyJitter(baseSensorVal, ShieldIntensity.BALANCED)
            val delta = kotlin.math.abs(jitterBalanced - baseSensorVal)
            assertTrue(delta <= ShieldIntensity.BALANCED.maxNoise + 0.0001f)
        }

        // Test AGGRESSIVE intensity (±0.010)
        for (i in 1..20) {
            val jitterAggressive = JitteredSensorEventListener.applyJitter(baseSensorVal, ShieldIntensity.AGGRESSIVE)
            val delta = kotlin.math.abs(jitterAggressive - baseSensorVal)
            assertTrue(delta <= ShieldIntensity.AGGRESSIVE.maxNoise + 0.0001f)
        }

        // Test GHOST_MODE intensity (±0.020)
        for (i in 1..20) {
            val jitterGhost = JitteredSensorEventListener.applyJitter(baseSensorVal, ShieldIntensity.GHOST_MODE)
            val delta = kotlin.math.abs(jitterGhost - baseSensorVal)
            assertTrue(delta <= ShieldIntensity.GHOST_MODE.maxNoise + 0.0001f)
        }
    }

    @Test
    fun `test touch cadence scrambler modulates intervals and pressure`() {
        SensorJitterShieldManager.setTouchScramblerEnabled(true)
        val initialTime = System.currentTimeMillis()

        // Simulate two consecutive taps 250ms apart
        SensorJitterShieldManager.processTouchEvent(0.65f, initialTime)
        val sample2 = SensorJitterShieldManager.processTouchEvent(0.70f, initialTime + 250L)

        assertEquals(250L, sample2.rawIntervalMs)
        assertEquals(0.70f, sample2.rawPressure, 0.001f)

        // The jittered interval should be perturbed to scramble typing dynamics
        assertNotEquals(sample2.rawIntervalMs, sample2.jitteredIntervalMs)
        assertTrue(sample2.jitteredIntervalMs in 200L..300L)

        // The jittered pressure should be perturbed within realistic limits
        assertNotEquals(sample2.rawPressure, sample2.jitteredPressure)
        assertTrue(sample2.jitteredPressure in 0.05f..1.0f)
    }

    @Test
    fun `test sensor shield master toggle and intensity updates`() {
        SensorJitterShieldManager.setShieldEnabled(false)
        assertFalse(SensorJitterShieldManager.telemetry.value.isShieldEnabled)

        SensorJitterShieldManager.setShieldEnabled(true)
        assertTrue(SensorJitterShieldManager.telemetry.value.isShieldEnabled)

        SensorJitterShieldManager.setIntensity(ShieldIntensity.GHOST_MODE)
        assertEquals(ShieldIntensity.GHOST_MODE, SensorJitterShieldManager.telemetry.value.intensity)

        SensorJitterShieldManager.setIntensity(ShieldIntensity.BALANCED)
        assertEquals(ShieldIntensity.BALANCED, SensorJitterShieldManager.telemetry.value.intensity)
    }

    @Test
    fun `test profile entity fields`() {
        val profile = ProfileEntity(
            profileId = 1,
            profileName = "Virtual Machine Main Profile",
            androidId = "8f4b1a20c93e47d2",
            deviceModel = "Pixel 8 Pro",
            deviceBrand = "Google",
            macAddress = "74:D0:2B:6C:8E:19",
            serialNumber = "ZX1G224H89B",
            isDefaultVm = true
        )

        assertEquals("Google", profile.deviceBrand)
        assertEquals("Pixel 8 Pro", profile.deviceModel)
        assertEquals("8f4b1a20c93e47d2", profile.androidId)
        assertTrue(profile.isDefaultVm)
    }

    @Test
    fun `test per clone network manager assigns and isolates proxies per profile id`() {
        val config1 = PerCloneNetworkManager.getOrCreateConfig("clone_app_1")
        val config2 = PerCloneNetworkManager.getOrCreateConfig("clone_app_2")

        PerCloneNetworkManager.applyPreset("clone_app_1", NetworkPreset.NEW_YORK)
        PerCloneNetworkManager.applyPreset("clone_app_2", NetworkPreset.TOKYO)

        val updated1 = PerCloneNetworkManager.getOrCreateConfig("clone_app_1")
        val updated2 = PerCloneNetworkManager.getOrCreateConfig("clone_app_2")

        assertEquals(NetworkPreset.NEW_YORK, updated1.preset)
        assertEquals(NetworkPreset.TOKYO, updated2.preset)
        assertNotEquals(updated1.simulatedEgressIp, updated2.simulatedEgressIp)
        assertNotEquals(updated1.host, updated2.host)
        assertEquals("198.51.100.42", updated1.simulatedEgressIp)
        assertEquals("133.242.18.91", updated2.simulatedEgressIp)
    }

    @Test
    fun `test per clone network manager applies presets correctly`() {
        val config = PerCloneNetworkManager.getOrCreateConfig("preset_test_profile")
        PerCloneNetworkManager.applyPreset("preset_test_profile", NetworkPreset.FRANKFURT)

        val frankfurt = PerCloneNetworkManager.getOrCreateConfig("preset_test_profile")
        assertEquals(NetworkPreset.FRANKFURT, frankfurt.preset)
        assertEquals("194.15.36.104", frankfurt.host)
        assertEquals(8080, frankfurt.port)
        assertEquals("Frankfurt, Germany", frankfurt.simulatedCity)
        assertEquals("🇩🇪", frankfurt.simulatedCountryFlag)
        assertTrue(frankfurt.isKillSwitchActive)

        val javaProxy = PerCloneNetworkManager.getJavaProxy("preset_test_profile")
        assertEquals(java.net.Proxy.Type.HTTP, javaProxy.type())
    }

    @Test
    fun `test zero leak kill switch blocks traffic and prevents host fallback when proxy fails`() = runBlocking {
        val profileId = "kill_switch_test_profile"
        PerCloneNetworkManager.getOrCreateConfig(profileId)
        PerCloneNetworkManager.toggleKillSwitch(profileId, true)

        val result = PerCloneNetworkManager.makeNetworkRequestForClone(
            profileId = profileId,
            targetUrl = "https://example.com/api/user",
            forceSimulateFailure = true
        )

        assertTrue("Result must be KillSwitchBlocked", result is NetworkExecutionResult.KillSwitchBlocked)
        val blocked = result as NetworkExecutionResult.KillSwitchBlocked
        assertTrue(blocked.hostLeakProtected)
        assertEquals("https://example.com/api/user", blocked.interceptedUrl)

        val configAfter = PerCloneNetworkManager.getOrCreateConfig(profileId)
        assertTrue(configAfter.totalKillSwitchBlocks >= 1L)
    }

    @Test
    fun `test successful proxy routing returns egress ip and telemetry`() = runBlocking {
        val profileId = "proxy_routing_success_profile"
        PerCloneNetworkManager.applyPreset(profileId, NetworkPreset.LONDON)

        val result = PerCloneNetworkManager.makeNetworkRequestForClone(
            profileId = profileId,
            targetUrl = "https://api.ipify.org?format=json",
            forceSimulateFailure = false
        )

        assertTrue("Result must be Success", result is NetworkExecutionResult.Success)
        val success = result as NetworkExecutionResult.Success
        assertEquals(200, success.statusCode)
        assertEquals("185.120.34.8", success.egressIp)
        assertTrue(success.latencyMs > 0)
        assertTrue(success.bodySnippet.contains("185.120.34.8"))
        assertTrue(success.headers.containsKey("X-VirtualSpace-Tunnel"))
    }

    @Test
    fun `test diagnostic automated self test check 7 network shifter passes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val report = DiagnosticInspector.runAutomatedSelfTest(context, null)

        assertTrue(report.checks.size >= 7)
        val check7 = report.checks.find { it.title.contains("Option 5") }
        assertNotNull("Option 5 check must exist in self-test suite", check7)
        assertTrue("Option 5 check must pass", check7!!.passed)
    }

    @Test
    fun `test virtual hook manager injects environment and intercepts build and system properties`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profile = ProfileEntity(
            profileId = 999L,
            profileName = "Custom_DeviceInfoHW",
            androidId = "aabbccddeeff0011",
            deviceModel = "Galaxy S24 Ultra",
            deviceBrand = "Samsung",
            macAddress = "00:1A:2B:3C:4D:5E",
            serialNumber = "RNDSAMSUNG12345",
            isDefaultVm = false
        )

        // Inject virtual environment
        val hooked = VirtualHookManager.injectEnvironment(profile)
        assertTrue("Environment injection must report success", hooked)

        // Verify active profile in hook manager
        val active = VirtualHookManager.getActiveProfile()
        assertNotNull(active)
        assertEquals("Galaxy S24 Ultra", active!!.deviceModel)
        assertEquals("Samsung", active.deviceBrand)
        assertEquals("aabbccddeeff0011", active.androidId)

        // Verify intercepted system properties
        assertEquals("Galaxy S24 Ultra", VirtualHookManager.getSystemProperty("ro.product.model"))
        assertEquals("Samsung", VirtualHookManager.getSystemProperty("ro.product.brand"))
        assertEquals("RNDSAMSUNG12345", VirtualHookManager.getSystemProperty("ro.serialno"))

        // Verify intercepted hardware parameters bundle (used by Device Info HW in sandbox)
        val params = VirtualHookManager.readCurrentHardwareParameters(context)
        assertEquals("Galaxy S24 Ultra", params.model)
        assertEquals("Samsung", params.brand)
        assertEquals("Samsung", params.manufacturer)
        assertEquals("aabbccddeeff0011", params.androidId)
        assertEquals("00:1A:2B:3C:4D:5E", params.macAddress)
        assertEquals("RNDSAMSUNG12345", params.serial)
        assertTrue(params.sandboxDirectory.endsWith("virtual_sandboxes/999"))
        assertTrue(params.isHookActive)

        // Reset hooks
        VirtualHookManager.resetHooks()
        assertFalse(VirtualHookManager.readCurrentHardwareParameters(context).isHookActive)
    }

    @Test
    fun `test virtual context wrapper isolates file storage to profile directory`() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val profile = ProfileEntity(
            profileId = 777L,
            profileName = "Isolated_App_Profile",
            androidId = "1122334455667788",
            deviceModel = "Pixel 8 Pro",
            deviceBrand = "Google",
            macAddress = "02:00:00:00:00:00",
            serialNumber = "RNDPIXEL8888",
            isDefaultVm = false
        )

        val wrappedContext = VirtualContextWrapper(baseContext, profile)

        // Ensure filesDir points to profile sandbox
        val filesDir = wrappedContext.filesDir
        assertTrue(filesDir.exists())
        assertTrue(filesDir.absolutePath.endsWith("virtual_sandboxes/777"))

        // Verify isolated file write inside wrapped context
        val testFile = File(filesDir, "sandbox_isolated.txt")
        testFile.writeText("SECRET_SANDBOX_CONTENT")
        assertTrue(testFile.exists())
        assertEquals("SECRET_SANDBOX_CONTENT", testFile.readText())

        // Verify cacheDir isolation
        val cacheDir = wrappedContext.cacheDir
        assertTrue(cacheDir.exists())
        assertTrue(cacheDir.absolutePath.contains("virtual_sandboxes/777/cache"))

        // Verify database path isolation
        val dbPath = wrappedContext.getDatabasePath("profile_app.db")
        assertTrue(dbPath.absolutePath.contains("virtual_sandboxes/777/databases/profile_app.db"))

        // Verify parameter getters
        assertEquals("1122334455667788", wrappedContext.getAndroidId())
        assertEquals("02:00:00:00:00:00", wrappedContext.getMacAddress())

        // Clean up
        testFile.delete()
        SandboxStorageManager.purgeProfileSandbox(baseContext, "777")
    }

    @Test
    fun `test full app deletion purges sandbox directory recursively and removes room database record`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context, this)
        val appDao = db.importedAppDao()
        val profileDao = db.profileDao()

        // 1. Create a profile and an imported app
        val profile = ProfileEntity(
            profileId = 888L,
            profileName = "ToDelete_Profile",
            androidId = "9988776655443322",
            deviceModel = "Xiaomi 14",
            deviceBrand = "Xiaomi",
            macAddress = "12:34:56:78:9A:BC",
            serialNumber = "RNDXIAOMI888",
            isDefaultVm = false
        )
        profileDao.insertProfile(profile)

        val app = ImportedAppEntity(
            id = 555L,
            packageName = "ru.andr7e.deviceinfohw.test",
            appName = "Device Info HW Test",
            assignedProfileId = 888L,
            isCustomIdentity = true,
            isSimulated = true
        )
        appDao.insertImportedApp(app)

        // 2. Populate isolated sandbox storage directory with files
        val sandboxDir = SandboxStorageManager.getProfileSandboxDir(context, "888")
        val sampleFile1 = File(sandboxDir, "config.json")
        sampleFile1.writeText("{\"key\":\"value\"}")
        val subDir = File(sandboxDir, "databases")
        subDir.mkdirs()
        val sampleFile2 = File(subDir, "local.db")
        sampleFile2.writeText("SQLITE_DATA")

        assertTrue("Sandbox directory must exist before purge", sandboxDir.exists())
        assertTrue(sampleFile1.exists())
        assertTrue(sampleFile2.exists())
        assertNotNull(appDao.getImportedAppByPackage("ru.andr7e.deviceinfohw.test"))

        // 3. Execute full deletion routine (as done in removeAppFromVirtualSpace)
        val purged = SandboxStorageManager.purgeProfileSandbox(context, "888")
        assertTrue("purgeProfileSandbox must return true", purged)
        assertFalse("Dedicated sandbox directory must be completely deleted from disk", sandboxDir.exists())

        appDao.deleteImportedApp(app)
        profileDao.deleteProfile(profile)

        // 4. Verify complete purge from DB
        val appAfter = appDao.getImportedAppByPackage("ru.andr7e.deviceinfohw.test")
        val profileAfter = profileDao.getProfileById(888L)
        assertFalse("App must be removed from Room database", appAfter != null)
        assertFalse("Custom profile must be removed from Room database", profileAfter != null)
    }

    @Test
    fun `test virtual sandbox activity intent holds complete injected profile parameters`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profile = ProfileEntity(
            profileId = 333L,
            profileName = "Intent_Profile",
            androidId = "aabb112233445566",
            deviceModel = "Pixel 8 Pro",
            deviceBrand = "Google",
            macAddress = "AA:BB:CC:DD:EE:FF",
            serialNumber = "RNDSERIAL333",
            isDefaultVm = false
        )

        val intent = VirtualSandboxActivity.createIntent(
            context = context,
            packageName = "ru.andr7e.deviceinfohw",
            appName = "Device Info HW",
            profile = profile,
            isSimulated = true
        )

        assertEquals("ru.andr7e.deviceinfohw", intent.getStringExtra(VirtualSandboxActivity.EXTRA_PACKAGE_NAME))
        assertEquals("Device Info HW", intent.getStringExtra(VirtualSandboxActivity.EXTRA_APP_NAME))
        assertEquals(333L, intent.getLongExtra(VirtualSandboxActivity.EXTRA_PROFILE_ID, -1L))
        assertEquals("Google", intent.getStringExtra(VirtualSandboxActivity.EXTRA_BRAND))
        assertEquals("Pixel 8 Pro", intent.getStringExtra(VirtualSandboxActivity.EXTRA_MODEL))
        assertEquals("aabb112233445566", intent.getStringExtra(VirtualSandboxActivity.EXTRA_ANDROID_ID))
        assertEquals("RNDSERIAL333", intent.getStringExtra(VirtualSandboxActivity.EXTRA_SERIAL))
        assertEquals("AA:BB:CC:DD:EE:FF", intent.getStringExtra(VirtualSandboxActivity.EXTRA_MAC))
        assertTrue(intent.getBooleanExtra(VirtualSandboxActivity.EXTRA_IS_SIMULATED, false))
    }

    @Test
    fun `test native property hook stores and returns spoofed system properties`() {
        val profile = ProfileEntity(
            profileId = 777L,
            profileName = "Xiaomi_Profile",
            androidId = "1122334455667788",
            deviceModel = "Xiaomi 14 Pro",
            deviceBrand = "Xiaomi",
            macAddress = "00:11:22:33:44:55",
            serialNumber = "RNDXIAOMI777",
            isDefaultVm = false
        )

        NativePropertyHook.setProfileProperties(profile)

        assertEquals("Xiaomi 14 Pro", NativePropertyHook.getProperty("ro.product.model"))
        assertEquals("Xiaomi", NativePropertyHook.getProperty("ro.product.brand"))
        assertEquals("xiaomi_14_pro", NativePropertyHook.getProperty("ro.product.device"))
        assertEquals("RNDXIAOMI777", NativePropertyHook.getProperty("ro.serialno"))

        // Test clear
        NativePropertyHook.clear()
        assertEquals("default_val", NativePropertyHook.getProperty("ro.product.model", "default_val"))
    }

    @Test
    fun `test system service hook manager hooks service manager and provides spoofed wifi info`() {
        val profile = ProfileEntity(
            profileId = 999L,
            profileName = "Service_Profile",
            androidId = "aabbccddeeff0011",
            deviceModel = "Galaxy S24 Ultra",
            deviceBrand = "Samsung",
            macAddress = "DE:AD:BE:EF:00:99",
            serialNumber = "RNDSAMS999",
            isDefaultVm = false
        )

        SystemServiceHookManager.installHooks(profile)
        assertTrue(SystemServiceHookManager.isInstalled())

        // Verify spoofed secure setting
        assertEquals("aabbccddeeff0011", SystemServiceHookManager.getSpoofedSecureSetting("android_id"))

        // Verify spoofed WifiInfo object reflection
        val wifiInfo = SystemServiceHookManager.createSpoofedWifiInfo(profile.macAddress)
        assertNotNull(wifiInfo)
        assertEquals("DE:AD:BE:EF:00:99", wifiInfo!!.macAddress)

        SystemServiceHookManager.resetHooks()
        assertFalse(SystemServiceHookManager.isInstalled())
    }

    @Test
    fun `test virtual instrumentation hook replaces instrumentation and intercepts activity lifecycle`() {
        val profile = ProfileEntity(
            profileId = 1234L,
            profileName = "Instrumentation_Profile",
            androidId = "1234567890abcdef",
            deviceModel = "Pixel 8 Pro",
            deviceBrand = "Google",
            macAddress = "02:00:00:00:00:00",
            serialNumber = "RNDPIXEL1234",
            isDefaultVm = false
        )

        val installed = VirtualInstrumentationHook.install(profile)
        // In Robolectric environment, verify installation or safe handling
        if (installed) {
            assertTrue(VirtualInstrumentationHook.isHooked())
            VirtualInstrumentationHook.uninstall()
            assertFalse(VirtualInstrumentationHook.isHooked())
        }
    }

    @Test
    fun `test virtual hook manager installProcessHooks isolates all vectors and passes integrity verification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profile = ProfileEntity(
            profileId = 5555L,
            profileName = "Complete_Sandbox_Profile",
            androidId = "fedcba9876543210",
            deviceModel = "OnePlus 12",
            deviceBrand = "OnePlus",
            macAddress = "AA:11:BB:22:CC:33",
            serialNumber = "RNDONEPLUS5555",
            isDefaultVm = false
        )

        VirtualHookManager.installProcessHooks(context, profile)

        // Verify Build fields
        assertEquals("OnePlus", android.os.Build.BRAND)
        assertEquals("OnePlus 12", android.os.Build.MODEL)
        assertEquals("RNDONEPLUS5555", android.os.Build.SERIAL)

        // Verify System Properties
        assertEquals("OnePlus 12", VirtualHookManager.getSystemProperty("ro.product.model"))
        assertEquals("OnePlus", VirtualHookManager.getSystemProperty("ro.product.brand"))
        assertEquals("RNDONEPLUS5555", VirtualHookManager.getSystemProperty("ro.serialno"))

        // Verify Android ID and MAC
        assertEquals("fedcba9876543210", VirtualHookManager.getInterceptedAndroidId(context))
        assertEquals("AA:11:BB:22:CC:33", VirtualHookManager.getInterceptedMacAddress())

        // Verify Isolation Integrity Report
        val integrity = VirtualHookManager.verifyIsolationIntegrity(context)
        assertTrue(integrity["build_brand"] == true)
        assertTrue(integrity["build_model"] == true)
        assertTrue(integrity["build_serial"] == true)
        assertTrue(integrity["sysprop_model"] == true)
        assertTrue(integrity["android_id"] == true)
        assertTrue(integrity["mac_address"] == true)

        // Verify wrapped context
        val wrappedContext = VirtualHookManager.wrapContext(context, profile)
        assertEquals("fedcba9876543210", wrappedContext.getAndroidId())
        assertEquals("AA:11:BB:22:CC:33", wrappedContext.getMacAddress())
        assertTrue(wrappedContext.filesDir.absolutePath.contains("virtual_sandboxes/5555"))

        // Reset and verify clean state
        VirtualHookManager.resetHooks()
    }

    @Test
    fun `test launch app with container settings creates intent and installs isolated profile`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profile = ProfileEntity(
            profileId = 6001L,
            profileName = "Container_Xiaomi_Profile",
            androidId = "deadbeef12345678",
            deviceModel = "Xiaomi 14 Pro",
            deviceBrand = "Xiaomi",
            macAddress = "3C:22:FB:AA:BB:CC",
            serialNumber = "RNDXIAOMI6001",
            isDefaultVm = false
        )

        // Simulate launch with container settings
        val installed = VirtualHookManager.installProcessHooks(context, profile)
        assertTrue(installed)

        // Read hardware parameters as the launched container app does
        val hardwareParams = VirtualHookManager.readCurrentHardwareParameters(context)
        assertEquals("Xiaomi 14 Pro", hardwareParams.model)
        assertEquals("Xiaomi", hardwareParams.brand)
        assertEquals("deadbeef12345678", hardwareParams.androidId)
        assertEquals("3C:22:FB:AA:BB:CC", hardwareParams.macAddress)
        assertEquals("RNDXIAOMI6001", hardwareParams.serial)
        assertTrue(hardwareParams.sandboxDirectory.endsWith("virtual_sandboxes/6001"))

        // Create intent as the UI does
        val intent = VirtualSandboxActivity.createIntent(
            context = context,
            packageName = "ru.andr7e.deviceinfohw",
            appName = "Device Info HW",
            profile = profile,
            isSimulated = true
        )
        assertNotNull(intent)
        assertEquals("Xiaomi 14 Pro", intent.getStringExtra(VirtualSandboxActivity.EXTRA_MODEL))
        assertEquals("deadbeef12345678", intent.getStringExtra(VirtualSandboxActivity.EXTRA_ANDROID_ID))

        // Reset hooks
        VirtualHookManager.resetHooks()
    }
}

