package com.bt.clipbo.presentation.ui.tags

import com.bt.clipbo.data.database.TagEntity

data class TagManagementUiState(
    val tags: List<TagEntity> = emptyList(),
    val isLoading: Boolean = true,
    val showCreateDialog: Boolean = false,
)
