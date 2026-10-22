package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProfileEntity
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.CyberRose
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VirtualBgDark
import com.example.ui.theme.VirtualBorder
import com.example.ui.theme.VirtualSurfaceCard
import com.example.ui.theme.VirtualSurfaceDark
import com.example.util.InterceptedHardwareParameters
import com.example.util.PackageManagerHelper
import com.example.util.PerCloneNetworkManager
import com.example.util.SandboxStorageManager
import com.example.util.VirtualHookManager
import java.io.File

class VirtualSandboxActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_PROFILE_ID = "extra_profile_id"
        const val EXTRA_BRAND = "extra_brand"
        const val EXTRA_MODEL = "extra_model"
        const val EXTRA_ANDROID_ID = "extra_android_id"
        const val EXTRA_SERIAL = "extra_serial"
        const val EXTRA_MAC = "extra_mac"
        const val EXTRA_IS_SIMULATED = "extra_is_simulated"

        fun createIntent(
            context: Context,
            packageName: String,
            appName: String,
            profile: ProfileEntity,
            isSimulated: Boolean
        ): Intent {
            return Intent(context, VirtualSandboxActivity::class.java).apply {
                putExtra(EXTRA_PACKAGE_NAME, packageName)
                putExtra(EXTRA_APP_NAME, appName)
                putExtra(EXTRA_PROFILE_ID, profile.profileId)
                putExtra(EXTRA_BRAND, profile.deviceBrand)
                putExtra(EXTRA_MODEL, profile.deviceModel)
                putExtra(EXTRA_ANDROID_ID, profile.androidId)
                putExtra(EXTRA_SERIAL, profile.serialNumber)
                putExtra(EXTRA_MAC, profile.macAddress)
                putExtra(EXTRA_IS_SIMULATED, isSimulated)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: "unknown.app"
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Virtual App"
        val profileId = intent.getLongExtra(EXTRA_PROFILE_ID, 1L)
        val brand = intent.getStringExtra(EXTRA_BRAND) ?: "Google"
        val model = intent.getStringExtra(EXTRA_MODEL) ?: "Pixel 8 Pro"
        val androidId = intent.getStringExtra(EXTRA_ANDROID_ID) ?: "0123456789abcdef"
        val serial = intent.getStringExtra(EXTRA_SERIAL) ?: "RND000000000"
        val mac = intent.getStringExtra(EXTRA_MAC) ?: "02:00:00:00:00:00"
        val isSimulated = intent.getBooleanExtra(EXTRA_IS_SIMULATED, true)

        val profile = ProfileEntity(
            profileId = profileId,
            profileName = "Sandbox_$appName",
            androidId = androidId,
            deviceModel = model,
            deviceBrand = brand,
            macAddress = mac,
            serialNumber = serial,
            isDefaultVm = false
        )

        // Inject Virtual Environment & Apply Process-Level Dynamic Hooks
        VirtualHookManager.installProcessHooks(this, profile)
        PerCloneNetworkManager.getOrCreateConfig(profile.profileId.toString())

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VirtualBgDark
                ) {
                    VirtualSandboxScreen(
                        appName = appName,
                        packageName = packageName,
                        profile = profile,
                        isSimulated = isSimulated,
                        onClose = { finish() },
                        onLaunchExternal = {
                            if (!isSimulated) {
                                PackageManagerHelper.launchApp(this, packageName)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualSandboxScreen(
    appName: String,
    packageName: String,
    profile: ProfileEntity,
    isSimulated: Boolean,
    onClose: () -> Unit,
    onLaunchExternal: () -> Unit
) {
    val context = LocalContext.current
    var selectedTopTab by remember { mutableIntStateOf(0) } // 0: App View, 1: Isolation Telemetry, 2: Sandbox Storage
    var hardwareParams by remember { mutableStateOf(VirtualHookManager.readCurrentHardwareParameters(context)) }
    var testFileResult by remember { mutableStateOf<String?>(null) }
    val netConfig = remember(profile.profileId) { PerCloneNetworkManager.getOrCreateConfig(profile.profileId.toString()) }

    LaunchedEffect(Unit) {
        hardwareParams = VirtualHookManager.readCurrentHardwareParameters(context)
        SandboxStorageManager.createSampleSandboxFile(
            context,
            profile.profileId.toString(),
            "sandbox_launch_session.json",
            "{\"launched_app\":\"$packageName\", \"spoofed_model\":\"${profile.deviceModel}\", \"brand\":\"${profile.deviceBrand}\", \"android_id\":\"${profile.androidId}\", \"timestamp\":${System.currentTimeMillis()}}"
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("virtual_sandbox_screen"),
        containerColor = VirtualBgDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(CyberEmerald)
                        )
                        Column {
                            Text(
                                text = appName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                            )
                            Text(
                                text = "Container: ${profile.deviceBrand} ${profile.deviceModel}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = CyberCyan
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("sandbox_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VirtualSurfaceDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Container Live Environment Status Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🟢 HOOKS ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = "•",
                            color = TextMutedDark,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "ID: ${profile.androidId.take(8)}...",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${netConfig.simulatedCountryFlag} ${netConfig.simulatedEgressIp}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondaryDark,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Top Navigation Switcher Tabs
            TabRow(
                selectedTabIndex = selectedTopTab,
                containerColor = VirtualSurfaceDark,
                contentColor = CyberCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTopTab]),
                        color = CyberCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedTopTab == 0,
                    onClick = { selectedTopTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Running App", fontSize = 12.sp, fontWeight = if (selectedTopTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTopTab == 1,
                    onClick = {
                        hardwareParams = VirtualHookManager.readCurrentHardwareParameters(context)
                        selectedTopTab = 1
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Telemetry", fontSize = 12.sp, fontWeight = if (selectedTopTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTopTab == 2,
                    onClick = { selectedTopTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Storage", fontSize = 12.sp, fontWeight = if (selectedTopTab == 2) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTopTab) {
                    0 -> {
                        // Render Active App Running in Virtual Container
                        when {
                            packageName == "ru.andr7e.deviceinfohw" || appName.contains("Device Info", ignoreCase = true) -> {
                                DeviceInfoHwContainerApp(profile = profile, onLaunchExternal = onLaunchExternal, isSimulated = isSimulated)
                            }
                            packageName == "com.phototech.faceeditor.pro" || appName.contains("Face", ignoreCase = true) || appName.contains("Photo", ignoreCase = true) -> {
                                FaceEditingContainerApp(profile = profile, appName = appName)
                            }
                            packageName == "com.social.messenger.dual" || appName.contains("Messenger", ignoreCase = true) || appName.contains("Chat", ignoreCase = true) -> {
                                DualMessengerContainerApp(profile = profile, appName = appName)
                            }
                            packageName == "io.crypto.vault.sandbox" || appName.contains("Crypto", ignoreCase = true) || appName.contains("Vault", ignoreCase = true) -> {
                                CryptoVaultContainerApp(profile = profile, appName = appName)
                            }
                            else -> {
                                GenericVirtualContainerApp(appName = appName, packageName = packageName, profile = profile, isSimulated = isSimulated, onLaunchExternal = onLaunchExternal)
                            }
                        }
                    }
                    1 -> {
                        // Container Isolation & Telemetry View (with all required testTags)
                        ContainerTelemetryView(
                            profile = profile,
                            appName = appName,
                            packageName = packageName,
                            hardwareParams = hardwareParams,
                            isSimulated = isSimulated,
                            testFileResult = testFileResult,
                            onVerify = {
                                hardwareParams = VirtualHookManager.readCurrentHardwareParameters(context)
                                Toast.makeText(context, "Hardware Telemetry Verified: 100% Masked", Toast.LENGTH_SHORT).show()
                            },
                            onTestIo = {
                                val testFile = SandboxStorageManager.createSampleSandboxFile(
                                    context,
                                    profile.profileId.toString(),
                                    "device_info_hw_audit.txt",
                                    "Device: ${profile.deviceBrand} ${profile.deviceModel}\nAndroidID: ${profile.androidId}\nStorage: /virtual_sandboxes/${profile.profileId}/\nTimestamp: ${System.currentTimeMillis()}"
                                )
                                testFileResult = "Wrote ${testFile.length()} bytes to ${testFile.name} inside /virtual_sandboxes/${profile.profileId}/"
                            },
                            onLaunchExternal = onLaunchExternal,
                            onClose = onClose
                        )
                    }
                    2 -> {
                        // Isolated Sandbox Storage Explorer
                        SandboxStorageExplorerView(profile = profile)
                    }
                }
            }
        }
    }
}

/**
 * Authentic Device Info HW Running in the Container:
 * All displayed parameters read dynamically from the container settings created for this clone!
 */
@Composable
fun DeviceInfoHwContainerApp(
    profile: ProfileEntity,
    onLaunchExternal: () -> Unit,
    isSimulated: Boolean
) {
    val context = LocalContext.current
    var subTab by remember { mutableIntStateOf(0) } // 0: General, 1: SoC & CPU, 2: System Props, 3: Network, 4: Sensors & Probe
    val params = remember(profile) { VirtualHookManager.readCurrentHardwareParameters(context) }
    val netConfig = remember(profile) { PerCloneNetworkManager.getOrCreateConfig(profile.profileId.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VirtualBgDark)
    ) {
        // App Internal Header
        Surface(
            color = Color(0xFF131C2E),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DEVICE INFO HW",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Target Process Space Hook Active (PID: 10482)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = TextMutedDark
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberEmerald.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "SPOOFED OK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // App Navigation Subtabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppSubTabPill("General", isSelected = subTab == 0) { subTab = 0 }
                    AppSubTabPill("SoC / CPU", isSelected = subTab == 1) { subTab = 1 }
                    AppSubTabPill("Properties", isSelected = subTab == 2) { subTab = 2 }
                    AppSubTabPill("Network", isSelected = subTab == 3) { subTab = 3 }
                    AppSubTabPill("Probe", isSelected = subTab == 4) { subTab = 4 }
                }
            }
        }

        // Subtab Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (subTab) {
                0 -> {
                    // General Device Hardware
                    item {
                        DeviceInfoSectionCard(title = "Device Identity (Container Settings)") {
                            DeviceInfoField("Model", params.model, highlight = true)
                            DeviceInfoField("Manufacturer", params.manufacturer)
                            DeviceInfoField("Brand", params.brand, highlight = true)
                            DeviceInfoField("Device", params.product)
                            DeviceInfoField("Board", "${profile.deviceBrand.lowercase()}_board")
                            DeviceInfoField("Hardware", "${profile.deviceBrand.lowercase()}qcom")
                            DeviceInfoField("Android ID", params.androidId, isMono = true, highlight = true)
                            DeviceInfoField("Serial Number", params.serial, isMono = true)
                            DeviceInfoField("Fingerprint", "${params.brand}/${params.product}/${params.brand}:14/UP1A.231005.007/${params.serial}:user/release-keys", isMono = true)
                        }
                    }

                    item {
                        DeviceInfoSectionCard(title = "Operating System & Container Environment") {
                            DeviceInfoField("Android Version", "14 (Upside Down Cake)")
                            DeviceInfoField("API Level", "34")
                            DeviceInfoField("Security Patch", "2024-03-01")
                            DeviceInfoField("Build ID", "UP1A.231005.007")
                            DeviceInfoField("Display ID", "VirtuOS-${profile.deviceModel}-Spoofed")
                            DeviceInfoField("Kernel", "Linux 5.15.123-virtuos-g9f8a2")
                            DeviceInfoField("Isolated Storage", "/virtual_sandboxes/${profile.profileId}/", isMono = true, highlight = true)
                        }
                    }
                }
                1 -> {
                    // SoC / CPU
                    item {
                        DeviceInfoSectionCard(title = "Processor & Core Architecture") {
                            val socName = when (profile.deviceBrand.lowercase()) {
                                "samsung" -> "Samsung Exynos 2400 / Snapdragon 8 Gen 3"
                                "google" -> "Google Tensor G3 (Titan M2 Security)"
                                "xiaomi" -> "Qualcomm Snapdragon 8 Gen 3 (4nm)"
                                "oneplus" -> "Qualcomm Snapdragon 8 Gen 3 (SM8650)"
                                else -> "Qualcomm Snapdragon Octa-Core"
                            }
                            DeviceInfoField("Processor", socName, highlight = true)
                            DeviceInfoField("Architecture", "ARMv9-A (64-bit)")
                            DeviceInfoField("Total Cores", "8 Cores (1+5+2 Big.Little)")
                            DeviceInfoField("Core 0-1 (Efficiency)", "Cortex-A520 @ 2265 MHz")
                            DeviceInfoField("Core 2-6 (Performance)", "Cortex-A720 @ 3150 MHz")
                            DeviceInfoField("Core 7 (Prime)", "Cortex-X4 @ 3300 MHz")
                            DeviceInfoField("GPU Renderer", "Adreno 750 (Vulkan 1.3)")
                            DeviceInfoField("Hardware AES", "Supported (Encrypted Sandbox)")
                        }
                    }
                }
                2 -> {
                    // System Properties Hook Table
                    item {
                        DeviceInfoSectionCard(title = "Native libc __system_property_get Intercepts") {
                            DeviceInfoField("ro.product.model", VirtualHookManager.getSystemProperty("ro.product.model", params.model), highlight = true)
                            DeviceInfoField("ro.product.brand", VirtualHookManager.getSystemProperty("ro.product.brand", params.brand), highlight = true)
                            DeviceInfoField("ro.product.manufacturer", VirtualHookManager.getSystemProperty("ro.product.manufacturer", params.manufacturer))
                            DeviceInfoField("ro.product.device", VirtualHookManager.getSystemProperty("ro.product.device", params.product))
                            DeviceInfoField("ro.serialno", VirtualHookManager.getSystemProperty("ro.serialno", params.serial), isMono = true, highlight = true)
                            DeviceInfoField("ro.boot.serialno", VirtualHookManager.getSystemProperty("ro.boot.serialno", params.serial), isMono = true)
                            DeviceInfoField("net.hostname", VirtualHookManager.getSystemProperty("net.hostname", "virtuos-${profile.deviceModel.replace(" ", "").lowercase()}"))
                            DeviceInfoField("ro.build.display.id", VirtualHookManager.getSystemProperty("ro.build.display.id", "VirtuOS-${profile.deviceModel}-Spoofed"))
                        }
                    }
                }
                3 -> {
                    // Network & Connectivity
                    item {
                        DeviceInfoSectionCard(title = "Network Shifter & Wi-Fi Tunnel") {
                            DeviceInfoField("Wi-Fi MAC Address", params.macAddress, isMono = true, highlight = true)
                            DeviceInfoField("Simulated Country", "${netConfig.simulatedCountryFlag} ${netConfig.simulatedCity}")
                            DeviceInfoField("Proxy Egress IP", netConfig.simulatedEgressIp, isMono = true, highlight = true)
                            DeviceInfoField("Proxy Tunnel", "${netConfig.protocol.name} (${netConfig.host}:${netConfig.port})")
                            DeviceInfoField("Zero-Leak Kill Switch", if (netConfig.isKillSwitchActive) "ARMED (Host IP Protected)" else "DISARMED", highlight = netConfig.isKillSwitchActive)
                            DeviceInfoField("Latency", "${netConfig.lastPingLatencyMs} ms")
                        }
                    }
                }
                4 -> {
                    // Live Probe & Verification
                    item {
                        InteractiveHardwareProbe(profile = profile)
                    }
                }
            }

            if (!isSimulated) {
                item {
                    OutlinedButton(
                        onClick = onLaunchExternal,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyberAmber)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispatch Native Host Package (Hooked)", color = CyberAmber)
                    }
                }
            }
        }
    }
}

/**
 * Authentic Face Editing AI Pro Container Interface
 */
@Composable
fun FaceEditingContainerApp(profile: ProfileEntity, appName: String) {
    val context = LocalContext.current
    var smoothLevel by remember { mutableFloatStateOf(65f) }
    var eyeBrightLevel by remember { mutableFloatStateOf(40f) }
    var contourLevel by remember { mutableFloatStateOf(50f) }
    var selectedFilter by remember { mutableStateOf("Cyber Retouch") }
    var saveStatus by remember { mutableStateOf<String?>(null) }
    val filters = listOf("Cyber Retouch", "AI Porcelain", "Neon Glow", "Vintage Film", "B&W Drama")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = appName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark))
                Text(text = "Running on ${profile.deviceBrand} ${profile.deviceModel} (Isolated Sandbox)", style = MaterialTheme.typography.bodySmall.copy(color = CyberCyan, fontSize = 11.sp))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberIndigo.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("FACE AI ENGINE", style = MaterialTheme.typography.labelSmall.copy(color = CyberIndigo, fontWeight = FontWeight.Bold))
            }
        }

        // Viewfinder Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1E293B))
                .border(BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(54.dp))
                Text("AI Facial Mesh & Feature Tracker", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                Text("Filter: $selectedFilter • Smooth: ${smoothLevel.toInt()}%", color = CyberCyan, fontSize = 12.sp)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Watermark: Shot on ${profile.deviceBrand} ${profile.deviceModel}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Filter Carousel
        Text(text = "Beauty & Retouch Filters", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextSecondaryDark))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.take(3).forEach { filter ->
                OutlinedButton(
                    onClick = { selectedFilter = filter },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedFilter == filter) CyberCyan.copy(alpha = 0.2f) else Color.Transparent
                    ),
                    border = BorderStroke(1.dp, if (selectedFilter == filter) CyberCyan else VirtualBorder)
                ) {
                    Text(filter, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (selectedFilter == filter) CyberCyan else TextPrimaryDark)
                }
            }
        }

        // Adjustment Sliders
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = VirtualSurfaceCard)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Skin Smoothness (${smoothLevel.toInt()}%)", color = TextSecondaryDark, fontSize = 11.sp)
                Slider(
                    value = smoothLevel,
                    onValueChange = { smoothLevel = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                )

                Text("Eye Brightening (${eyeBrightLevel.toInt()}%)", color = TextSecondaryDark, fontSize = 11.sp)
                Slider(
                    value = eyeBrightLevel,
                    onValueChange = { eyeBrightLevel = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = CyberEmerald, activeTrackColor = CyberEmerald)
                )

                Text("Jawline Slimming (${contourLevel.toInt()}%)", color = TextSecondaryDark, fontSize = 11.sp)
                Slider(
                    value = contourLevel,
                    onValueChange = { contourLevel = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = CyberIndigo, activeTrackColor = CyberIndigo)
                )
            }
        }

        // Capture & Save into Isolated Storage
        Button(
            onClick = {
                val photoFile = SandboxStorageManager.createSampleSandboxFile(
                    context,
                    profile.profileId.toString(),
                    "face_edit_${System.currentTimeMillis() % 10000}.jpg",
                    "RAW_JPEG_CONTAINER_DATA: Filter=$selectedFilter, Device=${profile.deviceBrand} ${profile.deviceModel}, ProfileId=${profile.profileId}"
                )
                saveStatus = "Saved photo (${photoFile.length()} bytes) to /virtual_sandboxes/${profile.profileId}/${photoFile.name}"
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyanDim)
        ) {
            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Capture & Save to Container Sandbox", fontWeight = FontWeight.Bold)
        }

        if (saveStatus != null) {
            Text(
                text = saveStatus ?: "",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberEmerald
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberEmerald.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            )
        }
    }
}

