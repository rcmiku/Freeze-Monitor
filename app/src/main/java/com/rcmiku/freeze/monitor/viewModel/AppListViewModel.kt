package com.rcmiku.freeze.monitor.viewModel

import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rcmiku.freeze.monitor.model.AppInfo
import com.rcmiku.freeze.monitor.util.Process
import com.rcmiku.freeze.monitor.util.Shell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

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

    private fun updateInstalledApps() {
        _installedApps.value = Process.getInstalledApps()
    }

    fun getFilterApps() {
        viewModelScope.launch {
            _filterApps.value = Process.getAppStateList()
        }
    }

    init {
        getRootGrantState()
        updateInstalledApps()
        autoUpdateCacheFilterApps()
    }
}