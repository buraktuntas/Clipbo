package com.bt.clipbo.presentation.ui.search

import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.utils.TimeFilter

data class SearchUiState(
    val searchQuery: String = "",
    val selectedTypes: Set<String> = emptySet(),
    val selectedTimeFilter: TimeFilter = TimeFilter.ALL,
    val showPinnedOnly: Boolean = false,
    val showSecureOnly: Boolean = false,
    val filteredItems: List<ClipboardEntity> = emptyList(),
    val isLoading: Boolean = true
)