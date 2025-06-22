package com.bt.clipbo.presentation.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bt.clipbo.data.database.TagEntity
import com.bt.clipbo.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagManagementViewModel
    @Inject
    constructor(
        private val tagRepository: TagRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TagManagementUiState())
        val uiState: StateFlow<TagManagementUiState> = _uiState.asStateFlow()

        init {
            loadTags()
        }

        private fun loadTags() {
            viewModelScope.launch {
                try {
                    tagRepository.getAllTags().collect { tags ->
                        _uiState.value =
                            _uiState.value.copy(
                                tags = tags,
                                isLoading = false,
                            )
                    }
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            tags = emptyList(),
                            isLoading = false,
                        )
                }
            }
        }

        fun createTag(
            name: String,
            color: String,
        ) {
            viewModelScope.launch {
                try {
                    val tag =
                        TagEntity(
                            name = name.trim(),
                            color = color,
                            usageCount = 0,
                            createdAt = System.currentTimeMillis(),
                        )
                    tagRepository.insertTag(tag)
                } catch (e: Exception) {
                    // Handle error - could emit error state
                }
            }
        }

        fun updateTag(tag: TagEntity) {
            viewModelScope.launch {
                try {
                    tagRepository.updateTag(tag)
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }

        fun deleteTag(tag: TagEntity) {
            viewModelScope.launch {
                try {
                    tagRepository.deleteTag(tag)
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }

        fun showCreateDialog() {
            _uiState.value = _uiState.value.copy(showCreateDialog = true)
        }

        fun hideCreateDialog() {
            _uiState.value = _uiState.value.copy(showCreateDialog = false)
        }

        fun searchTags(query: String) {
            viewModelScope.launch {
                try {
                    if (query.isBlank()) {
                        loadTags()
                    } else {
                        tagRepository.searchTags(query).collect { tags ->
                            _uiState.value =
                                _uiState.value.copy(
                                    tags = tags,
                                    isLoading = false,
                                )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            tags = emptyList(),
                            isLoading = false,
                        )
                }
            }
        }

        fun incrementTagUsage(tagId: Long) {
            viewModelScope.launch {
                try {
                    tagRepository.incrementUsageCount(tagId)
                } catch (e: Exception) {
                    // Handle error silently
                }
            }
        }

        fun getTagByName(
            name: String,
            callback: (TagEntity?) -> Unit,
        ) {
            viewModelScope.launch {
                try {
                    val tag = tagRepository.getTagByName(name)
                    callback(tag)
                } catch (e: Exception) {
                    callback(null)
                }
            }
        }

        fun validateTagName(name: String): ValidationResult {
            return when {
                name.isBlank() -> ValidationResult.Error("Etiket adı boş olamaz")
                name.length < 2 -> ValidationResult.Error("Etiket adı en az 2 karakter olmalı")
                name.length > 20 -> ValidationResult.Error("Etiket adı en fazla 20 karakter olabilir")
                name.any { !it.isLetterOrDigit() && it != ' ' && it != '-' && it != '_' } ->
                    ValidationResult.Error("Etiket adı sadece harf, rakam, boşluk, tire ve alt çizgi içerebilir")
                else -> ValidationResult.Success
            }
        }

        fun retry() {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loadTags()
        }

        sealed class ValidationResult {
            object Success : ValidationResult()

            data class Error(val message: String) : ValidationResult()
        }
    }
