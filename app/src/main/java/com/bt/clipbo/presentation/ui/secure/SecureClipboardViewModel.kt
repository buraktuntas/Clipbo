package com.bt.clipbo.presentation.ui.secure

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
class SecureClipboardViewModel
    @Inject
    constructor(
        private val clipboardUseCase: ClipboardUseCase,
        private val biometricHelper: BiometricHelper,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SecureClipboardUiState())
        val uiState: StateFlow<SecureClipboardUiState> = _uiState.asStateFlow()

        init {
            loadSecureItems()
        }

        fun checkBiometricAvailability(context: android.content.Context) {
            viewModelScope.launch {
                try {
                    val status = biometricHelper.isBiometricAvailable(context)
                    _uiState.value =
                        _uiState.value.copy(
                            biometricStatus = status,
                            showBiometricStatus = true,
                        )
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            biometricStatus = BiometricStatus.UNKNOWN,
                            showBiometricStatus = true,
                        )
                }
            }
        }

        fun authenticateUser(
            activity: androidx.fragment.app.FragmentActivity,
            onSuccess: () -> Unit,
            onError: (String) -> Unit,
        ) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isAuthenticating = true)

                try {
                    biometricHelper.authenticateUser(
                        activity = activity,
                        title = "🔒 Güvenli Pano",
                        subtitle = "Hassas verilerinize erişin",
                        description = "Güvenli clipboard içeriklerini görmek için doğrulayın",
                        onSuccess = {
                            viewModelScope.launch {
                                _uiState.value =
                                    _uiState.value.copy(
                                        isAuthenticated = true,
                                        isAuthenticating = false,
                                    )
                                onSuccess()
                            }
                        },
                        onError = { error ->
                            viewModelScope.launch {
                                _uiState.value = _uiState.value.copy(isAuthenticating = false)
                                onError(error)
                            }
                        },
                        onCancel = {
                            viewModelScope.launch {
                                _uiState.value = _uiState.value.copy(isAuthenticating = false)
                            }
                        },
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(isAuthenticating = false)
                    onError("Biometric authentication failed: ${e.message}")
                }
            }
        }

        private fun loadSecureItems() {
            viewModelScope.launch {
                try {
                    clipboardUseCase.getSecureItems().collect { items ->
                        _uiState.value =
                            _uiState.value.copy(
                                secureItems = items,
                                isLoading = false,
                            )
                    }
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            secureItems = emptyList(),
                            isLoading = false,
                        )
                }
            }
        }

        fun copyToClipboard(content: String) {
            viewModelScope.launch {
                try {
                    clipboardUseCase.copyToClipboard(content)
                } catch (e: Exception) {
                    // Handle error silently or emit error state
                }
            }
        }

        fun deleteItem(item: ClipboardEntity) {
            viewModelScope.launch {
                try {
                    clipboardUseCase.deleteItem(item)
                } catch (e: Exception) {
                    // Handle error silently or emit error state
                }
            }
        }

        fun togglePin(item: ClipboardEntity) {
            viewModelScope.launch {
                try {
                    clipboardUseCase.togglePin(item)
                } catch (e: Exception) {
                    // Handle error silently or emit error state
                }
            }
        }

        fun toggleSecureMode(item: ClipboardEntity) {
            viewModelScope.launch {
                try {
                    clipboardUseCase.toggleSecure(item)
                } catch (e: Exception) {
                    // Handle error silently or emit error state
                }
            }
        }

        fun logout() {
            _uiState.value =
                _uiState.value.copy(
                    isAuthenticated = false,
                    isAuthenticating = false,
                )
        }

        fun retry() {
            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    showBiometricStatus = false,
                )
            loadSecureItems()
        }

        fun clearError() {
            // If you have error state, clear it here
        }
    }
