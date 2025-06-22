package com.bt.clipbo.presentation.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.domain.usecase.ClipboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val clipboardUseCase: ClipboardUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MainUiState())
        val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

        init {
            loadClipboardItems()
        }

        private fun loadClipboardItems() {
            viewModelScope.launch {
                clipboardUseCase.getAllItems().collect { items ->
                    _uiState.value =
                        _uiState.value.copy(
                            clipboardItems = items,
                            isLoading = false,
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
    }
