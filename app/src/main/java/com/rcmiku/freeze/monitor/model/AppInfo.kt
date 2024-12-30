package com.rcmiku.freeze.monitor.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val packageName: String,
    val appIcon: Drawable,
    val appState: AppState,
    val flagSystem: Boolean,
)


data class AppState(
    val frozenProcess: Int,
    val runningProcess: Int,
    val processRes: String,
    val freezeType: String?
)