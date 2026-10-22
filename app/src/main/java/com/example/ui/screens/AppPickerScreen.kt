package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InstalledApp
import com.example.ui.components.ImportOptionDialog
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
import com.example.ui.viewmodel.ImportOption
import com.example.ui.viewmodel.VirtualSpaceUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(
    uiState: VirtualSpaceUiState,
    onNavigateBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleIncludeSystemApps: (Context) -> Unit,
    onSelectAppForImport: (InstalledApp) -> Unit,
    onDismissDialog: () -> Unit,
    onSelectOption: (ImportOption) -> Unit,
    onRandomizeCustomProfile: () -> Unit,
    onConfirmImport: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_picker_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Import Application",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = "Select an app to clone into Virtual Machine",
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
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryDark
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_app_input"),
                placeholder = {
                    Text(
                        "Search by name or package...",
                        color = TextMutedDark,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CyberCyan
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TextSecondaryDark
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
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

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (User Apps vs System Apps)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.filteredInstalledApps.size} apps found",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Medium
                    )
                )

                FilterChip(
                    selected = uiState.includeSystemApps,
                    onClick = { onToggleIncludeSystemApps(context) },
                    label = {
                        Text(
                            text = if (uiState.includeSystemApps) "All Apps (Inc. System)" else "User Apps Only",
                            fontSize = 11.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                        selectedLabelColor = CyberCyan,
                        containerColor = VirtualSurfaceCard,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = uiState.includeSystemApps,
                        borderColor = if (uiState.includeSystemApps) CyberCyan else VirtualBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Installed Apps List
            if (uiState.isLoadingApps) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = CyberCyan)
                        Text(
                            "Scanning PackageManager...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                        )
                    }
                }
            } else if (uiState.filteredInstalledApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No apps match '${uiState.searchQuery}'",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .testTag("installed_apps_list"),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredInstalledApps, key = { it.packageName }) { app ->
                        val isAlreadyImported = uiState.importedApps.any { it.app.packageName == app.packageName }
                        InstalledAppRowItem(
                            app = app,
                            isAlreadyImported = isAlreadyImported,
                            onClick = {
                                if (!isAlreadyImported) {
                                    onSelectAppForImport(app)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Smart Import Option Dialog
        val selectedApp = uiState.selectedAppForImport
        if (selectedApp != null) {
            ImportOptionDialog(
                installedApp = selectedApp,
                defaultVmProfile = uiState.defaultVmProfile,
                pendingCustomProfile = uiState.pendingCustomProfile,
                selectedOption = uiState.importOption,
                onSelectOption = onSelectOption,
                onRandomizeCustomProfile = onRandomizeCustomProfile,
                onConfirmImport = onConfirmImport,
                onDismiss = onDismissDialog
            )
        }
    }
}

@Composable
fun InstalledAppRowItem(
    app: InstalledApp,
    isAlreadyImported: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(VirtualSurfaceCard)
            .border(
                BorderStroke(
                    1.dp,
                    if (isAlreadyImported) CyberEmerald.copy(alpha = 0.4f) else VirtualBorder
                ),
                RoundedCornerShape(14.dp)
            )
            .clickable(enabled = !isAlreadyImported, onClick = onClick)
            .padding(12.dp)
            .testTag("app_picker_item_${app.packageName}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App Icon
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (app.appName.contains("Face", ignoreCase = true)) CyberIndigo.copy(alpha = 0.25f)
                    else CyberCyan.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = app.appName,
                tint = if (app.appName.contains("Face", ignoreCase = true)) CyberIndigo else CyberCyan,
                modifier = Modifier.size(26.dp)
            )
        }

        // App Details
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (app.appName.contains("Face", ignoreCase = true)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "CUSTOM PRESET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberAmber
                            )
                        )
                    }
                }
            }

            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Import Status / Action Badge
        if (isAlreadyImported) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberEmerald.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CyberEmerald,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "IMPORTED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberEmerald
                    )
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberCyanDim.copy(alpha = 0.3f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "SELECT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                )
            }
        }
    }
}
