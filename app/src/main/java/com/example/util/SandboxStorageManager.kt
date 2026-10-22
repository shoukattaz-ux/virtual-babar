package com.example.util

import android.content.Context
import java.io.File
import java.io.IOException

data class SandboxStats(
    val path: String,
    val fileCount: Int,
    val totalSizeBytes: Long,
    val exists: Boolean
)

/**
 * Manages isolated storage directories for each virtual machine profile.
 * Ensures each cloned/imported app has its own separate file storage directory
 * so its data doesn't mix with the host app or other clones.
 */
object SandboxStorageManager {

    /**
     * Creates and returns a private isolated directory for a specific profile ID.
     */
    fun getProfileSandboxDir(context: Context, profileId: String): File {
        val rootDir = context.filesDir
        val sandboxDir = File(rootDir, "virtual_sandboxes/$profileId")
        if (!sandboxDir.exists()) {
            sandboxDir.mkdirs() // Create the folder if it doesn't exist
        }
        return sandboxDir
    }

    /**
     * Returns an isolated databases path for a specific profile.
     */
    fun getProfileDatabasePath(context: Context, profileId: String, dbName: String): File {
        val sandboxDir = getProfileSandboxDir(context, profileId)
        val dbDir = File(sandboxDir, "databases")
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }
        return File(dbDir, dbName)
    }

    /**
     * Returns an isolated shared_prefs directory for a specific profile.
     */
    fun getProfileSharedPrefsDir(context: Context, profileId: String): File {
        val sandboxDir = getProfileSandboxDir(context, profileId)
        val prefsDir = File(sandboxDir, "shared_prefs")
        if (!prefsDir.exists()) {
            prefsDir.mkdirs()
        }
        return prefsDir
    }

    /**
     * Returns an isolated cache directory for a specific profile.
     */
    fun getProfileCacheDir(context: Context, profileId: String): File {
        val sandboxDir = getProfileSandboxDir(context, profileId)
        val cacheDir = File(sandboxDir, "cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir
    }

    /**
     * Returns storage statistics for a specific profile's sandbox.
     */
    fun getSandboxUsageStats(context: Context, profileId: String): SandboxStats {
        val dir = getProfileSandboxDir(context, profileId)
        var count = 0
        var totalBytes = 0L

        dir.walkTopDown().forEach { file ->
            if (file.isFile) {
                count++
                totalBytes += file.length()
            }
        }

        return SandboxStats(
            path = dir.absolutePath,
            fileCount = count,
            totalSizeBytes = totalBytes,
            exists = dir.exists()
        )
    }

    /**
     * Creates a sample file inside the profile's sandbox for testing/demonstration.
     */
    fun createSampleSandboxFile(
        context: Context,
        profileId: String,
        fileName: String = "sandbox_env.json",
        content: String = "{ \"isolated\": true, \"timestamp\": ${System.currentTimeMillis()} }"
    ): File {
        val sandboxDir = getProfileSandboxDir(context, profileId)
        val file = File(sandboxDir, fileName)
        file.writeText(content)
        return file
    }

    /**
     * Lists all files within the profile's sandbox directory.
     */
    fun listSandboxFiles(context: Context, profileId: String): List<String> {
        val sandboxDir = getProfileSandboxDir(context, profileId)
        val files = mutableListOf<String>()
        sandboxDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                files.add(file.relativeTo(sandboxDir).path)
            }
        }
        return files
    }

    /**
     * Wipes all contents of an isolated profile sandbox and recreates empty structure.
     */
    fun clearProfileSandbox(context: Context, profileId: String): Boolean {
        val sandboxDir = getProfileSandboxDir(context, profileId)
        return sandboxDir.deleteRecursively().also {
            sandboxDir.mkdirs()
        }
    }

    /**
     * Completely purges and deletes the dedicated isolated sandbox folder
     * (/virtual_sandboxes/{profileId}/) recursively without recreating it.
     */
    fun purgeProfileSandbox(context: Context, profileId: String): Boolean {
        val rootDir = context.filesDir
        val sandboxDir = File(rootDir, "virtual_sandboxes/$profileId")
        return if (sandboxDir.exists()) {
            sandboxDir.deleteRecursively()
        } else {
            true
        }
    }
}
