package com.bt.clipbo.presentation.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bt.clipbo.domain.usecase.ClipboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class StatisticsState(
    val todayCount: Int = 0,
    val weekCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class StatisticsViewModel
    @Inject
    constructor(
        private val clipboardUseCase: ClipboardUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(StatisticsState())
        val state: StateFlow<StatisticsState> = _state.asStateFlow()

        init {
            loadStatistics()
        }

        private fun loadStatistics() {
            viewModelScope.launch {
                try {
                    val calendar = Calendar.getInstance()
                    val startOfDay =
                        calendar.apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                    val startOfWeek =
                        calendar.apply {
                            set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                    clipboardUseCase.getAllItems().collect { items ->
                        val todayItems = items.filter { it.timestamp >= startOfDay }
                        val weekItems = items.filter { it.timestamp >= startOfWeek }

                        _state.value =
                            StatisticsState(
                                todayCount = todayItems.size,
                                weekCount = weekItems.size,
                                totalCount = items.size,
                                isLoading = false,
                            )
                    }
                } catch (e: Exception) {
                    _state.value = StatisticsState(isLoading = false)
                }
            }
        }

        fun refreshStatistics() {
            loadStatistics()
        }
    }
