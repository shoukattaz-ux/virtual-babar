package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.viewmodel.VirtualSpaceUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: VirtualSpaceUiState,
    onNavigateBack: () -> Unit,
    onRandomizeMasterVm: () -> Unit,
    onRandomizeProfile: (ProfileEntity) -> Unit,
    onCreateNewProfile: (String) -> Unit,
    onDeleteProfile: (ProfileEntity) -> Unit
) {
    var showCreateProfileDialog by remember { mutableStateOf(false) }
    BackHandler { onNavigateBack() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Virtual Device Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = "Randomize Engine & Profile Management",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryDark
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCreateProfileDialog = true },
                        modifier = Modifier.testTag("add_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Profile",
                            tint = CyberCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VirtualSurfaceDark
                )
            )
        },
        containerColor = VirtualBgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Virtual Machine Profile Hero Card with prominent Randomize Button
            item {
                val masterVm = uiState.defaultVmProfile
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("master_vm_settings_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = VirtualSurfaceCard),
                    border = BorderStroke(1.5.dp, CyberCyan.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
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
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CyberCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Master Virtual Machine Profile",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryDark
                                        )
                                    )
                                    Text(
                                        text = "Active Base Identity for Option A Apps",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = CyberCyan
                                        )
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberEmerald.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberEmerald
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = VirtualBorder, thickness = 0.5.dp)

                        if (masterVm != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SpecItemRow(label = "device_brand", value = masterVm.deviceBrand)
                                SpecItemRow(label = "device_model", value = masterVm.deviceModel)
                                SpecItemRow(label = "android_id", value = masterVm.androidId, isMonospace = true, highlight = true)
                                SpecItemRow(label = "mac_address", value = masterVm.macAddress, isMonospace = true)
                                SpecItemRow(label = "serial_number", value = masterVm.serialNumber, isMonospace = true)
                            }
                        }

                        // Prominent "Randomize Identity" Button as required in Task 2
                        Button(
                            onClick = onRandomizeMasterVm,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("randomize_master_vm_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberAmber)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Randomize Master VM Identity",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }
            }

            // Phase 2 Architecture Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = VirtualSurfaceDark),
                    border = BorderStroke(1.dp, VirtualBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyberIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Phase 2 Engine: DeviceProfileGenerator generates unique hardware fingerprints and persists them into Room DB for both Master VM & Custom app identities.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        )
                    }
                }
            }

            // Section Header: Stored Profiles
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Device Profiles (${uiState.profiles.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )

                    Button(
                        onClick = { showCreateProfileDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyanDim),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Profile", fontSize = 12.sp)
                    }
                }
            }

            // List of Profile Cards
            items(uiState.profiles, key = { it.profileId }) { profile ->
                ProfileDetailCard(
                    profile = profile,
                    onRandomize = { onRandomizeProfile(profile) },
                    onDelete = { onDeleteProfile(profile) }
                )
            }
        }

        if (showCreateProfileDialog) {
            CreateProfileDialog(
                onDismiss = { showCreateProfileDialog = false },
                onCreate = { name ->
                    onCreateNewProfile(name)
                    showCreateProfileDialog = false
                }
            )
        }
    }
}

@Composable
fun ProfileDetailCard(
    profile: ProfileEntity,
    onRandomize: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_card_${profile.profileId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (profile.isDefaultVm) VirtualSurfaceCard else VirtualSurfaceDark
        ),
        border = BorderStroke(
            1.dp,
            if (profile.isDefaultVm) CyberCyan.copy(alpha = 0.5f) else VirtualBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
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
                            .clip(CircleShape)
                            .background(
                                if (profile.isDefaultVm) CyberCyan.copy(alpha = 0.2f)
                                else CyberIndigo.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = if (profile.isDefaultVm) CyberCyan else CyberIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = profile.profileName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "Profile ID: #${profile.profileId}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMutedDark,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (profile.isDefaultVm) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "MAIN VM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Profile",
                                tint = CyberRose.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Randomize Identity Action
                    IconButton(
                        onClick = onRandomize,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CyberAmber.copy(alpha = 0.15f))
                            .testTag("randomize_button_${profile.profileId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Randomize Identity",
                            tint = CyberAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = VirtualBorder, thickness = 0.5.dp)

            // Hardware Specs Fields Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SpecItemRow(label = "device_brand", value = profile.deviceBrand)
                SpecItemRow(label = "device_model", value = profile.deviceModel)
                SpecItemRow(label = "android_id", value = profile.androidId, isMonospace = true, highlight = true)
                SpecItemRow(label = "mac_address", value = profile.macAddress, isMonospace = true)
                SpecItemRow(label = "serial_number", value = profile.serialNumber, isMonospace = true)
            }
        }
    }
}

@Composable
fun SpecItemRow(
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
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMutedDark,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
                color = if (highlight) CyberCyan else TextPrimaryDark,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun CreateProfileDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var profileName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.dp, VirtualBorder), RoundedCornerShape(20.dp)),
            color = VirtualSurfaceDark
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Create Device Identity Profile",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )

                Text(
                    text = "DeviceProfileGenerator will generate a new randomized hardware fingerprint with Android ID, MAC address, serial number, and realistic device model.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )

                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    placeholder = { Text("e.g., Face Editing Custom Profile", color = TextMutedDark) },
                    label = { Text("Profile Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_profile_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = VirtualBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedContainerColor = VirtualSurfaceCard,
                        unfocusedContainerColor = VirtualSurfaceCard
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, VirtualBorder)
                    ) {
                        Text("Cancel", color = TextSecondaryDark)
                    }

                    Button(
                        onClick = {
                            val name = profileName.ifBlank { "Custom Identity Profile" }
                            onCreate(name)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_create_profile_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyanDim)
                    ) {
                        Text("Generate")
                    }
                }
            }
        }
    }
}
