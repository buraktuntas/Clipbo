package com.bt.clipbo.presentation.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bt.clipbo.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RatingState(
    val showDialog: Boolean = false,
    val shouldShowRating: Boolean = false
)

@HiltViewModel
class RatingViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(RatingState())
    val state: StateFlow<RatingState> = _state.asStateFlow()

    init {
        checkShouldShowRating()
    }

    private fun checkShouldShowRating() {
        viewModelScope.launch {
            val copyCount = userPreferences.getCopyCount()
            val lastRatingPrompt = userPreferences.getLastRatingPrompt()
            val hasRated = userPreferences.hasRated()

            // Kullanıcı 10 kez kopyalama yaptıysa ve son 7 gün içinde değerlendirme istenmediyse
            val shouldShow = copyCount >= 10 && 
                           !hasRated && 
                           (System.currentTimeMillis() - lastRatingPrompt) > (7 * 24 * 60 * 60 * 1000)

            _state.value = _state.value.copy(shouldShowRating = shouldShow)
        }
    }

    fun showRatingDialog() {
        _state.value = _state.value.copy(showDialog = true)
    }

    fun hideRatingDialog() {
        _state.value = _state.value.copy(showDialog = false)
    }

    fun onRateLater() {
        viewModelScope.launch {
            userPreferences.updateLastRatingPrompt(System.currentTimeMillis())
            hideRatingDialog()
        }
    }

    fun onRateNow(context: Context) {
        viewModelScope.launch {
            userPreferences.setHasRated(true)
            hideRatingDialog()
            
            // Play Store'a yönlendir
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
} 