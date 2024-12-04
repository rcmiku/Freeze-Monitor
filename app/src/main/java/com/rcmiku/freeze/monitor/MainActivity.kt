package com.rcmiku.freeze.monitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import com.rcmiku.freeze.monitor.ui.FmApp
import com.rcmiku.freeze.monitor.util.AppContext
import com.rcmiku.freeze.monitor.viewModel.AppListViewModel
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        AppContext.init(this)
        val appListViewModel:AppListViewModel by viewModels()
        setContent {
            MiuixTheme(
                colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            ) {
                FmApp(viewModel = appListViewModel)
            }
        }
    }
}