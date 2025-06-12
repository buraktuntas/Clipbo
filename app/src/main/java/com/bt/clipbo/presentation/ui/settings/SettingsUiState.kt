package com.bt.clipbo.presentation.ui.settings

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val autoStartService: Boolean = true,
    val maxHistoryItems: Int = 100,
    val enableSecureMode: Boolean = true,
    val showMaxItemsDialog: Boolean = false
)