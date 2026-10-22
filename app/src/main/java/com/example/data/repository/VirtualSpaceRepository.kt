package com.example.data.repository

import com.example.data.db.ImportedAppDao
import com.example.data.db.ProfileDao
import com.example.data.model.ImportedAppEntity
import com.example.data.model.ProfileEntity
import kotlinx.coroutines.flow.Flow

class VirtualSpaceRepository(
    private val profileDao: ProfileDao,
    private val importedAppDao: ImportedAppDao
) {
    val allProfiles: Flow<List<ProfileEntity>> = profileDao.getAllProfiles()
    val allImportedApps: Flow<List<ImportedAppEntity>> = importedAppDao.getAllImportedApps()

    suspend fun ensureDefaultVmProfile(): ProfileEntity {
        val existing = profileDao.getDefaultVmProfile()
        if (existing != null) return existing

        val defaultVm = ProfileEntity(
            profileName = "Virtual Machine Main Profile",
            androidId = "8f4b1a20c93e47d2",
            deviceModel = "Pixel 8 Pro",
            deviceBrand = "Google",
            macAddress = "74:D0:2B:6C:8E:19",
            serialNumber = "ZX1G224H89B",
            isDefaultVm = true
        )
        val id = profileDao.insertProfile(defaultVm)
        return defaultVm.copy(profileId = id)
    }

    suspend fun getDefaultVmProfile(): ProfileEntity? {
        return profileDao.getDefaultVmProfile()
    }

    suspend fun getProfileById(id: Long): ProfileEntity? {
        return profileDao.getProfileById(id)
    }

    suspend fun importAppWithVmProfile(
        packageName: String,
        appName: String,
        isSimulated: Boolean = false
    ): Long {
        val defaultProfile = ensureDefaultVmProfile()
        val importedApp = ImportedAppEntity(
            packageName = packageName,
            appName = appName,
            assignedProfileId = defaultProfile.profileId,
            isCustomIdentity = false,
            isSimulated = isSimulated
        )
        return importedAppDao.insertImportedApp(importedApp)
    }

    suspend fun importAppWithCustomProfile(
        packageName: String,
        appName: String,
        customProfile: ProfileEntity,
        isSimulated: Boolean = false
    ): Long {
        val createdProfileId = profileDao.insertProfile(customProfile.copy(isDefaultVm = false))
        val importedApp = ImportedAppEntity(
            packageName = packageName,
            appName = appName,
            assignedProfileId = createdProfileId,
            isCustomIdentity = true,
            isSimulated = isSimulated
        )
        return importedAppDao.insertImportedApp(importedApp)
    }

    suspend fun removeImportedApp(app: ImportedAppEntity) {
        importedAppDao.deleteImportedApp(app)
        // If it was a custom identity profile created specifically for this app, clean up the profile
        if (app.isCustomIdentity) {
            profileDao.deleteProfileById(app.assignedProfileId)
        }
    }

    suspend fun updateAppProfile(appId: Long, newProfileId: Long, isCustom: Boolean) {
        importedAppDao.updateAssignedProfile(appId, newProfileId, isCustom)
    }

    suspend fun insertProfile(profile: ProfileEntity): Long {
        return profileDao.insertProfile(profile)
    }

    suspend fun randomizeMasterVmProfile(): ProfileEntity {
        val currentMaster = ensureDefaultVmProfile()
        val randomModel = com.example.util.DeviceProfileGenerator.generateRandomProfile("Virtual Machine Main Profile")
        val updated = currentMaster.copy(
            androidId = randomModel.androidId,
            deviceBrand = randomModel.deviceBrand,
            deviceModel = randomModel.deviceModel,
            serialNumber = randomModel.serialNumber,
            macAddress = randomModel.macAddress
        )
        profileDao.updateProfile(updated)
        return updated
    }

    suspend fun randomizeProfile(profile: ProfileEntity): ProfileEntity {
        val randomModel = com.example.util.DeviceProfileGenerator.generateRandomProfile(profile.profileName)
        val updated = profile.copy(
            androidId = randomModel.androidId,
            deviceBrand = randomModel.deviceBrand,
            deviceModel = randomModel.deviceModel,
            serialNumber = randomModel.serialNumber,
            macAddress = randomModel.macAddress
        )
        profileDao.updateProfile(updated)
        return updated
    }

    suspend fun createRandomProfile(profileName: String): ProfileEntity {
        val newEntity = com.example.util.DeviceProfileGenerator.generateRandomProfileEntity(profileName, isDefaultVm = false)
        val id = profileDao.insertProfile(newEntity)
        return newEntity.copy(profileId = id)
    }

    suspend fun updateProfile(profile: ProfileEntity) {
        profileDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: ProfileEntity) {
        if (!profile.isDefaultVm) {
            profileDao.deleteProfile(profile)
        }
    }

    suspend fun incrementLaunchCount(packageName: String) {
        importedAppDao.incrementLaunchCount(packageName)
    }
}
