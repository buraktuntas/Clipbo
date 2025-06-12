package com.bt.clipbo.presentation.ui.settings

import com.bt.clipbo.utils.BackupInfo

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val autoStartService: Boolean = true,
    val maxHistoryItems: Int = 100,
    val enableSecureMode: Boolean = true,
    val autoBackupEnabled: Boolean = true,
    val localBackupCount: Int = 0,
    val appVersion: String = "1.0.0",

    // Dialog states
    val showMaxItemsDialog: Boolean = false,
    val showBackupProgress: Boolean = false,
    val showLocalBackupsDialog: Boolean = false,
    val showClearAllDataDialog: Boolean = false,

    // Backup related
    val localBackups: List<BackupInfo> = emptyList(),
    val isBackupInProgress: Boolean = false,
    val isRestoreInProgress: Boolean = false,

    // Messages
    val toastMessage: String? = null,
    val isLoading: Boolean = false
)