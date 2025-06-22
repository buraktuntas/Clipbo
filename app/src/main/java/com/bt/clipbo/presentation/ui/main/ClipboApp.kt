package com.bt.clipbo.presentation.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bt.clipbo.presentation.ui.history.HistoryScreen
import com.bt.clipbo.presentation.ui.navigation.NavigationScreen
import com.bt.clipbo.presentation.ui.search.SearchAndFilterScreen
import com.bt.clipbo.presentation.ui.secure.SecureClipboardScreen
import com.bt.clipbo.presentation.ui.settings.SettingsScreen
import com.bt.clipbo.presentation.ui.tags.TagManagementScreen

@Composable
fun ClipboApp(
    onStartService: () -> Unit = {},
    onStopService: () -> Unit = {},
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Navigation) }

    when (currentScreen) {
        Screen.Navigation -> {
            NavigationScreen(
                onNavigateToHistory = { currentScreen = Screen.History },
                onNavigateToSearch = { currentScreen = Screen.Search },
                onNavigateToTags = { currentScreen = Screen.Tags },
                onNavigateToSecure = { currentScreen = Screen.Secure },
                onNavigateToSettings = { currentScreen = Screen.Settings },
                onStartService = onStartService,
                onStopService = onStopService,
            )
        }

        Screen.History -> {
            HistoryScreen(
                onNavigateBack = { currentScreen = Screen.Navigation },
            )
        }

        Screen.Search -> {
            SearchAndFilterScreen(
                onNavigateBack = { currentScreen = Screen.Navigation },
            )
        }

        Screen.Tags -> {
            TagManagementScreen(
                onNavigateBack = { currentScreen = Screen.Navigation },
            )
        }

        Screen.Secure -> {
            SecureClipboardScreen(
                onNavigateBack = { currentScreen = Screen.Navigation },
            )
        }

        Screen.Settings -> {
            SettingsScreen(
                onNavigateBack = { currentScreen = Screen.Navigation },
            )
        }
    }
}
