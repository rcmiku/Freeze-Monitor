package com.rcmiku.freeze.monitor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.rcmiku.freeze.monitor.R
import com.rcmiku.freeze.monitor.ui.components.AboutDialog
import com.rcmiku.freeze.monitor.ui.components.AppItem
import com.rcmiku.freeze.monitor.ui.components.RootDialog
import com.rcmiku.freeze.monitor.viewModel.AppListViewModel
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.LazyColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme


@Composable
fun FmApp(viewModel: AppListViewModel) {

    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
    val appList = viewModel.filterApps.collectAsState()
    val context = LocalContext.current
    var expanded by rememberSaveable { mutableStateOf(false) }
    val hazeState = remember { HazeState() }
    val searchValue = viewModel.search.collectAsState()
    val showRootDialog = remember { mutableStateOf(viewModel.rootState.value.not()) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        while (true) {
            delay(timeMillis = 5000L)
            viewModel.updateFilterApps(context)
        }
    }

    val hazeStyleTopAppBar = HazeStyle(
        blurRadius = 25.dp,
        backgroundColor = if (scrollBehavior.state.heightOffset > -1) Color.Transparent else MiuixTheme.colorScheme.background,
        tint = HazeTint(
            MiuixTheme.colorScheme.background.copy(
                if (scrollBehavior.state.heightOffset > -1) 1f
                else lerp(1f, 0.67f, (scrollBehavior.state.heightOffset + 1) / -143f)
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                color = Color.Transparent,
                modifier = Modifier.hazeChild(
                    state = hazeState,
                    style = hazeStyleTopAppBar,
                ) {
                    progressive =
                        HazeProgressive.verticalGradient(startIntensity = 0.6f, endIntensity = 0f)
                },
                title = "",
                largeTitle = stringResource(R.string.app_name),
                scrollBehavior = scrollBehavior,
                actions = {
                    AboutDialog(context = context)
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .windowInsetsPadding(
                    WindowInsets.displayCutout.only(
                        WindowInsetsSides.Horizontal
                    )
                )
                .haze(state = hazeState),
            topAppBarScrollBehavior = scrollBehavior,
            contentPadding = PaddingValues(top = padding.calculateTopPadding())
        ) {
            item {
                SearchBar(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 6.dp),
                    inputField = {
                        InputField(
                            query = searchValue.value,
                            onQueryChange = { viewModel.updateBySearch(it) },
                            onSearch = {
                                keyboardController?.hide()
                            },
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            label = stringResource(R.string.search),
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                                    imageVector = MiuixIcons.Search,
                                    tint = MiuixTheme.colorScheme.onSurfaceContainer,
                                    contentDescription = "Search"
                                )
                            },
                        )
                    },
                    outsideRightAction = {
                        Text(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .clickable(
                                    interactionSource = null,
                                    indication = null
                                ) {
                                    expanded = false
                                    viewModel.updateBySearch("")
                                },
                            text = stringResource(R.string.cancel),
                            color = MiuixTheme.colorScheme.primary
                        )
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {

                }
                SmallTitle(text = stringResource(R.string.app_list))
                if (appList.value.isNotEmpty())
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .windowInsetsPadding(WindowInsets.navigationBars),
                        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        for (app in appList.value) {
                            AppItem(appInfo = app)
                        }
                    }
            }
        }
    }
    RootDialog(showRootDialog)
}