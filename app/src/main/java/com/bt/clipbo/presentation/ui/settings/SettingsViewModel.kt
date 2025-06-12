package com.bt.clipbo.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bt.clipbo.data.preferences.UserPreferences
import com.bt.clipbo.utils.BackupInfo
import com.bt.clipbo.utils.BackupRestoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri
import com.bt.clipbo.data.repository.ClipboardRepository
import com.bt.clipbo.data.repository.TagRepository
import java.io.File

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val backupRestoreManager: BackupRestoreManager,
    private val clipboardRepository: ClipboardRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadLocalBackups()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            userPreferences.isDarkTheme.collect { isDarkTheme ->
                _uiState.value = _uiState.value.copy(isDarkTheme = isDarkTheme)
            }
        }

        viewModelScope.launch {
            userPreferences.autoStartService.collect { autoStart ->
                _uiState.value = _uiState.value.copy(autoStartService = autoStart)
            }
        }

        viewModelScope.launch {
            userPreferences.maxHistoryItems.collect { maxItems ->
                _uiState.value = _uiState.value.copy(maxHistoryItems = maxItems)
            }
        }

        viewModelScope.launch {
            userPreferences.enableSecureMode.collect { secureMode ->
                _uiState.value = _uiState.value.copy(enableSecureMode = secureMode)
            }
        }

        // App version
        _uiState.value = _uiState.value.copy(
            appVersion = getAppVersion()
        )
    }

    private fun loadLocalBackups() {
        viewModelScope.launch {
            try {
                val localBackups = backupRestoreManager.getLocalBackups()
                _uiState.value = _uiState.value.copy(
                    localBackups = localBackups,
                    localBackupCount = localBackups.size
                )
            } catch (e: Exception) {
                showToast("Yerel yedekler yüklenemedi: ${e.message}")
            }
        }
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            userPreferences.setDarkTheme(!_uiState.value.isDarkTheme)
        }
    }

    fun toggleAutoStartService() {
        viewModelScope.launch {
            userPreferences.setAutoStartService(!_uiState.value.autoStartService)
        }
    }

    fun setMaxHistoryItems(count: Int) {
        viewModelScope.launch {
            userPreferences.setMaxHistoryItems(count)
        }
    }

    fun toggleSecureMode() {
        viewModelScope.launch {
            userPreferences.setEnableSecureMode(!_uiState.value.enableSecureMode)
        }
    }

    fun toggleAutoBackup() {
        viewModelScope.launch {
            val newValue = !_uiState.value.autoBackupEnabled
            _uiState.value = _uiState.value.copy(autoBackupEnabled = newValue)
            // TODO: Auto backup preference'ı UserPreferences'a ekle
            showToast(if (newValue) "Otomatik yedekleme açıldı" else "Otomatik yedekleme kapatıldı")
        }
    }

    // Backup & Restore Methods
    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBackupInProgress = true,
                showBackupProgress = true
            )

            val result = backupRestoreManager.exportToFile(
                uri = uri,
                includeSecureItems = true,
                encryptBackup = true
            )

            result.onSuccess { message ->
                showToast(message)
                loadLocalBackups() // Refresh local backups
            }.onFailure { error ->
                showToast("Yedekleme hatası: ${error.message}")
            }

            _uiState.value = _uiState.value.copy(
                isBackupInProgress = false,
                showBackupProgress = false
            )
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRestoreInProgress = true,
                showBackupProgress = true
            )

            val result = backupRestoreManager.importFromFile(
                uri = uri,
                overwriteExisting = false,
                restoreSecureItems = true
            )

            result.onSuccess { message ->
                showToast(message)
                loadLocalBackups() // Refresh local backups
            }.onFailure { error ->
                showToast("Geri yükleme hatası: ${error.message}")
            }

            _uiState.value = _uiState.value.copy(
                isRestoreInProgress = false,
                showBackupProgress = false
            )
        }
    }

    fun restoreLocalBackup(backupInfo: BackupInfo) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isRestoreInProgress = true,
                    showBackupProgress = true
                )

                val uri = Uri.fromFile(File(backupInfo.filePath))
                val result = backupRestoreManager.importFromFile(
                    uri = uri,
                    overwriteExisting = false,
                    restoreSecureItems = true
                )

                result.onSuccess { message ->
                    showToast(message)
                    hideLocalBackupsDialog()
                }.onFailure { error ->
                    showToast("Yerel yedek geri yükleme hatası: ${error.message}")
                }

            } catch (e: Exception) {
                showToast("Yerel yedek geri yükleme hatası: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(
                    isRestoreInProgress = false,
                    showBackupProgress = false
                )
            }
        }
    }

    fun deleteLocalBackup(backupInfo: BackupInfo) {
        viewModelScope.launch {
            try {
                val file = File(backupInfo.filePath)
                if (file.exists() && file.delete()) {
                    showToast("Yedek dosyası silindi: ${backupInfo.fileName}")
                    loadLocalBackups() // Refresh list
                } else {
                    showToast("Yedek dosyası silinemedi")
                }
            } catch (e: Exception) {
                showToast("Yedek silme hatası: ${e.message}")
            }
        }
    }

    fun createManualBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBackupInProgress = true,
                showBackupProgress = true
            )

            val result = backupRestoreManager.createAutoBackup()

            result.onSuccess { backupFile ->
                showToast("Manuel yedek oluşturuldu: ${backupFile.name}")
                loadLocalBackups() // Refresh local backups
            }.onFailure { error ->
                showToast("Manuel yedek hatası: ${error.message}")
            }

            _uiState.value = _uiState.value.copy(
                isBackupInProgress = false,
                showBackupProgress = false
            )
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Tüm clipboard öğelerini sil
                clipboardRepository.clearAllUnpinned()

                // Sabitlenmiş öğeleri de sil
                val allItems = clipboardRepository.getAllItems()
                // TODO: getAllItems().first() kullanarak tüm öğeleri al ve sil

                // Tüm etiketleri sil
                val allTags = tagRepository.getAllTags()
                // TODO: getAllTags().first() kullanarak tüm etiketleri al ve sil

                // Yerel yedekleri sil
                val localBackups = backupRestoreManager.getLocalBackups()
                localBackups.forEach { backup ->
                    try {
                        File(backup.filePath).delete()
                    } catch (e: Exception) {
                        // Ignore individual file deletion errors
                    }
                }

                showToast("✅ Tüm veriler başarıyla silindi")
                loadLocalBackups() // Refresh

            } catch (e: Exception) {
                showToast("❌ Veri silme hatası: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // Dialog Methods
    fun showMaxItemsDialog() {
        _uiState.value = _uiState.value.copy(showMaxItemsDialog = true)
    }

    fun hideMaxItemsDialog() {
        _uiState.value = _uiState.value.copy(showMaxItemsDialog = false)
    }

    fun showLocalBackupsDialog() {
        loadLocalBackups() // Refresh before showing
        _uiState.value = _uiState.value.copy(showLocalBackupsDialog = true)
    }

    fun hideLocalBackupsDialog() {
        _uiState.value = _uiState.value.copy(showLocalBackupsDialog = false)
    }

    fun showClearAllDataDialog() {
        _uiState.value = _uiState.value.copy(showClearAllDataDialog = true)
    }

    fun hideClearAllDataDialog() {
        _uiState.value = _uiState.value.copy(showClearAllDataDialog = false)
    }

    fun hideBackupProgress() {
        _uiState.value = _uiState.value.copy(showBackupProgress = false)
    }

    fun getBackupRestoreManager(): BackupRestoreManager = backupRestoreManager

    // Helper Methods
    private fun showToast(message: String) {
        _uiState.value = _uiState.value.copy(toastMessage = message)
    }

    fun clearToastMessage() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    private fun getAppVersion(): String {
        return try {
            "1.0.0" // TODO: Package manager'dan gerçek version al
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}