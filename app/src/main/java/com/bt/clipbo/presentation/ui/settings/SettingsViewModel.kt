package com.bt.clipbo.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bt.clipbo.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
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

    fun showMaxItemsDialog() {
        _uiState.value = _uiState.value.copy(showMaxItemsDialog = true)
    }

    fun hideMaxItemsDialog() {
        _uiState.value = _uiState.value.copy(showMaxItemsDialog = false)
    }
}