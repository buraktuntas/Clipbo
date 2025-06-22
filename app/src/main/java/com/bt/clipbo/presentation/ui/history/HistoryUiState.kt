package com.bt.clipbo.presentation.ui.history

import com.bt.clipbo.data.database.ClipboardEntity

data class HistoryUiState(
    val clipboardItems: List<ClipboardEntity> = emptyList(),
    val isLoading: Boolean = true,
)
