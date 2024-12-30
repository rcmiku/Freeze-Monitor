package com.rcmiku.freeze.monitor.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.rcmiku.freeze.monitor.model.AppInfo
import com.rcmiku.freeze.monitor.model.AppState
import com.rcmiku.freeze.monitor.util.AppContext.context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Process {

    private val _freezeType = listOf("V1", "V2", "SIGSTOP")
    private lateinit var installedApps: List<ApplicationInfo>

    private fun isRunning(source: List<List<String>>, target: String): Int {
        val regex = Regex("${Regex.escape(target)}(:|$)")
        return source.count {
            it.getOrNull(1)?.let { indexValue ->
                regex.containsMatchIn(indexValue)
            } ?: false
        }
    }

    fun getInstalledApps(): List<ApplicationInfo> {
        val packageManager = context.packageManager
        val regex = Regex("package:(\\S+)")

        val installedPackages = Shell.pmPackageList()
            .toString()
            .trimIndent()
            .let { pmPackageList ->
                regex.findAll(pmPackageList)
                    .map { match -> match.groupValues[1] }
                    .mapNotNull { packageName ->
                        try {
                            packageManager.getApplicationInfo(packageName, 0)
                        } catch (e: PackageManager.NameNotFoundException) {
                            null
                        }
                    }
                    .filter { appInfo ->
                        appInfo.flags and ApplicationInfo.FLAG_PERSISTENT == 0
                    }
                    .toList()
            }

        installedApps = installedPackages.toMutableList()
        return installedPackages
    }


    suspend fun getAppStateList(): MutableList<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager: PackageManager = context.packageManager

        val runningServices = Shell.getRunningProcess()
            .toString()
            .lines()
            .filter { it.isNotBlank() }
            .map { it.split(" ") }

        val runningApps = installedApps.filter { isRunning(runningServices, it.packageName) != 0 }

        val apps = runningApps.map { app ->
            val isSystemApp =
                app.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM
            val appState = processSource(runningServices, app.packageName)

            AppInfo(
                appName = app.loadLabel(packageManager).toString(),
                packageName = app.packageName,
                appIcon = app.loadIcon(packageManager),
                appState = appState,
                flagSystem = isSystemApp
            )
        }.toMutableList()

        apps.sortWith(
            compareBy(
                { it.appState.frozenProcess == 0 },
                { it.appName.lowercase() }
            )
        )

        return@withContext apps
    }


    private fun processSource(source: List<List<String>>, target: String): AppState {
        val regex = Regex("${Regex.escape(target)}(:|$)")
        var count = 0
        var sum = 0
        var freezeType: String? = null
        var freezeCount = 0

        for (row in source) {
            val matches = row.getOrNull(1)?.let { regex.containsMatchIn(it) } == true
            if (matches) {
                count++
                sum += row.getOrNull(2)?.toIntOrNull() ?: 0

                val type = when (row.getOrNull(0)) {
                    "__refrigerator" -> _freezeType[0]
                    "do_freezer_trap", "get_signal" -> _freezeType[1]
                    "do_signal_stop" -> _freezeType[2]
                    else -> null
                }

                if (type != null) {
                    freezeCount++
                    if (freezeType == null) {
                        freezeType = type
                    }
                }
            }
        }

        return AppState(
            frozenProcess = freezeCount,
            runningProcess = count,
            processRes = (sum * 1024).sizeIn(),
            freezeType = freezeType,
        )
    }

    private fun Int.sizeIn(): String {
        return when {
            this < 1000 -> "%d B".format(this)
            this < 1000 * 1000 -> "%d KB".format(this / 1024)
            this < 1000 * 1000 * 1000 -> "%d MB".format(this / (1024 * 1024))
            else -> "%.2f GB".format(this / (1024.0 * 1024 * 1024))
        }
    }

}