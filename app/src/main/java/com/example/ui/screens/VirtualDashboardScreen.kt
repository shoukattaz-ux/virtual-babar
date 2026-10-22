package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.example.ui.components.AppContainerDialog
import com.example.ui.components.VirtualStatusBar
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VirtualBgDark
import com.example.ui.theme.VirtualBorder
import com.example.ui.theme.VirtualSurfaceCard
import com.example.ui.theme.VirtualSurfaceDark
import com.example.ui.viewmodel.ImportedAppWithProfile
import com.example.ui.viewmodel.VirtualSpaceUiState

@Composable
fun VirtualDashboardScreen(
    uiState: VirtualSpaceUiState,
    onNavigateToAppPicker: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToSensorShield: () -> Unit,
    onNavigateToNetworkShifter: () -> Unit,
    onRandomizeMasterVm: () -> Unit,
    onOpenAppContainer: (ImportedAppWithProfile) -> Unit,
    onCloseAppContainer: () -> Unit,
    onLaunchApp: (Context, ImportedAppWithProfile) -> Unit,
    onRandomizeAppIdentity: (ImportedAppWithProfile) -> Unit,
    onToggleAppProfileMode: (ImportedAppWithProfile) -> Unit,
    onContainerCreateTestFile: (ImportedAppWithProfile) -> Unit,
    onContainerClearSandbox: (ImportedAppWithProfile) -> Unit,
    onRemoveApp: (ImportedAppWithProfile) -> Unit,
    onDismissNotification: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusNotification) {
        val msg = uiState.statusNotification
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            onDismissNotification()
        }
    }

    val activeModelName = uiState.defaultVmProfile?.let { "${it.deviceBrand} ${it.deviceModel}" } ?: "Pixel 8 Pro"

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("virtual_dashboard_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = VirtualBgDark
    ) { innerPadding ->
        Box(
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header / Fake Status Bar (Synced with active phone model)
                VirtualStatusBar(
                    activePhoneModel = activeModelName,
                    batteryLevel = 98
                )

                // Virtual Desktop Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Virtual Machine Status Card (System Widget with Randomize action)
                    VirtualHostWidget(
                        defaultVmProfile = uiState.defaultVmProfile,
                        importedCount = uiState.importedApps.size,
                        profileCount = uiState.profiles.size,
                        onOpenSettings = onNavigateToSettings,
                        onRandomizeMasterVm = onRandomizeMasterVm
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Title bar for App Grid & Action buttons
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
                                text = "Virtual Apps",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${uiState.importedApps.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Option 5: Network Shifter Button
                            IconButton(
                                onClick = onNavigateToNetworkShifter,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberCyan.copy(alpha = 0.15f))
                                    .testTag("network_shifter_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VpnLock,
                                    contentDescription = "Network Shifter & Kill-Switch",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Sensor & Touch Jitter Shield Button
                            IconButton(
                                onClick = onNavigateToSensorShield,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberEmerald.copy(alpha = 0.15f))
                                    .testTag("sensor_shield_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Sensor & Touch Shield Lab",
                                    tint = CyberEmerald,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Inspector / Diagnostics Button
                            IconButton(
                                onClick = onNavigateToDiagnostics,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberCyan.copy(alpha = 0.15f))
                                    .testTag("diagnostics_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = "Inspector / Diagnostics",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Settings Button
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Virtual Machine Settings",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Add App Button
                            IconButton(
                                onClick = onNavigateToAppPicker,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberCyanDim.copy(alpha = 0.3f))
                                    .testTag("add_app_header_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add App to Virtual Space",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Apps Grid Area
                    if (uiState.importedApps.isEmpty()) {
                        EmptyVirtualSpaceState(onImportApp = onNavigateToAppPicker)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("virtual_apps_grid"),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.importedApps, key = { it.app.id }) { item ->
                                VirtualAppGridItem(
                                    appWithProfile = item,
                                    onLaunch = { onLaunchApp(context, item) },
                                    onOpenSettings = { onOpenAppContainer(item) }
                                )
                            }
                        }
                    }
                }

                // Bottom Phone Dock
                VirtualPhoneDock(
                    onAddApp = onNavigateToAppPicker,
                    onOpenSettings = onNavigateToSettings,
                    onOpenDiagnostics = onNavigateToDiagnostics,
                    onOpenSensorShield = onNavigateToSensorShield
                )
            }
        }

        // Active App Container Dialog
        val activeApp = uiState.activeAppInContainer
        if (activeApp != null) {
            AppContainerDialog(
                appWithProfile = activeApp,
                isLaunching = uiState.isVirtualLaunching,
                sandboxStats = uiState.sandboxStats,
                sandboxFiles = uiState.sandboxFiles,
                onLaunch = { onLaunchApp(context, activeApp) },
                onRandomizeIdentity = { onRandomizeAppIdentity(activeApp) },
                onToggleProfileMode = { onToggleAppProfileMode(activeApp) },
                onCreateTestFile = { onContainerCreateTestFile(activeApp) },
                onClearSandbox = { onContainerClearSandbox(activeApp) },
                onRemove = { onRemoveApp(activeApp) },
                onOpenSensorShield = onNavigateToSensorShield,
                onOpenNetworkShifter = onNavigateToNetworkShifter,
                onDismiss = onCloseAppContainer
            )
        }
    }
}

@Composable
fun VirtualHostWidget(
    defaultVmProfile: com.example.data.model.ProfileEntity?,
    importedCount: Int,
    profileCount: Int,
    onOpenSettings: () -> Unit,
    onRandomizeMasterVm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("virtual_host_widget"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = VirtualSurfaceCard),
        border = BorderStroke(1.dp, VirtualBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "VirtuOS Master Host",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = defaultVmProfile?.let { "${it.deviceBrand} ${it.deviceModel}" } ?: "Configuring VM...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = CyberCyan
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick Randomize Master VM
                    IconButton(
                        onClick = onRandomizeMasterVm,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CyberAmber.copy(alpha = 0.15f))
                            .testTag("dashboard_randomize_master_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Randomize Master VM Identity",
                            tint = CyberAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberEmerald.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ISOLATED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberEmerald
                            )
                        )
                    }
                }
            }

            // Specs Quick Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HostStatPill(
                    label = "Android ID",
                    value = defaultVmProfile?.androidId?.take(8)?.plus("...") ?: "Pending",
                    modifier = Modifier.weight(1f)
                )
                HostStatPill(
                    label = "Profiles",
                    value = "$profileCount Active",
                    modifier = Modifier.weight(0.7f)
                )
                HostStatPill(
                    label = "Apps",
                    value = "$importedCount Imported",
                    modifier = Modifier.weight(0.7f)
                )
            }
        }
    }
}

