package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.example.ui.screens.AppPickerScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.NetworkShifterScreen
import com.example.ui.screens.SensorShieldScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VirtualDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VirtualBgDark
import com.example.ui.viewmodel.VirtualSpaceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VirtualBgDark
                ) {
                    VirtualSpaceApp()
                }
            }
        }
    }
}

@Composable
fun VirtualSpaceApp(
    viewModel: VirtualSpaceViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            VirtualDashboardScreen(
                uiState = uiState,
                onNavigateToAppPicker = { navController.navigate("app_picker") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToDiagnostics = { navController.navigate("diagnostics") },
                onNavigateToSensorShield = { navController.navigate("sensor_shield") },
                onNavigateToNetworkShifter = { navController.navigate("network_shifter") },
                onRandomizeMasterVm = { viewModel.randomizeMasterVmProfile() },
                onOpenAppContainer = { viewModel.openAppContainer(it) },
                onCloseAppContainer = { viewModel.closeAppContainer() },
                onLaunchApp = { ctx, item -> viewModel.launchInSandbox(ctx, item) },
                onRandomizeAppIdentity = { item -> viewModel.randomizeAppInContainer(item) },
                onToggleAppProfileMode = { item -> viewModel.toggleAppProfileMode(item) },
                onContainerCreateTestFile = { item -> viewModel.createSampleSandboxFile(item.profile?.profileId ?: item.app.assignedProfileId) },
                onContainerClearSandbox = { item -> viewModel.clearSandboxStorage(item.profile?.profileId ?: item.app.assignedProfileId) },
                onRemoveApp = { item -> viewModel.removeAppFromVirtualSpace(context, item) },
                onDismissNotification = { viewModel.clearStatusNotification() }
            )
        }

        composable("app_picker") {
            AppPickerScreen(
                uiState = uiState,
                onNavigateBack = {
                    viewModel.dismissImportDialog()
                    navController.popBackStack()
                },
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onToggleIncludeSystemApps = { context -> viewModel.toggleIncludeSystemApps(context) },
                onSelectAppForImport = { app -> viewModel.selectAppForImport(app) },
                onDismissDialog = { viewModel.dismissImportDialog() },
                onSelectOption = { option -> viewModel.setImportOption(option) },
                onRandomizeCustomProfile = { viewModel.randomizePendingCustomProfile() },
                onConfirmImport = {
                    viewModel.confirmImport {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() },
                onRandomizeMasterVm = { viewModel.randomizeMasterVmProfile() },
                onRandomizeProfile = { profile -> viewModel.randomizeExistingProfile(profile) },
                onCreateNewProfile = { name -> viewModel.createNewRandomProfile(name) },
                onDeleteProfile = { profile -> viewModel.deleteProfile(profile) }
            )
        }

        composable("diagnostics") {
            DiagnosticsScreen(
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSensorShield = { navController.navigate("sensor_shield") }
            )
        }

        composable("sensor_shield") {
            SensorShieldScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("network_shifter") {
            NetworkShifterScreen(
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
