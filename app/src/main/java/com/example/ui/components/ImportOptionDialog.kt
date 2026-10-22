package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InstalledApp
import com.example.data.model.ProfileEntity
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VirtualBorder
import com.example.ui.theme.VirtualSurfaceCard
import com.example.ui.theme.VirtualSurfaceDark
import com.example.ui.viewmodel.ImportOption

/**
 * Smart Import Prompt: Option Dialog Box offering:
 * Option A: Use Virtual Machine Identity (Inherit the main VM profile).
 * Option B: Create Unique / Custom Identity (Generate a separate, distinct profile for this app).
 */
@Composable
fun ImportOptionDialog(
    installedApp: InstalledApp,
    defaultVmProfile: ProfileEntity?,
    pendingCustomProfile: ProfileEntity?,
    selectedOption: ImportOption,
    onSelectOption: (ImportOption) -> Unit,
    onRandomizeCustomProfile: () -> Unit,
    onConfirmImport: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, VirtualBorder), RoundedCornerShape(24.dp))
                .testTag("import_options_dialog"),
            color = VirtualSurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with App Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberCyan.copy(alpha = 0.15f))
                            .border(BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f)), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = installedApp.appName,
                            tint = CyberCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = installedApp.appName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = installedApp.packageName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            ),
                            maxLines = 1
                        )
                    }
                }

                HorizontalDivider(color = VirtualBorder, thickness = 1.dp)

                // Dialog Prompt Title
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Import Options",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    )
                    Text(
                        text = "Select device identity sandbox mode for this application:",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )
                }

                // OPTION A: Use Virtual Machine Identity
                val isOptA = selectedOption == ImportOption.USE_VM_IDENTITY
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectOption(ImportOption.USE_VM_IDENTITY) }
                        .testTag("option_a_vm_identity"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOptA) CyberCyan.copy(alpha = 0.12f) else VirtualSurfaceCard
                    ),
                    border = BorderStroke(
                        width = if (isOptA) 1.5.dp else 1.dp,
                        color = if (isOptA) CyberCyan else VirtualBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = isOptA,
                                    onClick = { onSelectOption(ImportOption.USE_VM_IDENTITY) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CyberCyan,
                                        unselectedColor = TextMutedDark
                                    )
                                )
                                Column {
                                    Text(
                                        text = "Option A: Use VM Identity",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOptA) CyberCyan else TextPrimaryDark
                                        )
                                    )
                                    Text(
                                        text = "Inherit the main VM profile",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = TextSecondaryDark
                                        )
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "VM HOST",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                )
                            }
                        }

                        if (defaultVmProfile != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ProfileSpecMiniView(
                                profile = defaultVmProfile,
                                accentColor = CyberCyan
                            )
                        }
                    }
                }

                // OPTION B: Create Unique / Custom Identity
                val isOptB = selectedOption == ImportOption.CREATE_CUSTOM_IDENTITY
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectOption(ImportOption.CREATE_CUSTOM_IDENTITY) }
                        .testTag("option_b_custom_identity"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOptB) CyberIndigo.copy(alpha = 0.12f) else VirtualSurfaceCard
                    ),
                    border = BorderStroke(
                        width = if (isOptB) 1.5.dp else 1.dp,
                        color = if (isOptB) CyberIndigo else VirtualBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = isOptB,
                                    onClick = { onSelectOption(ImportOption.CREATE_CUSTOM_IDENTITY) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CyberIndigo,
                                        unselectedColor = TextMutedDark
                                    )
                                )
                                Column {
                                    Text(
                                        text = "Option B: Unique Identity",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOptB) CyberIndigo else TextPrimaryDark
                                        )
                                    )
                                    Text(
                                        text = "Generate a distinct profile for this app",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = TextSecondaryDark
                                        )
                                    )
                                }
                            }

                            if (isOptB) {
                                IconButton(
                                    onClick = onRandomizeCustomProfile,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("randomize_identity_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Casino,
                                        contentDescription = "Randomize Device Identity",
                                        tint = CyberAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        if (pendingCustomProfile != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ProfileSpecMiniView(
                                profile = pendingCustomProfile,
                                accentColor = CyberIndigo
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_cancel_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, VirtualBorder)
                    ) {
                        Text("Cancel", color = TextSecondaryDark)
                    }

                    Button(
                        onClick = onConfirmImport,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("dialog_confirm_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOptA) CyberCyanDim else CyberIndigo
                        )
                    ) {
                        Text(
                            text = "Import App",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSpecMiniView(
    profile: ProfileEntity,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(VirtualSurfaceDark.copy(alpha = 0.6f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Device:",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark)
            )
            Text(
                text = "${profile.deviceBrand} ${profile.deviceModel}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Android ID:",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark)
            )
            Text(
                text = profile.androidId,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = accentColor
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MAC:",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark)
            )
            Text(
                text = profile.macAddress,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondaryDark
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Serial No:",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark)
            )
            Text(
                text = profile.serialNumber,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondaryDark
                )
            )
        }
    }
}
