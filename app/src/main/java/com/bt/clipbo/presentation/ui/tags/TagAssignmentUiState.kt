package com.bt.clipbo.presentation.ui.tags

import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.data.database.TagEntity

data class TagAssignmentUiState(
    val currentItem: ClipboardEntity? = null,
    val availableTags: List<TagEntity> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val showCreateTagDialog: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
)