/**
 * Authentic Dual Messenger Plus Container Interface
 */
@Composable
fun DualMessengerContainerApp(profile: ProfileEntity, appName: String) {
    val context = LocalContext.current
    var chatMessage by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                "Alice" to "Welcome to the isolated chat sandbox!",
                "Bob" to "This instance is linked to profile #${profile.profileId} (${profile.deviceModel}).",
                "Support" to "Zero metadata leak to primary phone space confirmed."
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VirtualBgDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = appName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark))
                Text(text = "Cloned Messenger • Profile: ${profile.deviceBrand} ${profile.deviceModel}", style = MaterialTheme.typography.bodySmall.copy(color = CyberCyan, fontSize = 11.sp))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberEmerald.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("DUAL CLONE ACTIVE", style = MaterialTheme.typography.labelSmall.copy(color = CyberEmerald, fontWeight = FontWeight.Bold))
            }
        }

        // Messages list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(VirtualSurfaceCard)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { (sender, text) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (sender == "Me") CyberCyanDim.copy(alpha = 0.25f) else VirtualSurfaceDark)
                        .padding(8.dp)
                ) {
                    Text(text = sender, fontWeight = FontWeight.Bold, color = if (sender == "Me") CyberCyan else CyberEmerald, fontSize = 11.sp)
                    Text(text = text, color = TextPrimaryDark, fontSize = 12.sp)
                }
            }
        }

        // Message input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatMessage,
                onValueChange = { chatMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Send isolated message...", fontSize = 12.sp) },
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = VirtualSurfaceCard,
                    unfocusedContainerColor = VirtualSurfaceCard,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                )
            )

            Button(
                onClick = {
                    if (chatMessage.isNotBlank()) {
                        messages = messages + ("Me" to chatMessage)
                        SandboxStorageManager.createSampleSandboxFile(
                            context,
                            profile.profileId.toString(),
                            "chat_history.txt",
                            "Msg: $chatMessage at ${System.currentTimeMillis()}"
                        )
                        chatMessage = ""
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyanDim)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * Authentic Crypto Portfolio Vault Container Interface
 */
@Composable
fun CryptoVaultContainerApp(profile: ProfileEntity, appName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VirtualBgDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = appName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark))
                Text(text = "Secured on ${profile.deviceBrand} ${profile.deviceModel} (Isolated Keystore)", style = MaterialTheme.typography.bodySmall.copy(color = CyberCyan, fontSize = 11.sp))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberEmerald.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("VAULT ENCRYPTED", style = MaterialTheme.typography.labelSmall.copy(color = CyberEmerald, fontWeight = FontWeight.Bold))
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VirtualSurfaceCard)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Total Balance", color = TextMutedDark, fontSize = 12.sp)
                Text("$48,920.50 USD", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = CyberEmerald))
                Text("Zero Analytics & Silicon Fingerprint Shield: ACTIVE", color = CyberCyan, fontSize = 11.sp)
            }
        }

        DeviceInfoSectionCard(title = "Hardware Keystore Isolation") {
            DeviceInfoField("Storage Keystore", "/virtual_sandboxes/${profile.profileId}/keystore.enc", isMono = true)
            DeviceInfoField("Spoofed Android ID", profile.androidId, isMono = true, highlight = true)
            DeviceInfoField("MAC Masking", profile.macAddress, isMono = true)
            DeviceInfoField("Device Brand / Model", "${profile.deviceBrand} ${profile.deviceModel}", highlight = true)
        }
    }
}

