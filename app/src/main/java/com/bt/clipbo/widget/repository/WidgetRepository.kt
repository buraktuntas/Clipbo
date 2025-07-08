package com.bt.clipbo.widget.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.bt.clipbo.data.database.ClipboardDao
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.widget.WidgetClipboardItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clipboardDao: ClipboardDao
) {

    companion object {
        private const val TAG = "WidgetRepository"
        private const val PREFS_NAME = "clipbo_widget_prefs"
        private const val KEY_SERVICE_RUNNING = "service_running"
        private const val KEY_LAST_UPDATE = "last_update"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Get recent clipboard items with comprehensive error handling
     */
    fun getRecentItems(limit: Int): Flow<List<WidgetClipboardItem>> {
        return try {
            clipboardDao.getAllItems()
                .map { entities ->
                    entities
                        .take(limit.coerceIn(1, 20))
                        .mapNotNull { entity ->
                            try {
                                entity.toWidgetItem()
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to convert entity ${entity.id}", e)
                                null
                            }
                        }
                }
                .catch { exception ->
                    Log.e(TAG, "Error fetching recent items", exception)
                    emit(emptyList())
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create items flow", e)
            flowOf(emptyList())
        }
    }

    /**
     * Get service running status
     */
    fun isServiceRunning(): Flow<Boolean> = try {
        flowOf(getServiceRunningStatus())
    } catch (e: Exception) {
        Log.e(TAG, "Error checking service status", e)
        flowOf(false)
    }

    /**
     * Update service status
     */
    fun updateServiceStatus(isRunning: Boolean) {
        try {
            val timestamp = System.currentTimeMillis()
            sharedPreferences.edit()
                .putBoolean(KEY_SERVICE_RUNNING, isRunning)
                .putLong(KEY_LAST_UPDATE, timestamp)
                .apply()

            Log.d(TAG, "Service status updated: $isRunning")
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating service status", e)
        }
    }

    private fun getServiceRunningStatus(): Boolean {
        return try {
            sharedPreferences.getBoolean(KEY_SERVICE_RUNNING, false)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading service status", e)
            false
        }
    }

    /**
     * Clear widget cache
     */
    fun clearCache() {
        try {
            sharedPreferences.edit().clear().apply()
            Log.d(TAG, "Widget cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Exception clearing cache", e)
        }
    }
}

/**
 * Extension function to convert ClipboardEntity to WidgetClipboardItem
 */
private fun ClipboardEntity.toWidgetItem(): WidgetClipboardItem {
    return WidgetClipboardItem(
        id = this.id,
        content = this.content,
        preview = this.preview.ifEmpty {
            this.content.take(30) + if (this.content.length > 30) "..." else ""
        },
        type = this.type,
        timestamp = this.timestamp,
        isPinned = this.isPinned,
        isSecure = this.isSecure
    )
}