@Composable
fun HostStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(VirtualSurfaceDark)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                color = TextMutedDark
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimaryDark
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Enhanced Virtual App Grid Item with distinct Option A / Option B Profile Badges
 */
/**
 * Enhanced Virtual App Grid Item with distinct Option A / Option B Profile Badges
 * Clicking the app launches it with container settings immediately.
 * A dedicated settings icon allows modifying the container settings.
 */
@Composable
fun VirtualAppGridItem(
    appWithProfile: ImportedAppWithProfile,
    onLaunch: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onClick: () -> Unit = onLaunch
) {
    val app = appWithProfile.app
    val profile = appWithProfile.profile
    val isCustom = app.isCustomIdentity
    val targetLaunch = if (onLaunch != {}) onLaunch else onClick

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = targetLaunch)
            .testTag("app_item_${app.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isCustom) CyberIndigo.copy(alpha = 0.22f)
                    else CyberCyan.copy(alpha = 0.18f)
                )
                .border(
                    BorderStroke(
                        1.5.dp,
                        if (isCustom) CyberIndigo.copy(alpha = 0.6f)
                        else CyberCyan.copy(alpha = 0.5f)
                    ),
                    RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = app.appName,
                tint = if (isCustom) CyberIndigo else CyberCyan,
                modifier = Modifier.size(34.dp)
            )

            // Top-Left: Settings gear button to configure container
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(24.dp)
                    .testTag("app_settings_btn_${app.packageName}")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Container Settings for ${app.appName}",
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(13.dp)
                )
            }

            // Top-Right Corner Identity Indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isCustom) CyberAmber else CyberCyanDim)
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text(
                    text = if (isCustom) "UID" else "VM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            // Bottom-Right Corner Quick Launch Play Indicator
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(CyberEmerald),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Launch in Container",
                    tint = Color.Black,
                    modifier = Modifier.size(11.dp)
                )
            }
        }

        // App Label
        Text(
            text = app.appName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                color = TextPrimaryDark,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Subtitle Profile Tag (Shows active model configured in container)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isCustom) CyberIndigo.copy(alpha = 0.18f)
                    else CyberCyan.copy(alpha = 0.15f)
                )
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = profile?.deviceModel ?: if (isCustom) "Unique ID" else "VM Profile",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isCustom) CyberIndigo else CyberCyan
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyVirtualSpaceState(onImportApp: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("empty_virtual_space_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = VirtualSurfaceCard.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, VirtualBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = "Virtual Phone is Ready",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )

                Text(
                    text = "No apps imported yet. Bring any installed app (e.g. Face Editing app) into this isolated space to spoof device identities.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center
                    )
                )

                Button(
                    onClick = onImportApp,
                    modifier = Modifier.testTag("empty_state_import_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyanDim)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import First App", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VirtualPhoneDock(
    onAddApp: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenSensorShield: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(VirtualSurfaceCard.copy(alpha = 0.95f))
            .border(BorderStroke(1.dp, VirtualBorder), RoundedCornerShape(26.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("virtual_phone_dock")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Identity Profiles
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onOpenSettings)
                    .testTag("dock_profiles_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VirtualSurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Identities",
                        tint = CyberIndigo,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Text(
                    text = "Identities",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = TextSecondaryDark
                    )
                )
            }

            // Sensor & Touch Shield (Option 3)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onOpenSensorShield)
                    .testTag("dock_shield_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VirtualSurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield",
                        tint = CyberEmerald,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Text(
                    text = "Shield",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = CyberEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Central Glowing Import Add App Button
            FloatingActionButton(
                onClick = onAddApp,
                modifier = Modifier
                    .size(50.dp)
                    .testTag("dock_add_app_fab"),
                shape = CircleShape,
                containerColor = CyberCyan,
                contentColor = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Import Application",
                    modifier = Modifier.size(26.dp)
                )
            }

            // Inspector / Diagnostics
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onOpenDiagnostics)
                    .testTag("dock_diagnostics_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(VirtualSurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Inspector",
                        tint = CyberEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Inspector",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = TextSecondaryDark
                    )
                )
            }

            // Settings / System
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onOpenSettings)
                    .testTag("dock_settings_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(VirtualSurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "VM Config",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = TextSecondaryDark
                    )
                )
            }
        }
    }
}
