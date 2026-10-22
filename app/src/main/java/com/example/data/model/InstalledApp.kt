package com.example.data.model

import android.graphics.drawable.Drawable

data class InstalledApp(
    val appName: String,
    val packageName: String,
    val icon: Drawable? = null,
    val versionName: String = "1.0",
    val isSystemApp: Boolean = false,
    val isSimulated: Boolean = false
)
