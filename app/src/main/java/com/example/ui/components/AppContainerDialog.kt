package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VpnLock
import com.example.util.PerCloneNetworkManager
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.CyberRose
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VirtualBorder
import com.example.ui.theme.VirtualSurfaceCard
import com.example.ui.theme.VirtualSurfaceDark
import com.example.ui.viewmodel.ImportedAppWithProfile
import com.example.util.SandboxStats

/**
 * Inspection modal for an imported app in the Virtual Sandbox:
 * Shows active spoofed hardware parameters, isolated storage directory details,
 * and offers launch, sandbox testing, and mode switching.
 */
@Composable
fun AppContainerDialog(
    appWithProfile: ImportedAppWithProfile,
    isLaunching: Boolean,
    sandboxStats: SandboxStats?,
    sandboxFiles: List<String>,
    onLaunch: () -> Unit,
    onRandomizeIdentity: () -> Unit,
    onToggleProfileMode: () -> Unit,
    onCreateTestFile: () -> Unit,
    onClearSandbox: () -> Unit,
    onRemove: () -> Unit,
    onOpenSensorShield: (() -> Unit)? = null,
    onOpenNetworkShifter: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val app = appWithProfile.app
    val profile = appWithProfile.profile

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, VirtualBorder), RoundedCornerShape(24.dp))
                .testTag("app_container_dialog"),
            color = VirtualSurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CyberEmerald)
                        )
                        Text(
                            text = "PHASE 3 SANDBOX & MASKING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberEmerald,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMutedDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // App Hero Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(VirtualSurfaceCard)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (app.isCustomIdentity) CyberIndigo.copy(alpha = 0.2f)
                                else CyberCyan.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = app.appName,
                            tint = if (app.isCustomIdentity) CyberIndigo else CyberCyan,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (app.isCustomIdentity) CyberAmber.copy(alpha = 0.2f)
                                        else CyberCyan.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (app.isCustomIdentity) "OPTION B: UNIQUE PROFILE" else "OPTION A: MASTER VM PROFILE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (app.isCustomIdentity) CyberAmber else CyberCyan
                                    )
                                )
                            }
                        }
                    }
                }

                // Card 1: Masked Environment Identifiers
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(VirtualSurfaceCard)
                        .border(BorderStroke(1.dp, VirtualBorder), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Environment Masking",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                            )
                        }

                        IconButton(
                            onClick = onRandomizeIdentity,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CyberAmber.copy(alpha = 0.15f))
                                .testTag("container_randomize_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Randomize App Identity",
                                tint = CyberAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = VirtualBorder, thickness = 0.5.dp)

                    if (profile != null) {
                        ProfileParamRow(label = "Build.BRAND", value = profile.deviceBrand)
                        ProfileParamRow(label = "Build.MODEL", value = profile.deviceModel)
                        ProfileParamRow(label = "Build.SERIAL", value = profile.serialNumber, isMonospace = true)
                        ProfileParamRow(label = "ANDROID_ID", value = profile.androidId, isMonospace = true, highlight = true)
                        ProfileParamRow(label = "MAC_ADDRESS", value = profile.macAddress, isMonospace = true)
                    }

                    HorizontalDivider(color = VirtualBorder, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Assigned Profile #${profile?.profileId}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark)
                        )

                        OutlinedButton(
                            onClick = onToggleProfileMode,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CyberIndigo.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("switch_identity_mode_button")
                        ) {
                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyberIndigo)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (app.isCustomIdentity) "Switch to VM" else "Make Unique",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = CyberIndigo)
                            )
                        }
                    }
                }

                // Card 2: Isolated Sandbox Storage (Phase 3 Core Deliverable)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(VirtualSurfaceCard)
                        .border(BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = CyberEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Isolated Storage Sandbox",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberEmerald
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${sandboxStats?.fileCount ?: 0} FILES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberEmerald
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = VirtualBorder, thickness = 0.5.dp)

                    // Sandbox Directory Path
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Dedicated Sandbox Directory:",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark, fontSize = 10.sp)
                        )
                        Text(
                            text = sandboxStats?.path ?: ".../virtual_sandboxes/${profile?.profileId}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextPrimaryDark
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Files inside sandbox preview
                    if (sandboxFiles.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(VirtualSurfaceDark)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            sandboxFiles.take(4).forEach { file ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = file,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = TextSecondaryDark
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Sandbox file manipulation controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCreateTestFile,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("create_sandbox_file_button"),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = CyberCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Create File",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = CyberCyan)
                            )
                        }

                        OutlinedButton(
                            onClick = onClearSandbox,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("clear_sandbox_button"),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CyberRose.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(12.dp), tint = CyberRose)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear Data",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = CyberRose)
                            )
                        }
                    }
                }

                // Card 3: Option 3 Anti-Tracking Sensor & Biometric Shield
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(VirtualSurfaceCard)
                        .border(BorderStroke(1.dp, VirtualBorder), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyberEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Sensor & Touch Shield",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberEmerald
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ONLINE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberEmerald
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = VirtualBorder, thickness = 0.5.dp)

                    Text(
                        text = "Real-time micro-jitter proxy active (±0.005) on accelerometer & touch intervals to prevent silicon fingerprinting and behavioral habit profiling.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    )

                    if (onOpenSensorShield != null) {
                        OutlinedButton(
                            onClick = onOpenSensorShield,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("container_open_sensor_shield_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = CyberEmerald
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open Sensor Shield Lab & Waveforms",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = CyberEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Card 4: Option 5 Per-Clone Network Shifter (Virtual Proxy / Kill-Switch)
                val targetProfileId = profile?.profileId?.toString() ?: app.assignedProfileId.toString()
                val netConfig = PerCloneNetworkManager.getOrCreateConfig(targetProfileId)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(VirtualSurfaceCard)
                        .border(BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Icon(
                                imageVector = Icons.Default.VpnLock,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Per-Clone Network Shifter",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (netConfig.isKillSwitchActive) CyberEmerald.copy(alpha = 0.15f) else CyberAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (netConfig.isKillSwitchActive) "KILL-SWITCH ARMED" else "UNSHIELDED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netConfig.isKillSwitchActive) CyberEmerald else CyberAmber
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = VirtualBorder, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = netConfig.simulatedCountryFlag, fontSize = 18.sp)
                            Column {
                                Text(
                                    text = netConfig.simulatedCity,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextPrimaryDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "IP: ${netConfig.simulatedEgressIp} (${netConfig.protocol.name})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CyberCyan,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Text(
                            text = "${netConfig.lastPingLatencyMs}ms",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberEmerald,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = "Routes all outbound app traffic through a dedicated proxy tunnel. Real device IP is never exposed to analytics trackers.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    )

                    if (onOpenNetworkShifter != null) {
                        OutlinedButton(
                            onClick = onOpenNetworkShifter,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("container_open_network_shifter_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = CyberCyan
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Configure Proxy & Kill-Switch",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Actions: Launch in Virtual Sandbox & Delete
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onLaunch,
                        enabled = !isLaunching,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("launch_in_sandbox_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyanDim)
                    ) {
                        if (isLaunching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Masking & Launching Sandbox...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Launch in Virtual Sandbox",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("remove_app_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CyberRose.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = CyberRose,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Remove from Virtual Space",
                            color = CyberRose,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileParamRow(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark, fontSize = 11.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
                color = if (highlight) CyberCyan else TextPrimaryDark,
                fontSize = 11.sp
            )
        )
    }
}
