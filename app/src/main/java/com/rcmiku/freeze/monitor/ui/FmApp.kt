package com.rcmiku.freeze.monitor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.rcmiku.freeze.monitor.R
import com.rcmiku.freeze.monitor.ui.components.AboutDialog
import com.rcmiku.freeze.monitor.ui.components.AppItem
import com.rcmiku.freeze.monitor.ui.components.RootDialog
import com.rcmiku.freeze.monitor.viewModel.AppListViewModel
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.LazyColumn
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.extra.DropdownImpl
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.ImmersionMore
import top.yukonga.miuix.kmp.icon.icons.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissPopup


@OptIn(ExperimentalHazeApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FmApp(viewModel: AppListViewModel) {

    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
    val appList = viewModel.cacheFilterApps.collectAsState()
    var expanded by rememberSaveable { mutableStateOf(false) }
    val hazeState = remember { HazeState() }
    val searchValue = viewModel.search.collectAsState()
    val showRootDialog = remember { mutableStateOf(viewModel.rootState.value.not()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isTopPopupExpanded = remember { mutableStateOf(false) }
    val showTopPopup = remember { mutableStateOf(false) }
    val showAboutDialog = remember { mutableStateOf(false) }
    val showSystemApp = viewModel.showSystemApp.collectAsState()
    val dropdownOptions = stringArrayResource(R.array.dropdownOptions)
    val isLoading = viewModel.filterApps.collectAsState()

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
                    inputScale = HazeInputScale.Auto
                },
                title = "",
                largeTitle = stringResource(R.string.app_name),
                scrollBehavior = scrollBehavior,
                actions = {
                    if (isTopPopupExpanded.value) {
                        ListPopup(
                            show = showTopPopup,
                            popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
                            alignment = PopupPositionProvider.Align.TopRight,
                            onDismissRequest = {
                                isTopPopupExpanded.value = false
                            }
                        ) {
                            ListPopupColumn {
                                dropdownOptions.forEachIndexed { index, text ->
                                    DropdownImpl(
                                        text = text,
                                        optionSize = dropdownOptions.size,
                                        isSelected = when (index) {
                                            0 -> !showSystemApp.value
                                            1 -> showSystemApp.value
                                            else -> false
                                        },
                                        onSelectedIndexChange = {
                                            dismissPopup(showTopPopup)
                                            isTopPopupExpanded.value = false

                                            when (index) {
                                                0 -> viewModel.updateByQuery(
                                                    searchValue.value,
                                                    false
                                                )

                                                1 -> viewModel.updateByQuery(
                                                    searchValue.value,
                                                    true
                                                )

                                                2 -> showAboutDialog.value = true
                                            }
                                        },
                                        index = index
                                    )
                                }
                            }
                        }
                        showTopPopup.value = true
                    }

                    IconButton(
                        modifier = Modifier
                            .padding(end = 21.dp)
                            .size(40.dp),
                        onClick = {
                            isTopPopupExpanded.value = true
                        }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.ImmersionMore,
                            contentDescription = "Menu"
                        )
                    }
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
                            onQueryChange = { viewModel.updateByQuery(it, showSystemApp.value) },
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
                                    viewModel.updateByQuery("", showSystemApp.value)
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
        if (isLoading.value.isEmpty())
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { LoadingIndicator(color = MiuixTheme.colorScheme.onPrimaryVariant) }
    }
    AboutDialog(showAboutDialog)
    RootDialog(showRootDialog)
}