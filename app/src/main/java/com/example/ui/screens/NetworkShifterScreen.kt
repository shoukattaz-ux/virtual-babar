package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ProfileEntity
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.CyberRose
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VirtualBgDark
import com.example.ui.theme.VirtualBorder
import com.example.ui.theme.VirtualSurfaceCard
import com.example.ui.theme.VirtualSurfaceDark
import com.example.ui.viewmodel.ImportedAppWithProfile
import com.example.ui.viewmodel.VirtualSpaceUiState
import com.example.util.CloneProxyConfig
import com.example.util.NetworkExecutionResult
import com.example.util.NetworkPreset
import com.example.util.PerCloneNetworkManager
import com.example.util.ProxyProtocol
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NetworkShifterScreen(
    uiState: VirtualSpaceUiState,
    initialProfileId: String? = null,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val allConfigs by PerCloneNetworkManager.activeConfigsFlow.collectAsStateWithLifecycle()
    val logs by PerCloneNetworkManager.recentLogs.collectAsStateWithLifecycle()

    // Build selectable profile items: default VM profile + imported clone profiles
    val selectableProfiles = remember(uiState.profiles, uiState.importedApps) {
        val list = mutableListOf<Pair<String, String>>()
        uiState.defaultVmProfile?.let {
            list.add(it.profileId.toString() to "Master VM: ${it.deviceBrand} ${it.deviceModel}")
        }
        uiState.importedApps.forEach { item ->
            val p = item.profile
            val name = item.app.appName
            val id = p?.profileId?.toString() ?: item.app.assignedProfileId.toString()
            val label = "App: $name (#$id)"
            if (list.none { it.first == id }) {
                list.add(id to label)
            }
        }
        if (list.isEmpty()) {
            list.add("1" to "Default Master VM (#1)")
        }
        list
    }

    var selectedProfileId by remember {
        mutableStateOf(initialProfileId ?: selectableProfiles.firstOrNull()?.first ?: "1")
    }

    val activeConfig = allConfigs[selectedProfileId] ?: remember(selectedProfileId) {
        PerCloneNetworkManager.getOrCreateConfig(selectedProfileId)
    }

    // Editable form state
    var editHost by remember(activeConfig) { mutableStateOf(activeConfig.host) }
    var editPort by remember(activeConfig) { mutableStateOf(activeConfig.port.toString()) }
    var editProtocol by remember(activeConfig) { mutableStateOf(activeConfig.protocol) }

    // Test execution state
    var testUrl by remember { mutableStateOf("https://api.ipify.org?format=json") }
    var isExecutingTest by remember { mutableStateOf(false) }
    var lastTestResult by remember { mutableStateOf<NetworkExecutionResult?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("network_shifter_screen"),
        containerColor = VirtualBgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            VirtualBgDark,
                            Color(0xFF090D16)
                        )
                    )
                )
        ) {
            // Screen Header
            NetworkShifterHeader(
                onNavigateBack = onNavigateBack,
                onResetLogs = { PerCloneNetworkManager.clearLogs() }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Selector Carousel
                item {
                    ProfileSelectorBar(
                        profiles = selectableProfiles,
                        selectedId = selectedProfileId,
                        onSelect = { selectedProfileId = it }
                    )
                }

                // Active Egress Node & Telemetry Card
                item {
                    ActiveEgressCard(config = activeConfig)
                }

                // Zero-Leak Kill-Switch Card
                item {
                    KillSwitchShieldCard(
                        isKillSwitchActive = activeConfig.isKillSwitchActive,
                        onToggle = { enabled ->
                            PerCloneNetworkManager.toggleKillSwitch(selectedProfileId, enabled)
                        }
                    )
                }

                // Preset Node Selector
                item {
                    PresetNodeSelector(
                        selectedPreset = activeConfig.preset,
                        onSelectPreset = { preset ->
                            PerCloneNetworkManager.applyPreset(selectedProfileId, preset)
                            editHost = preset.defaultHost
                            editPort = preset.defaultPort.toString()
                            editProtocol = preset.protocol
                        }
                    )
                }

                // Custom Configuration & Manual Overrides
                item {
                    CustomProxyConfigCard(
                        host = editHost,
                        port = editPort,
                        protocol = editProtocol,
                        onHostChange = { editHost = it },
                        onPortChange = { editPort = it },
                        onProtocolChange = { editProtocol = it },
                        onSave = {
                            val parsedPort = editPort.toIntOrNull() ?: 8080
                            val updated = activeConfig.copy(
                                preset = NetworkPreset.CUSTOM,
                                protocol = editProtocol,
                                host = editHost.trim(),
                                port = parsedPort,
                                simulatedCity = "Custom Node",
                                simulatedCountryFlag = "🌐",
                                simulatedEgressIp = if (editHost.isNotBlank()) editHost else "Dynamic IP"
                            )
                            PerCloneNetworkManager.updateConfig(updated)
                        }
                    )
                }

                // Live Simulator & Kill-Switch Drop Tester
                item {
                    LiveNetworkSimulatorCard(
                        testUrl = testUrl,
                        onUrlChange = { testUrl = it },
                        isExecuting = isExecutingTest,
                        lastResult = lastTestResult,
                        onExecuteStandard = {
                            isExecutingTest = true
                            coroutineScope.launch {
                                val result = PerCloneNetworkManager.makeNetworkRequestForClone(
                                    profileId = selectedProfileId,
                                    targetUrl = testUrl,
                                    forceSimulateFailure = false
                                )
                                lastTestResult = result
                                isExecutingTest = false
                            }
                        },
                        onSimulateKillSwitchDrop = {
                            isExecutingTest = true
                            coroutineScope.launch {
                                val result = PerCloneNetworkManager.makeNetworkRequestForClone(
                                    profileId = selectedProfileId,
                                    targetUrl = testUrl,
                                    forceSimulateFailure = true
                                )
                                lastTestResult = result
                                isExecutingTest = false
                            }
                        }
                    )
                }

                // Live Network & Intercept Event Logs
                item {
                    NetworkLogTerminalCard(logs = logs)
                }
            }
        }
    }
}

