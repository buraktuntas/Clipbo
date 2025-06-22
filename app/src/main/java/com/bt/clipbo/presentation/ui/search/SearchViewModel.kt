package com.bt.clipbo.presentation.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.domain.usecase.ClipboardUseCase
import com.bt.clipbo.utils.TimeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val clipboardUseCase: ClipboardUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SearchUiState())
        val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

        private var allItems: List<ClipboardEntity> = emptyList()

        init {
            loadAllItems()
        }

        private fun loadAllItems() {
            viewModelScope.launch {
                clipboardUseCase.getAllItems()
                    .catch { error ->
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                error = error.message ?: "Bilinmeyen hata",
                            )
                    }
                    .collect { items ->
                        allItems = items
                        applyFilters()
                    }
            }
        }

        fun updateSearchQuery(query: String) {
            _uiState.value =
                _uiState.value.copy(
                    searchQuery = query,
                    error = null,
                )
            applyFilters()
        }

        fun updateSelectedType(type: String) {
            val currentTypes = _uiState.value.selectedTypes.toMutableSet()
            if (currentTypes.contains(type)) {
                currentTypes.remove(type)
            } else {
                currentTypes.add(type)
            }
            _uiState.value =
                _uiState.value.copy(
                    selectedTypes = currentTypes,
                    error = null,
                )
            applyFilters()
        }

        fun updateTimeFilter(timeFilter: TimeFilter) {
            _uiState.value =
                _uiState.value.copy(
                    selectedTimeFilter = timeFilter,
                    error = null,
                )
            applyFilters()
        }

        fun toggleShowPinnedOnly() {
            _uiState.value =
                _uiState.value.copy(
                    showPinnedOnly = !_uiState.value.showPinnedOnly,
                    error = null,
                )
            applyFilters()
        }

        fun toggleShowSecureOnly() {
            _uiState.value =
                _uiState.value.copy(
                    showSecureOnly = !_uiState.value.showSecureOnly,
                    error = null,
                )
            applyFilters()
        }

        fun clearFilters() {
            _uiState.value = SearchUiState()
            applyFilters()
        }

        private fun applyFilters() {
            try {
                val state = _uiState.value
                var filteredItems = allItems

                // Metin araması
                if (state.searchQuery.isNotEmpty()) {
                    filteredItems =
                        filteredItems.filter { item ->
                            item.content.contains(state.searchQuery, ignoreCase = true) ||
                                item.type.contains(state.searchQuery, ignoreCase = true) ||
                                item.tags.contains(state.searchQuery, ignoreCase = true)
                        }
                }

                // Tür filtresi
                if (state.selectedTypes.isNotEmpty()) {
                    filteredItems =
                        filteredItems.filter { item ->
                            state.selectedTypes.contains(item.type)
                        }
                }

                // Zaman filtresi
                val currentTime = System.currentTimeMillis()
                filteredItems =
                    filteredItems.filter { item ->
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

                _uiState.value =
                    _uiState.value.copy(
                        filteredItems = filteredItems,
                        isLoading = false,
                        error = null,
                    )
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        error = "Filtreleme sırasında hata: ${e.message}",
                    )
            }
        }

        fun copyToClipboard(content: String) {
            viewModelScope.launch {
                try {
                    clipboardUseCase.copyToClipboard(content)
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            error = "Kopyalama hatası: ${e.message}",
                        )
                }
            }
        }

        fun deleteItem(item: ClipboardEntity) {
            viewModelScope.launch {
                try {
                    clipboardUseCase.deleteItem(item)
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            error = "Silme hatası: ${e.message}",
                        )
                }
            }
        }

        fun togglePin(item: ClipboardEntity) {
            viewModelScope.launch {
                try {
                    clipboardUseCase.togglePin(item)
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            error = "Sabitleme hatası: ${e.message}",
                        )
                }
            }
        }

        fun clearError() {
            _uiState.value = _uiState.value.copy(error = null)
        }

        fun retryLoad() {
            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    error = null,
                )
            loadAllItems()
        }
    }
