package com.bt.clipbo.presentation.ui.secure

import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.utils.BiometricStatus

data class SecureClipboardUiState(
    val secureItems: List<ClipboardEntity> = emptyList(),
    val isAuthenticated: Boolean = false,
    val isAuthenticating: Boolean = false,
    val isLoading: Boolean = true,
    val biometricStatus: BiometricStatus = BiometricStatus.UNKNOWN,
    val showBiometricStatus: Boolean = false,
)
