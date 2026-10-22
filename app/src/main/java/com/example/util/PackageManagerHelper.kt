package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.data.model.InstalledApp

object PackageManagerHelper {

    /**
     * Retrieves user-installed applications using Android's PackageManager API.
     */
    fun getInstalledApps(context: Context, includeSystemApps: Boolean = false): List<InstalledApp> {
        val appList = mutableListOf<InstalledApp>()
        val packageManager = context.packageManager

        try {
            // Retrieve all installed applications
            val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            for (packageInfo in packages) {
                val isSystem = (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystem = (packageInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                // Filter out system apps unless explicitly requested
                if (includeSystemApps || (!isSystem || isUpdatedSystem)) {
                    // Avoid listing self
                    if (packageInfo.packageName == context.packageName) continue

                    val appName = try {
                        packageManager.getApplicationLabel(packageInfo).toString()
                    } catch (e: Exception) {
                        packageInfo.packageName
                    }

                    val icon = try {
                        packageManager.getApplicationIcon(packageInfo)
                    } catch (e: Exception) {
                        null
                    }

                    val versionName = try {
                        val pkgInfo = packageManager.getPackageInfo(packageInfo.packageName, 0)
                        pkgInfo.versionName ?: "1.0"
                    } catch (e: Exception) {
                        "1.0"
                    }

                    appList.add(
                        InstalledApp(
                            appName = appName,
                            packageName = packageInfo.packageName,
                            icon = icon,
                            versionName = versionName,
                            isSystemApp = isSystem && !isUpdatedSystem,
                            isSimulated = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Add simulated apps (including Face Editing Pro as specified in prompt)
        // so the user can easily test the Face Editing scenario even on a fresh emulator
        val sampleApps = getSampleUserApps()
        for (sample in sampleApps) {
            if (appList.none { it.packageName == sample.packageName }) {
                appList.add(sample)
            }
        }

        return appList.sortedBy { it.appName.lowercase() }
    }

    private fun getSampleUserApps(): List<InstalledApp> {
        return listOf(
            InstalledApp(
                appName = "Device Info HW",
                packageName = "ru.andr7e.deviceinfohw",
                icon = null,
                versionName = "5.16.2",
                isSystemApp = false,
                isSimulated = true
            ),
            InstalledApp(
                appName = "Face Editing AI Pro",
                packageName = "com.phototech.faceeditor.pro",
                icon = null,
                versionName = "3.4.1",
                isSystemApp = false,
                isSimulated = true
            ),
            InstalledApp(
                appName = "Dual Messenger Plus",
                packageName = "com.social.messenger.dual",
                icon = null,
                versionName = "2.18.0",
                isSystemApp = false,
                isSimulated = true
            ),
            InstalledApp(
                appName = "Crypto Portfolio Vault",
                packageName = "io.crypto.vault.sandbox",
                icon = null,
                versionName = "4.2.0",
                isSystemApp = false,
                isSimulated = true
            ),
            InstalledApp(
                appName = "Secret Game Space",
                packageName = "com.games.secretsandbox",
                icon = null,
                versionName = "1.0.5",
                isSystemApp = false,
                isSimulated = true
            )
        )
    }

    /**
     * Attempts to launch an installed application by package name.
     */
    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
