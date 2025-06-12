package com.bt.clipbo.presentation.ui.secure

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.domain.usecase.ClipboardUseCase
import com.bt.clipbo.utils.BiometricHelper
import com.bt.clipbo.utils.BiometricStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecureClipboardViewModel @Inject constructor(
    private val clipboardUseCase: ClipboardUseCase,
    private val biometricHelper: BiometricHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecureClipboardUiState())
    val uiState: StateFlow<SecureClipboardUiState> = _uiState.asStateFlow()

    init {
        loadSecureItems()
    }

    fun checkBiometricAvailability(context: android.content.Context) {
        val status = biometricHelper.isBiometricAvailable(context)
        _uiState.value = _uiState.value.copy(
            biometricStatus = status,
            showBiometricStatus = true
        )
    }

    fun authenticateUser(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _uiState.value = _uiState.value.copy(isAuthenticating = true)

        biometricHelper.authenticateUser(
            activity = activity,
            title = "🔒 Güvenli Pano",
            subtitle = "Hassas verilerinize erişin",
            description = "Güvenli clipboard içeriklerini görmek için doğrulayın",
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = true,
                    isAuthenticating = false
                )
                onSuccess()
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(isAuthenticating = false)
                onError(error)
            },
            onCancel = {
                _uiState.value = _uiState.value.copy(isAuthenticating = false)
            }
        )
    }

    private fun loadSecureItems() {
        viewModelScope.launch {
            clipboardUseCase.getSecureItems().collect { items ->
                _uiState.value = _uiState.value.copy(
                    secureItems = items,
                    isLoading = false
                )
            }
        }
    }

    fun copyToClipboard(content: String) {
        viewModelScope.launch {
            clipboardUseCase.copyToClipboard(content)
        }
    }

    fun deleteItem(item: ClipboardEntity) {
        viewModelScope.launch {
            clipboardUseCase.deleteItem(item)
        }
    }

    fun togglePin(item: ClipboardEntity) {
        viewModelScope.launch {
            clipboardUseCase.togglePin(item)
        }
    }

    fun toggleSecureMode(item: ClipboardEntity) {
        viewModelScope.launch {
            clipboardUseCase.toggleSecure(item)
        }
    }

    fun logout() {
        _uiState.value = _uiState.value.copy(isAuthenticated = false)
    }
}