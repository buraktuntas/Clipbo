package com.bt.clipbo.presentation.ui.history

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
class HistoryViewModel @Inject constructor(
    private val clipboardUseCase: ClipboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadClipboardItems()
    }

    private fun loadClipboardItems() {
        viewModelScope.launch {
            clipboardUseCase.getAllItems().collect { items ->
                _uiState.value = _uiState.value.copy(
                    clipboardItems = items,
                    isLoading = false
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

    fun clearAllUnpinned() {
        viewModelScope.launch {
            clipboardUseCase.clearAllUnpinned()
        }
    }
}