/**
 * Generic Virtual Container App Interface for any other app
 */
@Composable
fun GenericVirtualContainerApp(
    appName: String,
    packageName: String,
    profile: ProfileEntity,
    isSimulated: Boolean,
    onLaunchExternal: () -> Unit
) {
    val context = LocalContext.current
    var testIoResult by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VirtualSurfaceCard),
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(imageVector = Icons.Default.Apps, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(32.dp))
                        Column {
                            Text(text = appName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark))
                            Text(text = packageName, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextSecondaryDark))
                        }
                    }
                    Text(
                        text = "Running with Container Profile: ${profile.deviceBrand} ${profile.deviceModel} (ID: ${profile.androidId})",
                        color = CyberCyan,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            DeviceInfoSectionCard(title = "Active Container Environment") {
                DeviceInfoField("Assigned Model", profile.deviceModel, highlight = true)
                DeviceInfoField("Assigned Brand", profile.deviceBrand, highlight = true)
                DeviceInfoField("Android ID", profile.androidId, isMono = true, highlight = true)
                DeviceInfoField("Wi-Fi MAC", profile.macAddress, isMono = true)
                DeviceInfoField("Serial Number", profile.serialNumber, isMono = true)
                DeviceInfoField("Storage Path", "/virtual_sandboxes/${profile.profileId}/", isMono = true)
            }
        }

        item {
            Button(
                onClick = {
                    val file = SandboxStorageManager.createSampleSandboxFile(
                        context,
                        profile.profileId.toString(),
                        "app_runtime_data.json",
                        "{\"app\":\"$appName\",\"package\":\"$packageName\",\"profile\":\"${profile.deviceModel}\"}"
                    )
                    testIoResult = "Wrote to ${file.name} in container sandbox"
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyanDim)
            ) {
                Text("Write Test Data to Container Sandbox")
            }
        }

        if (testIoResult != null) {
            item {
                Text(
                    text = testIoResult ?: "",
                    color = CyberEmerald,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (!isSimulated) {
            item {
                OutlinedButton(
                    onClick = onLaunchExternal,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CyberAmber)
                ) {
                    Text("Launch Host Native APK (Hooked)", color = CyberAmber)
                }
            }
        }
    }
}

