package com.rcmiku.freeze.monitor.viewModel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rcmiku.freeze.monitor.model.AppInfo
import com.rcmiku.freeze.monitor.util.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppListViewModel : ViewModel() {

    private val _filterApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _cacheFilterApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _installedApps = MutableStateFlow<List<ApplicationInfo>>(emptyList())
    private val _freezeType = listOf("V1", "V2", "SIGSTOP")
    var filterApps: StateFlow<List<AppInfo>> = _filterApps
    private val _search = MutableStateFlow("")
    private val _rootState = MutableStateFlow(false)
    val rootState = _rootState
    val search: StateFlow<String> = _search

    fun updateFilterApps(context: Context) {
        viewModelScope.launch {
            _filterApps.value = getFilterApps(context)
        }
    }

    private fun getRootGrantState() {
        _rootState.value = Shell.isGranted()
    }

    fun updateBySearch(string: String) {
        viewModelScope.launch {
            _search.value = string
            _filterApps.value = _cacheFilterApps.value.filter {
                if (search.value.isNotEmpty())
                    it.appName.contains(search.value, ignoreCase = true)
                else
                    true
            }
        }
    }

    fun updateInstalledApps(context: Context) {
        viewModelScope.launch {
            _installedApps.value = getInstalledApps(context)
        }
    }

    private fun getInstalledApps(context: Context): List<ApplicationInfo> {
        val packageManager: PackageManager = context.packageManager
        val installedPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(
                    PackageManager.MATCH_UNINSTALLED_PACKAGES.toLong()
                )
            )
        } else {
            packageManager.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES)
        }
        return installedPackages
    }

    private suspend fun getFilterApps(context: Context): List<AppInfo> {
        return withContext(Dispatchers.IO) {
            if (!_rootState.value)
                return@withContext emptyList<AppInfo>()
            val packageManager: PackageManager = context.packageManager
            var apps = mutableListOf<AppInfo>()
            val freezeStatus = Shell.getFreezeStatus().toString()
            val runningServices =
                Shell.getRunningProcess().toString().lines()
                    .filter { it.isNotBlank() }
                    .map { it.split(" ") }
                    .toList()
            val freezeStateList = freezeStatus.lines()
                .filter { it.isNotBlank() }
                .map { it.split(" ") }
                .toList()

            apps.addAll(_installedApps.value
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                .filter { countProcess(runningServices, it.packageName) != 0 }
                .map { app ->
                    AppInfo(
                        app.loadLabel(packageManager).toString(),
                        app.packageName,
                        app.loadIcon(packageManager),
                        countProcess(freezeStatus, app.packageName),
                        countProcess(runningServices, app.packageName),
                        sumProcessRes(runningServices, app.packageName),
                        getFreezeType(freezeStateList, app.packageName) ?: ""
                    )
                })
            apps.sortWith(
                (compareBy(
                    { it.frozenProcess.inv() },
                    { it.appName }))
            )
            _cacheFilterApps.value = apps
            apps = apps.filter {
                if (search.value.isNotEmpty())
                    it.appName.contains(search.value, ignoreCase = true)
                else
                    true
            }.toMutableList()
            apps
        }
    }

    init {
        getRootGrantState()
    }

    private fun countProcess(source: String, target: String): Int {
        val regex = Regex("${Regex.escape(target)}[:\\s]")
        return regex.findAll(source).count()
    }

    private fun countProcess(source: List<List<String>>, target: String): Int {
        val regex = Regex("${Regex.escape(target)}(:|$)")
        return source.count {
            it.getOrNull(1)?.let { indexValue ->
                regex.containsMatchIn(indexValue)
            } ?: false
        }
    }

    private fun sumProcessRes(source: List<List<String>>, target: String): Int {
        val regex = Regex("${Regex.escape(target)}(:|$)")
        return source.sumOf { row ->
            if (row.getOrNull(1)?.let { regex.containsMatchIn(it) } == true) {
                row.getOrNull(2)?.toIntOrNull() ?: 0
            } else {
                0
            }.div(1024)
        }
    }

    private fun getFreezeType(source: List<List<String>>, target: String): String? {
        val regex = Regex("${Regex.escape(target)}(:|$)")
        for (list in source) {
            if (regex.containsMatchIn(list[1])) {
                return when (list[0]) {
                    "__refrigerator" -> _freezeType[0]
                    "do_freezer_trap" -> _freezeType[1]
                    "get_signal" -> _freezeType[1]
                    "do_signal_stop" -> _freezeType[2]
                    else -> null
                }
            }
        }
        return null
    }
}