@Composable
private fun NetworkShifterHeader(
    onNavigateBack: () -> Unit,
    onResetLogs: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VirtualSurfaceDark)
                    .testTag("network_shifter_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimaryDark
                )
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Network Shifter",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OPTION 5",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
                Text(
                    text = "Virtual Proxy Tunnels & Zero-Leak Kill-Switch",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                )
            }
        }

        IconButton(
            onClick = onResetLogs,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(VirtualSurfaceDark)
                .testTag("clear_network_logs_button")
        ) {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = "Clear Logs",
                tint = TextSecondaryDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ProfileSelectorBar(
    profiles: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ASSIGNED CLONE / PROFILE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextSecondaryDark,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            profiles.forEach { (id, label) ->
                val isSelected = id == selectedId
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) CyberCyan.copy(alpha = 0.22f) else VirtualSurfaceCard,
                    animationSpec = tween(200)
                )
                val borderColor = if (isSelected) CyberCyan else VirtualBorder

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(12.dp))
                        .clickable { onSelect(id) }
                        .testTag("profile_pill_$id"),
                    color = bgColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (id == "1") Icons.Default.Security else Icons.Default.VpnLock,
                            contentDescription = null,
                            tint = if (isSelected) CyberCyan else TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSelected) TextPrimaryDark else TextSecondaryDark,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveEgressCard(config: CloneProxyConfig) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)), RoundedCornerShape(18.dp))
            .testTag("active_egress_card"),
        color = VirtualSurfaceCard
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = config.simulatedCountryFlag,
                        fontSize = 24.sp
                    )
                    Column {
                        Text(
                            text = config.simulatedCity,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = "${config.protocol.name} Tunnel • ${config.preset.displayName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberEmerald.copy(alpha = 0.15f))
                        .border(1.dp, CyberEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CyberEmerald)
                        )
                        Text(
                            text = "${config.lastPingLatencyMs}ms",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberEmerald,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = VirtualBorder)

            // Grid of egress specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "OUTBOUND IP (EGRESS)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMutedDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = config.simulatedEgressIp,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PROXY TUNNEL ENDPOINT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMutedDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${config.host}:${config.port}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ROUTED / BLOCKED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMutedDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${config.totalRequestsRouted} req / ${config.totalKillSwitchBlocks} drop",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondaryDark,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun KillSwitchShieldCard(
    isKillSwitchActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val borderColor = if (isKillSwitchActive) CyberEmerald else CyberAmber.copy(alpha = 0.6f)
    val statusBg = if (isKillSwitchActive) CyberEmerald.copy(alpha = 0.12f) else CyberAmber.copy(alpha = 0.12f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(18.dp))
            .testTag("kill_switch_card"),
        color = VirtualSurfaceCard
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isKillSwitchActive) Icons.Default.Lock else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isKillSwitchActive) CyberEmerald else CyberAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Zero-Leak Network Kill-Switch",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = if (isKillSwitchActive) "ARMED • Zero Host IP Leakage Enforced"
                            else "DISARMED • Direct Host Fallback Allowed (Risk)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isKillSwitchActive) CyberEmerald else CyberAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Switch(
                    checked = isKillSwitchActive,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CyberEmerald,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("kill_switch_toggle")
                )
            }

            Text(
                text = "When active, if the assigned proxy drops or becomes unreachable, the virtual machine blocks all outbound sockets from this clone immediately. It strictly prevents falling back to your real phone's Wi-Fi/cellular connection, stopping anti-fraud trackers from associating accounts.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondaryDark,
                    lineHeight = 17.sp,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun PresetNodeSelector(
    selectedPreset: NetworkPreset,
    onSelectPreset: (NetworkPreset) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "CURATED EGRESS PRESETS (1-TAP SHIFT)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextSecondaryDark,
                letterSpacing = 1.sp
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                NetworkPreset.NEW_YORK,
                NetworkPreset.LONDON,
                NetworkPreset.TOKYO,
                NetworkPreset.FRANKFURT,
                NetworkPreset.SINGAPORE,
                NetworkPreset.CUSTOM
            ).forEach { preset ->
                val isSelected = selectedPreset == preset
                val bgColor = if (isSelected) CyberCyan.copy(alpha = 0.2f) else VirtualSurfaceDark
                val borderColor = if (isSelected) CyberCyan else VirtualBorder

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(12.dp))
                        .clickable { onSelectPreset(preset) }
                        .testTag("preset_${preset.name.lowercase()}"),
                    color = bgColor
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = preset.flag, fontSize = 16.sp)
                            Text(
                                text = preset.displayName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextPrimaryDark else TextSecondaryDark
                                )
                            )
                        }
                        if (preset != NetworkPreset.CUSTOM) {
                            Text(
                                text = "${preset.protocol.name} • ${preset.defaultPort}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMutedDark,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomProxyConfigCard(
    host: String,
    port: String,
    protocol: ProxyProtocol,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onProtocolChange: (ProxyProtocol) -> Unit,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(BorderStroke(1.dp, VirtualBorder), RoundedCornerShape(18.dp))
            .testTag("custom_proxy_config_card"),
        color = VirtualSurfaceCard
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "MANUAL PROXY OVERRIDES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark,
                    letterSpacing = 1.sp
                )
            )

            // Protocol Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(ProxyProtocol.HTTP, ProxyProtocol.SOCKS5, ProxyProtocol.DIRECT).forEach { proto ->
                    val isSelected = protocol == proto
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                1.dp,
                                if (isSelected) CyberCyan else VirtualBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onProtocolChange(proto) }
                            .testTag("proto_${proto.name.lowercase()}"),
                        color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else VirtualSurfaceDark
                    ) {
                        Text(
                            text = proto.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) CyberCyan else TextSecondaryDark,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Host & Port input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = host,
                    onValueChange = onHostChange,
                    label = { Text("Proxy Host / IP") },
                    placeholder = { Text("198.51.100.42") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(2f)
                        .testTag("proxy_host_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = VirtualBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = onPortChange,
                    label = { Text("Port") },
                    placeholder = { Text("8080") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("proxy_port_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = VirtualBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )
            }

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_proxy_config_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = VirtualBgDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Apply & Save Proxy Configuration",
                    color = VirtualBgDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LiveNetworkSimulatorCard(
    testUrl: String,
    onUrlChange: (String) -> Unit,
    isExecuting: Boolean,
    lastResult: NetworkExecutionResult?,
    onExecuteStandard: () -> Unit,
    onSimulateKillSwitchDrop: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(BorderStroke(1.dp, VirtualBorder), RoundedCornerShape(18.dp))
            .testTag("network_simulator_card"),
        color = VirtualSurfaceCard
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ISOLATED CONNECTION TEST TERMINAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            letterSpacing = 1.sp
                        )
                    )
                }

                if (isExecuting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = CyberCyan,
                        strokeWidth = 2.dp
                    )
                }
            }

            OutlinedTextField(
                value = testUrl,
                onValueChange = onUrlChange,
                label = { Text("Target Endpoint URL") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_url_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = VirtualBorder,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                )
            )

            // Test Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExecuteStandard,
                    enabled = !isExecuting,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("execute_proxy_test_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Route Request",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = onSimulateKillSwitchDrop,
                    enabled = !isExecuting,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("simulate_killswitch_drop_button"),
                    border = BorderStroke(1.dp, CyberRose),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = CyberRose,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Test Kill-Switch",
                        fontWeight = FontWeight.Bold,
                        color = CyberRose,
                        fontSize = 12.sp
                    )
                }
            }

            // Results Display
            lastResult?.let { result ->
                when (result) {
                    is NetworkExecutionResult.Success -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, CyberEmerald, RoundedCornerShape(12.dp)),
                            color = CyberEmerald.copy(alpha = 0.08f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = CyberEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Status: 200 OK • Outbound IP: ${result.egressIp} (${result.latencyMs}ms)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = CyberEmerald,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                }
                                Text(
                                    text = "Route: ${result.routeSummary}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VirtualBgDark)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = result.bodySnippet,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CyberCyan,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    is NetworkExecutionResult.KillSwitchBlocked -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, CyberRose, RoundedCornerShape(12.dp)),
                            color = CyberRose.copy(alpha = 0.08f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = CyberRose,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "ZERO-LEAK KILL SWITCH TRIGGERED",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = CyberRose,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Text(
                                    text = result.reason,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextPrimaryDark,
                                        fontSize = 12.sp
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VirtualBgDark)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "PROTECTION CONFIRMED: Outbound socket dropped.\nTarget: ${result.interceptedUrl}\nProxy Blocked: ${result.blockedProxy}\nHost Interface Exposed: 0 bytes (No leak)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CyberRose,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    is NetworkExecutionResult.DirectHostWarning -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, CyberAmber, RoundedCornerShape(12.dp)),
                            color = CyberAmber.copy(alpha = 0.08f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ DIRECT HOST TRAFFIC DETECTED",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyberAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = result.warning,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    is NetworkExecutionResult.Failed -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.Red, RoundedCornerShape(12.dp)),
                            color = Color.Red.copy(alpha = 0.08f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Error: ${result.errorMessage}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Red)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkLogTerminalCard(logs: List<com.example.util.NetworkLogEntry>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(BorderStroke(1.dp, VirtualBorder), RoundedCornerShape(18.dp))
            .testTag("network_log_terminal"),
        color = VirtualSurfaceCard
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "LIVE INTERCEPT & TUNNEL LOGS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Text(
                    text = "${logs.size} events",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(VirtualBgDark)
                    .padding(12.dp)
            ) {
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No proxy events yet. Tap 'Route Request' above to test.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark)
                        )
                    }
                } else {
                    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs.size) { index ->
                            val log = logs[index]
                            val logColor = when {
                                log.isKillSwitchEvent -> CyberRose
                                log.isSuccess -> CyberCyan
                                else -> CyberAmber
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = timeFormat.format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextMutedDark,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = log.message,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = logColor,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
