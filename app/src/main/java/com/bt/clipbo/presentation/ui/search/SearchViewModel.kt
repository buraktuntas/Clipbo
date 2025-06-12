package com.bt.clipbo.presentation.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.domain.usecase.ClipboardUseCase
import com.bt.clipbo.utils.TimeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val clipboardUseCase: ClipboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var allItems: List<ClipboardEntity> = emptyList()

    init {
        loadAllItems()
    }

    private fun loadAllItems() {
        viewModelScope.launch {
            clipboardUseCase.getAllItems().collect { items ->
                allItems = items
                applyFilters()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun updateSelectedType(type: String) {
        val currentTypes = _uiState.value.selectedTypes.toMutableSet()
        if (currentTypes.contains(type)) {
            currentTypes.remove(type)
        } else {
            currentTypes.add(type)
        }
        _uiState.value = _uiState.value.copy(selectedTypes = currentTypes)
        applyFilters()
    }

    fun updateTimeFilter(timeFilter: TimeFilter) {
        _uiState.value = _uiState.value.copy(selectedTimeFilter = timeFilter)
        applyFilters()
    }

    fun toggleShowPinnedOnly() {
        _uiState.value = _uiState.value.copy(
            showPinnedOnly = !_uiState.value.showPinnedOnly
        )
        applyFilters()
    }

    fun toggleShowSecureOnly() {
        _uiState.value = _uiState.value.copy(
            showSecureOnly = !_uiState.value.showSecureOnly
        )
        applyFilters()
    }

    fun clearFilters() {
        _uiState.value = SearchUiState()
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        var filteredItems = allItems

        // Metin araması
        if (state.searchQuery.isNotEmpty()) {
            filteredItems = filteredItems.filter { item ->
                item.content.contains(state.searchQuery, ignoreCase = true) ||
                        item.type.contains(state.searchQuery, ignoreCase = true)
            }
        }

        // Tür filtresi
        if (state.selectedTypes.isNotEmpty()) {
            filteredItems = filteredItems.filter { item ->
                state.selectedTypes.contains(item.type)
            }
        }

        // Zaman filtresi
        val currentTime = System.currentTimeMillis()
        filteredItems = filteredItems.filter { item ->
            when (state.selectedTimeFilter) {
                TimeFilter.ALL -> true
                TimeFilter.LAST_HOUR -> (currentTime - item.timestamp) < 3600_000L
                TimeFilter.TODAY -> (currentTime - item.timestamp) < 86400_000L
                TimeFilter.LAST_WEEK -> (currentTime - item.timestamp) < 604800_000L
                TimeFilter.LAST_MONTH -> (currentTime - item.timestamp) < 2592000_000L
            }
        }

        // Sabitlenenler filtresi
        if (state.showPinnedOnly) {
            filteredItems = filteredItems.filter { it.isPinned }
        }

        // Güvenli öğeler filtresi
        if (state.showSecureOnly) {
            filteredItems = filteredItems.filter { it.isSecure }
        }

        _uiState.value = _uiState.value.copy(
            filteredItems = filteredItems,
            isLoading = false
        )
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