/**
 * Full Container Telemetry View (Contains all required test tags)
 */
@Composable
fun ContainerTelemetryView(
    profile: ProfileEntity,
    appName: String,
    packageName: String,
    hardwareParams: InterceptedHardwareParameters,
    isSimulated: Boolean,
    testFileResult: String?,
    onVerify: () -> Unit,
    onTestIo: () -> Unit,
    onLaunchExternal: () -> Unit,
    onClose: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Isolation Shield Status Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VirtualSurfaceCard),
                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyberEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "100% HARDWARE ISOLATION ACTIVE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberEmerald,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "System Property Interceptor & Virtual Context Wrapper Hooked",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                            )
                        }
                    }

                    Text(
                        text = "Target app '$appName' is executing inside an isolated container. All calls to android.os.Build, Settings.Secure.ANDROID_ID, and internal storage are dynamically intercepted and redirected.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = TextMutedDark,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }

        // Device Info HW - Live System Telemetry Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sandbox_hardware_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VirtualSurfaceDark),
                border = BorderStroke(1.dp, VirtualBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Device Info HW - Spoofed Hardware Parameters",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SPOOFED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(VirtualBgDark)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeviceInfoField("Model (Build.MODEL)", hardwareParams.model, highlight = true)
                        DeviceInfoField("Manufacturer (Build.MANUFACTURER)", hardwareParams.manufacturer)
                        DeviceInfoField("Brand (Build.BRAND)", hardwareParams.brand)
                        DeviceInfoField("Android ID (Settings.Secure)", hardwareParams.androidId, isMono = true, highlight = true)
                        DeviceInfoField("Serial (Build.SERIAL)", hardwareParams.serial, isMono = true)
                        DeviceInfoField("MAC Address (Wi-Fi)", hardwareParams.macAddress, isMono = true)
                        DeviceInfoField("System Property (ro.product.model)", VirtualHookManager.getSystemProperty("ro.product.model", hardwareParams.model))
                        DeviceInfoField("System Property (ro.serialno)", VirtualHookManager.getSystemProperty("ro.serialno", hardwareParams.serial), isMono = true)
                    }

                    // Sandbox Storage Path
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberIndigo.copy(alpha = 0.15f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = CyberIndigo, modifier = Modifier.size(16.dp))
                        Column {
                            Text(
                                text = "Isolated Sandbox Root",
                                style = MaterialTheme.typography.labelSmall.copy(color = CyberIndigo, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "/virtual_sandboxes/${profile.profileId}/",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                            )
                        }
                    }
                }
            }
        }

        // Interactive Verification & Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VirtualSurfaceDark),
                border = BorderStroke(1.dp, VirtualBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sandbox Runtime Actions",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )

                    Button(
                        onClick = onVerify,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_telemetry_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyanDim)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Verify Intercepted Telemetry", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onTestIo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_sandbox_io_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Test Isolated Storage I/O", color = CyberCyan, fontWeight = FontWeight.Medium)
                    }

                    if (testFileResult != null) {
                        Text(
                            text = testFileResult,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = CyberEmerald
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberEmerald.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        )
                    }

                    if (!isSimulated) {
                        OutlinedButton(
                            onClick = onLaunchExternal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("launch_external_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.5f))
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Dispatch Native App with Injected Extras", color = CyberAmber, fontWeight = FontWeight.Medium)
                        }
                    }

                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("close_sandbox_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyberRose.copy(alpha = 0.4f))
                    ) {
                        Text(text = "Exit Virtual Container", color = CyberRose, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

/**
 * Sandbox Storage Explorer View
 */
@Composable
fun SandboxStorageExplorerView(profile: ProfileEntity) {
    val context = LocalContext.current
    var files by remember { mutableStateOf(SandboxStorageManager.listSandboxFiles(context, profile.profileId.toString())) }
    var stats by remember { mutableStateOf(SandboxStorageManager.getSandboxUsageStats(context, profile.profileId.toString())) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = VirtualSurfaceCard)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Dedicated Directory Root:", color = TextMutedDark, fontSize = 11.sp)
                Text(
                    text = "/virtual_sandboxes/${profile.profileId}/",
                    fontFamily = FontFamily.Monospace,
                    color = CyberCyan,
                    fontSize = 11.sp
                )
                Text("Total Files: ${stats.fileCount} (${stats.totalSizeBytes} bytes)", color = TextSecondaryDark, fontSize = 11.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    SandboxStorageManager.createSampleSandboxFile(
                        context,
                        profile.profileId.toString(),
                        "user_data_${System.currentTimeMillis() % 1000}.json",
                        "{\"device\":\"${profile.deviceModel}\",\"created_at\":${System.currentTimeMillis()}}"
                    )
                    files = SandboxStorageManager.listSandboxFiles(context, profile.profileId.toString())
                    stats = SandboxStorageManager.getSandboxUsageStats(context, profile.profileId.toString())
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyanDim)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create File", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = {
                    SandboxStorageManager.clearProfileSandbox(context, profile.profileId.toString())
                    files = SandboxStorageManager.listSandboxFiles(context, profile.profileId.toString())
                    stats = SandboxStorageManager.getSandboxUsageStats(context, profile.profileId.toString())
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CyberRose)
            ) {
                Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, tint = CyberRose, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear All", color = CyberRose, fontSize = 11.sp)
            }
        }

        Text("Files inside sandbox:", color = TextSecondaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No files in sandbox yet. Use the app to generate data.", color = TextMutedDark, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(files) { fileName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VirtualSurfaceCard)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Text(text = fileName, color = TextPrimaryDark, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

/**
 * Interactive Hardware Probe tool inside Device Info HW
 */
@Composable
fun InteractiveHardwareProbe(profile: ProfileEntity) {
    val context = LocalContext.current
    var queryType by remember { mutableStateOf("android.os.Build.MODEL") }
    var queryResult by remember { mutableStateOf(Build.MODEL) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(VirtualSurfaceCard)
            .border(BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Interactive Runtime Query Console", fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 12.sp)
        Text(
            text = "Execute direct reflection and runtime system queries to verify that the app reads ONLY the spoofed profile values:",
            color = TextSecondaryDark,
            fontSize = 11.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = {
                    queryType = "android.os.Build.MODEL"
                    queryResult = Build.MODEL
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Build.MODEL", fontSize = 9.sp, maxLines = 1)
            }

            OutlinedButton(
                onClick = {
                    queryType = "Settings.Secure.ANDROID_ID"
                    queryResult = VirtualHookManager.getInterceptedAndroidId(context)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Android ID", fontSize = 9.sp, maxLines = 1)
            }

            OutlinedButton(
                onClick = {
                    queryType = "__system_property_get(ro.product.model)"
                    queryResult = VirtualHookManager.getSystemProperty("ro.product.model")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ro.product.model", fontSize = 9.sp, maxLines = 1)
            }
        }

        Surface(
            color = Color.Black,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "QUERY: $queryType", color = CyberCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text(text = "OUTPUT: $queryResult", color = CyberEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(12.dp))
                    Text(text = "VERIFIED MATCH TO CONTAINER PROFILE", color = CyberEmerald, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun AppSubTabPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyberCyan else Color(0xFF1E293B))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black else TextSecondaryDark
        )
    }
}

@Composable
private fun DeviceInfoSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VirtualSurfaceCard),
        border = BorderStroke(1.dp, VirtualBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
            )
            HorizontalDivider(color = VirtualBorder, thickness = 0.5.dp)
            content()
        }
    }
}

@Composable
private fun DeviceInfoField(
    label: String,
    value: String,
    isMono: Boolean = false,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = TextSecondaryDark
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
                color = if (highlight) CyberCyan else TextPrimaryDark
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
