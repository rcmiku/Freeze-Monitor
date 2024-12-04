package com.rcmiku.freeze.monitor.viewModel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rcmiku.freeze.monitor.model.AppInfo
import com.rcmiku.freeze.monitor.util.AppContext
import com.rcmiku.freeze.monitor.util.Process
import com.rcmiku.freeze.monitor.util.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppListViewModel : ViewModel() {

    private val _filterApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _cacheFilterApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _installedApps = MutableStateFlow<List<ApplicationInfo>>(emptyList())
    private val _search = MutableStateFlow("")
    private val _rootState = MutableStateFlow(false)
    private val _showSystemApp = MutableStateFlow(false)
    val rootState: StateFlow<Boolean> = _rootState
    val search: StateFlow<String> = _search
    var cacheFilterApps: StateFlow<List<AppInfo>> = _cacheFilterApps
    val showSystemApp: StateFlow<Boolean> = _showSystemApp
    var filterApps: StateFlow<List<AppInfo>> = _filterApps

    private fun getRootGrantState() {
        _rootState.value = Shell.isGranted()
    }

    private fun autoUpdateCacheFilterApps() {
        viewModelScope.launch {
            combine(_filterApps, _search, _showSystemApp) { apps, search, showSystem ->
                apps.asSequence()
                    .filter { if (showSystem) it.flagSystem else !it.flagSystem }
                    .filter {
                        if (search.isNotEmpty()) it.appName.contains(
                            search,
                            ignoreCase = true
                        ) else true
                    }
                    .toList()
            }.collect { filteredApps ->
                _cacheFilterApps.value = filteredApps
            }
        }
    }

    fun updateByQuery(appName: String, showSystemApp: Boolean) {
        _search.value = appName
        _showSystemApp.value = showSystemApp
    }

    private fun updateInstalledApps(context: Context) {
        viewModelScope.launch {
            _installedApps.value = getInstalledApps(context)
        }
    }

    private fun getInstalledApps(context: Context): List<ApplicationInfo> {
        val packageManager: PackageManager = context.packageManager
        val installedPackages = mutableListOf<ApplicationInfo>()
        val regex = Regex("package:(\\S+)")
        val pmPackageList = Shell.pmPackageList().toString().trimIndent()
        val packageNameList = regex.findAll(pmPackageList).map { it.groupValues[1] }.toList()
        packageNameList.map {
            installedPackages.add(packageManager.getApplicationInfo(it, 0))
        }
        return installedPackages
    }

    private suspend fun getFilterApps(context: Context): List<AppInfo> {
        return withContext(Dispatchers.IO) {
            if (!_rootState.value)
                return@withContext emptyList<AppInfo>()
            val packageManager: PackageManager = context.packageManager
            val apps = mutableListOf<AppInfo>()
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

            apps.addAll(_installedApps.value.asSequence()
                .filter { it.flags and ApplicationInfo.FLAG_PERSISTENT == 0 }
                .filter { Process.countProcess(runningServices, it.packageName) != 0 }
                .map { app ->
                    AppInfo(
                        app.loadLabel(packageManager).toString(),
                        app.packageName,
                        app.loadIcon(packageManager),
                        Process.countProcess(freezeStatus, app.packageName),
                        Process.countProcess(runningServices, app.packageName),
                        Process.sumProcessRes(runningServices, app.packageName),
                        Process.getFreezeType(freezeStateList, app.packageName) ?: "",
                        app.flags and ApplicationInfo.FLAG_SYSTEM == 1
                    )
                })
            apps.sortWith(
                (compareBy(
                    { it.frozenProcess.inv() },
                    { it.appName }))
            )
            apps
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startAutoRefresh() {
        viewModelScope.launch {
            flow {
                emit(Unit)
                while (true) {
                    delay(5_000)
                    emit(Unit)
                }
            }
                .flatMapLatest {
                    flow { emit(getFilterApps(AppContext.context)) }
                }
                .collect { value ->
                    _filterApps.value = value
                }
        }
    }

    init {
        getRootGrantState()
        updateInstalledApps(AppContext.context)
        startAutoRefresh()
        autoUpdateCacheFilterApps()
    }
}