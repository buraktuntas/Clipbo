package com.bt.clipbo.presentation.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.data.database.TagEntity
import com.bt.clipbo.data.repository.ClipboardRepository
import com.bt.clipbo.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagAssignmentViewModel @Inject constructor(
    private val tagRepository: TagRepository,
    private val clipboardRepository: ClipboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagAssignmentUiState())
    val uiState: StateFlow<TagAssignmentUiState> = _uiState.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            tagRepository.getAllTags().collect { tags ->
                _uiState.value = _uiState.value.copy(
                    availableTags = tags,
                    isLoading = false
                )
            }
        }
    }

    fun setClipboardItem(item: ClipboardEntity) {
        val currentTags = parseTagsFromString(item.tags)
        _uiState.value = _uiState.value.copy(
            currentItem = item,
            selectedTags = currentTags.toSet()
        )
    }

    fun toggleTag(tag: TagEntity) {
        val currentSelected = _uiState.value.selectedTags.toMutableSet()
        if (currentSelected.contains(tag.name)) {
            currentSelected.remove(tag.name)
        } else {
            currentSelected.add(tag.name)
        }
        _uiState.value = _uiState.value.copy(selectedTags = currentSelected)
    }

    fun createNewTag(name: String, color: String) {
        viewModelScope.launch {
            val newTag = TagEntity(
                name = name.trim(),
                color = color,
                usageCount = 1
            )
            tagRepository.insertTag(newTag)

            // Yeni etiketi seçili listeye ekle
            val currentSelected = _uiState.value.selectedTags.toMutableSet()
            currentSelected.add(name.trim())
            _uiState.value = _uiState.value.copy(selectedTags = currentSelected)
        }
    }

    fun saveTagAssignments() {
        val currentItem = _uiState.value.currentItem ?: return
        val selectedTags = _uiState.value.selectedTags

        viewModelScope.launch {
            // Etiketleri JSON string'e çevir
            val tagsString = selectedTags.joinToString(",")

            // Clipboard item'ı güncelle
            val updatedItem = currentItem.copy(tags = tagsString)
            clipboardRepository.updateItem(updatedItem)

            // Kullanım sayaçlarını güncelle
            selectedTags.forEach { tagName ->
                val tag = _uiState.value.availableTags.find { it.name == tagName }
                tag?.let {
                    tagRepository.incrementUsageCount(it.id)
                }
            }

            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }

    fun showCreateTagDialog() {
        _uiState.value = _uiState.value.copy(showCreateTagDialog = true)
    }

    fun hideCreateTagDialog() {
        _uiState.value = _uiState.value.copy(showCreateTagDialog = false)
    }

    private fun parseTagsFromString(tagsString: String): List<String> {
        return if (tagsString.isBlank()) {
            emptyList()
        } else {
            tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
}