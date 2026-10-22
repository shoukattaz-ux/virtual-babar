package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ImportedAppEntity
import com.example.data.model.InstalledApp
import com.example.data.model.ProfileEntity
import com.example.data.repository.VirtualSpaceRepository
import com.example.ui.screens.VirtualSandboxActivity
import com.example.util.BuildSpoofer
import com.example.util.DeviceProfileGenerator
import com.example.util.PackageManagerHelper
import com.example.util.SandboxStats
import com.example.util.SandboxStorageManager
import com.example.util.SensorJitterShieldManager
import com.example.util.SpoofedEnvironmentState
import com.example.util.VirtualHookManager
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ImportOption {
    USE_VM_IDENTITY,       // Option A: Use Virtual Machine Identity (Inherit main VM profile)
    CREATE_CUSTOM_IDENTITY // Option B: Create Unique / Custom Identity (Generate a separate profile)
}

data class ImportedAppWithProfile(
    val app: ImportedAppEntity,
    val profile: ProfileEntity?
)

data class VirtualSpaceUiState(
    val importedApps: List<ImportedAppWithProfile> = emptyList(),
    val profiles: List<ProfileEntity> = emptyList(),
    val defaultVmProfile: ProfileEntity? = null,
    val installedApps: List<InstalledApp> = emptyList(),
    val filteredInstalledApps: List<InstalledApp> = emptyList(),
    val isLoadingApps: Boolean = false,
    val searchQuery: String = "",
    val includeSystemApps: Boolean = false,
    val selectedAppForImport: InstalledApp? = null,
    val importOption: ImportOption = ImportOption.USE_VM_IDENTITY,
    val pendingCustomProfile: ProfileEntity? = null,
    val activeAppInContainer: ImportedAppWithProfile? = null,
    val isVirtualLaunching: Boolean = false,
    val sandboxStats: SandboxStats? = null,
    val sandboxFiles: List<String> = emptyList(),
    val spoofedEnvironment: SpoofedEnvironmentState? = null,
    val statusNotification: String? = null
)

class VirtualSpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VirtualSpaceRepository

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val _isLoadingApps = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _includeSystemApps = MutableStateFlow(false)

    private val _selectedAppForImport = MutableStateFlow<InstalledApp?>(null)
    private val _importOption = MutableStateFlow(ImportOption.USE_VM_IDENTITY)
    private val _pendingCustomProfile = MutableStateFlow<ProfileEntity?>(null)

    private val _activeAppInContainer = MutableStateFlow<ImportedAppWithProfile?>(null)
    private val _isVirtualLaunching = MutableStateFlow(false)
    private val _sandboxStats = MutableStateFlow<SandboxStats?>(null)
    private val _sandboxFiles = MutableStateFlow<List<String>>(emptyList())
    private val _spoofedEnvironment = MutableStateFlow<SpoofedEnvironmentState?>(null)
    private val _statusNotification = MutableStateFlow<String?>(null)

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = VirtualSpaceRepository(database.profileDao(), database.importedAppDao())

        // Option 3: Initialize Behavioral & Sensor Jitter Shield Manager
        SensorJitterShieldManager.init(application, viewModelScope)

        // Ensure default VM profile exists immediately and initialize its sandbox
        viewModelScope.launch {
            val defaultProfile = repository.ensureDefaultVmProfile()
            withContext(Dispatchers.IO) {
                SandboxStorageManager.getProfileSandboxDir(application, defaultProfile.profileId.toString())
                SandboxStorageManager.createSampleSandboxFile(
                    application,
                    defaultProfile.profileId.toString(),
                    "vm_host_config.json",
                    "{ \"vm_initialized\": true, \"profile\": \"${defaultProfile.profileName}\" }"
                )
            }
            loadInstalledApps(application)
        }
    }

    val uiState: StateFlow<VirtualSpaceUiState> = combine(
        combine(
            repository.allProfiles,
            repository.allImportedApps,
            _installedApps,
            _searchQuery,
            _includeSystemApps
        ) { profiles, importedApps, installed, query, includeSys ->
            StateFirstHalf(profiles, importedApps, installed, query, includeSys)
        },
        combine(
            _selectedAppForImport,
            _importOption,
            _pendingCustomProfile,
            _activeAppInContainer,
            _isVirtualLaunching
        ) { selectedImport, opt, pendingCustom, activeContainer, isLaunching ->
            StateSecondHalf(selectedImport, opt, pendingCustom, activeContainer, isLaunching)
        },
        combine(
            _sandboxStats,
            _sandboxFiles,
            _spoofedEnvironment,
            _statusNotification
        ) { stats, files, spoofed, statusMsg ->
            StateThirdHalf(stats, files, spoofed, statusMsg)
        }
    ) { first, second, third ->
        val defaultVm = first.profiles.find { it.isDefaultVm }

        val appsWithProfile = first.importedApps.map { app ->
            val assigned = first.profiles.find { it.profileId == app.assignedProfileId } ?: defaultVm
            ImportedAppWithProfile(app, assigned)
        }

        val filtered = first.installed.filter { app ->
            first.query.isBlank() ||
                app.appName.contains(first.query, ignoreCase = true) ||
                app.packageName.contains(first.query, ignoreCase = true)
        }

        VirtualSpaceUiState(
            importedApps = appsWithProfile,
            profiles = first.profiles,
            defaultVmProfile = defaultVm,
            installedApps = first.installed,
            filteredInstalledApps = filtered,
            isLoadingApps = false,
            searchQuery = first.query,
            includeSystemApps = first.includeSys,
            selectedAppForImport = second.selectedImport,
            importOption = second.opt,
            pendingCustomProfile = second.pendingCustom,
            activeAppInContainer = second.activeContainer,
            isVirtualLaunching = second.isLaunching,
            sandboxStats = third.stats,
            sandboxFiles = third.files,
            spoofedEnvironment = third.spoofed,
            statusNotification = third.statusMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VirtualSpaceUiState(isLoadingApps = true)
    )

    private data class StateFirstHalf(
        val profiles: List<ProfileEntity>,
        val importedApps: List<ImportedAppEntity>,
        val installed: List<InstalledApp>,
        val query: String,
        val includeSys: Boolean
    )

    private data class StateSecondHalf(
        val selectedImport: InstalledApp?,
        val opt: ImportOption,
        val pendingCustom: ProfileEntity?,
        val activeContainer: ImportedAppWithProfile?,
        val isLaunching: Boolean
    )

    private data class StateThirdHalf(
        val stats: SandboxStats?,
        val files: List<String>,
        val spoofed: SpoofedEnvironmentState?,
        val statusMsg: String?
    )

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            _isLoadingApps.value = true
            val apps = withContext(Dispatchers.IO) {
                PackageManagerHelper.getInstalledApps(context, _includeSystemApps.value)
            }
            _installedApps.value = apps
            _isLoadingApps.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleIncludeSystemApps(context: Context) {
        _includeSystemApps.value = !_includeSystemApps.value
        loadInstalledApps(context)
    }

    fun selectAppForImport(app: InstalledApp) {
        _selectedAppForImport.value = app
        _importOption.value = ImportOption.USE_VM_IDENTITY
        _pendingCustomProfile.value = DeviceProfileGenerator.generateRandomProfileEntity("Custom_${app.appName}")
    }

    fun dismissImportDialog() {
        _selectedAppForImport.value = null
        _pendingCustomProfile.value = null
    }

    fun setImportOption(option: ImportOption) {
        _importOption.value = option
    }

    fun randomizePendingCustomProfile() {
        val currentApp = _selectedAppForImport.value ?: return
        _pendingCustomProfile.value = DeviceProfileGenerator.generateRandomProfileEntity("Custom_${currentApp.appName}")
    }

    /**
     * Logic for Option A vs Option B with Sandbox Directory Initialization:
     * - Option A: Links app to current Master VM profile and sets up shared VM sandbox directory.
     * - Option B: Creates new profile via DeviceProfileGenerator and creates isolated sandbox folder.
     */
    fun confirmImport(onComplete: () -> Unit) {
        val app = _selectedAppForImport.value ?: return
        val option = _importOption.value
        val context = getApplication<Application>()

        viewModelScope.launch {
            try {
                val assignedProfileId: Long
                if (option == ImportOption.USE_VM_IDENTITY) {
                    assignedProfileId = repository.importAppWithVmProfile(
                        packageName = app.packageName,
                        appName = app.appName,
                        isSimulated = app.isSimulated
                    )
                    _statusNotification.value = "Imported '${app.appName}' into Master VM Sandbox"
                } else {
                    val customProfile = _pendingCustomProfile.value
                        ?: DeviceProfileGenerator.generateRandomProfileEntity("Custom_${app.appName}")
                    assignedProfileId = repository.importAppWithCustomProfile(
                        packageName = app.packageName,
                        appName = app.appName,
                        customProfile = customProfile,
                        isSimulated = app.isSimulated
                    )
                    _statusNotification.value = "Imported '${app.appName}' with Unique Isolated Sandbox"
                }

                // Phase 3: Prepare sandbox storage directories for this imported app
                withContext(Dispatchers.IO) {
                    val profile = repository.getProfileById(assignedProfileId)
                    val pId = (profile?.profileId ?: assignedProfileId).toString()
                    SandboxStorageManager.getProfileSandboxDir(context, pId)
                    SandboxStorageManager.getProfileDatabasePath(context, pId, "${app.appName.lowercase().replace(" ", "_")}.db")
                    SandboxStorageManager.getProfileSharedPrefsDir(context, pId)
                    SandboxStorageManager.createSampleSandboxFile(
                        context,
                        pId,
                        "app_meta.json",
                        "{ \"app\": \"${app.appName}\", \"package\": \"${app.packageName}\", \"profile_id\": \"$pId\" }"
                    )
                }

                dismissImportDialog()
                onComplete()
            } catch (e: Exception) {
                _statusNotification.value = "Failed to import: ${e.message}"
            }
        }
    }

    /**
     * Requirement: Full Deletion Routine (removeAppFromVirtualSpace)
     * 1. Removes the application's entry from the local SQLite/Room profile database.
     * 2. Clears and deletes the dedicated isolated sandbox folder (/virtual_sandboxes/{profileId}/)
     *    recursively from local internal storage.
     * 3. Dynamically updates the Virtual Dashboard UI to remove the app tile upon completion.
     */
    fun removeAppFromVirtualSpace(context: Context, appWithProfile: ImportedAppWithProfile) {
        val app = appWithProfile.app
        val profileId = (appWithProfile.profile?.profileId ?: app.assignedProfileId).toString()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // 1. Delete dedicated isolated sandbox folder recursively from local internal storage
                val rootDir = context.filesDir
                val sandboxDir = File(rootDir, "virtual_sandboxes/$profileId")
                if (sandboxDir.exists()) {
                    sandboxDir.deleteRecursively()
                }
                val pkgDir = File(rootDir, "virtual_sandboxes/${app.packageName}")
                if (pkgDir.exists()) {
                    pkgDir.deleteRecursively()
                }
                SandboxStorageManager.purgeProfileSandbox(context, profileId)

                // 2. Remove application entry from local SQLite/Room database
                repository.removeImportedApp(app)

                // If custom dedicated profile, also delete the profile row
                if (app.isCustomIdentity && appWithProfile.profile != null) {
                    repository.deleteProfile(appWithProfile.profile)
                }
            }

            // 3. Dynamically update Virtual Dashboard UI to remove app tile upon completion
            if (_activeAppInContainer.value?.app?.id == app.id) {
                _activeAppInContainer.value = null
                _sandboxStats.value = null
                _sandboxFiles.value = emptyList()
            }
            _statusNotification.value = "Purged '${app.appName}' from Virtual Space & deleted sandbox files."
        }
    }

    fun removeAppFromVirtualSpace(context: Context, app: ImportedAppEntity) {
        val profileId = app.assignedProfileId.toString()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val rootDir = context.filesDir
                val sandboxDir = File(rootDir, "virtual_sandboxes/$profileId")
                if (sandboxDir.exists()) {
                    sandboxDir.deleteRecursively()
                }
                val pkgDir = File(rootDir, "virtual_sandboxes/${app.packageName}")
                if (pkgDir.exists()) {
                    pkgDir.deleteRecursively()
                }
                SandboxStorageManager.purgeProfileSandbox(context, profileId)
                repository.removeImportedApp(app)
            }
            if (_activeAppInContainer.value?.app?.id == app.id) {
                _activeAppInContainer.value = null
                _sandboxStats.value = null
                _sandboxFiles.value = emptyList()
            }
            _statusNotification.value = "Purged '${app.appName}' from Virtual Space & deleted sandbox files."
        }
    }

    fun removeImportedApp(app: ImportedAppEntity) {
        val context = getApplication<Application>()
        removeAppFromVirtualSpace(context, app)
    }

    fun openAppContainer(appWithProfile: ImportedAppWithProfile) {
        _activeAppInContainer.value = appWithProfile
        refreshSandboxStats(appWithProfile)
    }

    fun closeAppContainer() {
        _activeAppInContainer.value = null
        _sandboxStats.value = null
        _sandboxFiles.value = emptyList()
    }

    private fun refreshSandboxStats(appWithProfile: ImportedAppWithProfile) {
        val context = getApplication<Application>()
        val profileId = (appWithProfile.profile?.profileId ?: appWithProfile.app.assignedProfileId).toString()

        viewModelScope.launch {
            val (stats, files) = withContext(Dispatchers.IO) {
                val dir = SandboxStorageManager.getProfileSandboxDir(context, profileId)
                val existingFiles = SandboxStorageManager.listSandboxFiles(context, profileId)
                if (existingFiles.isEmpty()) {
                    SandboxStorageManager.createSampleSandboxFile(
                        context,
                        profileId,
                        "sandbox_env.json",
                        "{ \"profile\": \"${appWithProfile.profile?.profileName}\", \"isolated\": true }"
                    )
                }
                val updatedStats = SandboxStorageManager.getSandboxUsageStats(context, profileId)
                val updatedFiles = SandboxStorageManager.listSandboxFiles(context, profileId)
                Pair(updatedStats, updatedFiles)
            }
            _sandboxStats.value = stats
            _sandboxFiles.value = files
        }
    }

    /**
     * Requirement: Launching Function (launchInSandbox) including environment injection:
     * - Hooks android.os.Build fields and system properties dynamically
     * - Injects Settings.Secure.ANDROID_ID and MAC address spoofing
     * - Mounts and provisions isolated sandbox storage directory (/virtual_sandboxes/{profileId}/)
     * - Launches VirtualSandboxActivity running in the hooked virtual environment
     */
    fun launchInSandbox(context: Context, appWithProfile: ImportedAppWithProfile) {
        val profile = appWithProfile.profile ?: return
        viewModelScope.launch {
            _isVirtualLaunching.value = true

            // 1. Process-Level Environment Injection: Hook system properties, Build parameters, and system services
            withContext(Dispatchers.IO) {
                VirtualHookManager.installProcessHooks(context, profile)
                // Ensure storage directories are mounted
                SandboxStorageManager.getProfileSandboxDir(context, profile.profileId.toString())
                SandboxStorageManager.getProfileDatabasePath(context, profile.profileId.toString(), "app.db")
            }

            _spoofedEnvironment.value = BuildSpoofer.getActiveState()
            repository.incrementLaunchCount(appWithProfile.app.packageName)

            // Close container modal so user transitions straight into the running app
            _activeAppInContainer.value = null

            // 2. Launch in Virtual Container with wrapped environment
            val intent = VirtualSandboxActivity.createIntent(
                context = context,
                packageName = appWithProfile.app.packageName,
                appName = appWithProfile.app.appName,
                profile = profile,
                isSimulated = appWithProfile.app.isSimulated
            )
            context.startActivity(intent)

            _statusNotification.value = "Launched '${appWithProfile.app.appName}' in Container (${profile.deviceBrand} ${profile.deviceModel})"
            kotlinx.coroutines.delay(400)
            _isVirtualLaunching.value = false
        }
    }

    /**
     * Backward-compatible alias for launchInSandbox.
     */
    fun launchAppFromContainer(context: Context, appWithProfile: ImportedAppWithProfile) {
        launchInSandbox(context, appWithProfile)
    }

    fun createSampleSandboxFile(profileId: Long) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                SandboxStorageManager.createSampleSandboxFile(
                    context,
                    profileId.toString(),
                    "data_${System.currentTimeMillis() % 1000}.dat",
                    "Payload: Isolated sandbox session data at ${System.currentTimeMillis()}"
                )
            }
            _activeAppInContainer.value?.let { refreshSandboxStats(it) }
            _statusNotification.value = "Created test file in sandbox #$profileId"
        }
    }

    fun clearSandboxStorage(profileId: Long) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                SandboxStorageManager.clearProfileSandbox(context, profileId.toString())
            }
            _activeAppInContainer.value?.let { refreshSandboxStats(it) }
            _statusNotification.value = "Cleared sandbox storage for profile #$profileId"
        }
    }

    fun randomizeMasterVmProfile() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            val updated = repository.randomizeMasterVmProfile()
            withContext(Dispatchers.IO) {
                BuildSpoofer.spoofBuildProperties(
                    brand = updated.deviceBrand,
                    model = updated.deviceModel,
                    serial = updated.serialNumber,
                    androidId = updated.androidId,
                    macAddress = updated.macAddress
                )
            }
            _spoofedEnvironment.value = BuildSpoofer.getActiveState()
            _statusNotification.value = "Master VM Identity Randomized: ${updated.deviceBrand} ${updated.deviceModel}"
        }
    }

    fun randomizeExistingProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            val updated = repository.randomizeProfile(profile)
            _statusNotification.value = "Randomized Identity for '${profile.profileName}': ${updated.deviceBrand} ${updated.deviceModel}"
        }
    }

    fun randomizeAppInContainer(appWithProfile: ImportedAppWithProfile) {
        val profile = appWithProfile.profile ?: return
        viewModelScope.launch {
            val updated = repository.randomizeProfile(profile)
            _activeAppInContainer.value = _activeAppInContainer.value?.copy(profile = updated)
            _statusNotification.value = "Randomized Identity for '${appWithProfile.app.appName}' (${updated.deviceBrand} ${updated.deviceModel})"
            refreshSandboxStats(appWithProfile.copy(profile = updated))
        }
    }

    fun toggleAppProfileMode(appWithProfile: ImportedAppWithProfile) {
        val app = appWithProfile.app
        val context = getApplication<Application>()
        viewModelScope.launch {
            val defaultVm = repository.getDefaultVmProfile() ?: return@launch
            if (app.isCustomIdentity) {
                val oldProfileId = app.assignedProfileId
                repository.updateAppProfile(app.id, defaultVm.profileId, isCustom = false)
                repository.deleteProfile(ProfileEntity(profileId = oldProfileId, profileName = "", androidId = "", deviceModel = "", deviceBrand = "", macAddress = "", serialNumber = "", isDefaultVm = false))
                withContext(Dispatchers.IO) {
                    SandboxStorageManager.clearProfileSandbox(context, oldProfileId.toString())
                }
                val updatedApp = app.copy(assignedProfileId = defaultVm.profileId, isCustomIdentity = false)
                val newItem = ImportedAppWithProfile(updatedApp, defaultVm)
                _activeAppInContainer.value = newItem
                _statusNotification.value = "Switched '${app.appName}' to Master VM Profile"
                refreshSandboxStats(newItem)
            } else {
                val newCustom = DeviceProfileGenerator.generateRandomProfileEntity("Custom_${app.appName}", isDefaultVm = false)
                val newId = repository.insertProfile(newCustom)
                val savedProfile = newCustom.copy(profileId = newId)
                repository.updateAppProfile(app.id, newId, isCustom = true)
                val updatedApp = app.copy(assignedProfileId = newId, isCustomIdentity = true)
                val newItem = ImportedAppWithProfile(updatedApp, savedProfile)
                _activeAppInContainer.value = newItem
                _statusNotification.value = "Switched '${app.appName}' to Unique Identity Profile"
                refreshSandboxStats(newItem)
            }
        }
    }

    fun createNewRandomProfile(profileName: String) {
        viewModelScope.launch {
            val profile = repository.createRandomProfile(profileName)
            _statusNotification.value = "Generated new Profile: '${profile.profileName}' (${profile.deviceBrand} ${profile.deviceModel})"
        }
    }

    fun updateProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
            _statusNotification.value = "Updated Profile '${profile.profileName}'"
        }
    }

    fun deleteProfile(profile: ProfileEntity) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            if (!profile.isDefaultVm) {
                repository.deleteProfile(profile)
                withContext(Dispatchers.IO) {
                    SandboxStorageManager.clearProfileSandbox(context, profile.profileId.toString())
                }
                _statusNotification.value = "Deleted profile '${profile.profileName}'"
            }
        }
    }

    fun clearStatusNotification() {
        _statusNotification.value = null
    }
}
