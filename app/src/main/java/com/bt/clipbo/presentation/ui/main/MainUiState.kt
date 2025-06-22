package com.bt.clipbo.presentation.ui.main

import com.bt.clipbo.data.database.ClipboardEntity

data class MainUiState(
    val clipboardItems: List<ClipboardEntity> = emptyList(),
    val isLoading: Boolean = true,
)
