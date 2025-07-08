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
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.read
import kotlin.concurrent.write

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

        @Volatile
        private var INSTANCE: WidgetRepository? = null

        // Thread-safe instance management
        private val lock = ReentrantReadWriteLock()

        /**
         * Thread-safe singleton instance getter
         */
        fun getInstance(): WidgetRepository = lock.read {
            INSTANCE ?: throw IllegalStateException(
                "WidgetRepository not initialized. Call initialize() first."
            )
        }

        /**
         * Thread-safe initialization
         */
        fun initialize(instance: WidgetRepository) = lock.write {
            if (INSTANCE == null) {
                INSTANCE = instance
                Log.d(TAG, "WidgetRepository initialized successfully")
            } else {
                Log.w(TAG, "WidgetRepository already initialized")
            }
        }

        /**
         * Check if initialized
         */
        fun isInitialized(): Boolean = lock.read {
            INSTANCE != null
        }

        /**
         * Safe cleanup
         */
        fun cleanup() = lock.write {
            INSTANCE = null
            Log.d(TAG, "WidgetRepository cleaned up")
        }
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Get recent clipboard items for widget display
     * Thread-safe ve error-resistant
     */
    fun getRecentItems(limit: Int): Flow<List<WidgetClipboardItem>> {
        return try {
            clipboardDao.getAllItems()
                .map { entities ->
                    entities.take(limit).map { entity ->
                        entity.toWidgetItem()
                    }
                }
                .catch { exception ->
                    Log.e(TAG, "Error fetching recent items", exception)
                    emit(emptyList()) // Fallback to empty list
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create items flow", e)
            flowOf(emptyList()) // Safe fallback
        }
    }

    /**
     * Get service running status
     * Multiple source checking için robust
     */
    fun isServiceRunning(): Flow<Boolean> = try {
        flowOf(getServiceRunningStatus())
    } catch (e: Exception) {
        Log.e(TAG, "Error checking service status", e)
        flowOf(false) // Safe fallback
    }

    /**
     * Update service status atomically
     */
    fun updateServiceStatus(isRunning: Boolean) {
        try {
            val timestamp = System.currentTimeMillis()

            sharedPreferences.edit()
                .putBoolean(KEY_SERVICE_RUNNING, isRunning)
                .putLong(KEY_LAST_UPDATE, timestamp)
                .apply() // Async write

            Log.d(TAG, "Service status updated: $isRunning at $timestamp")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update service status", e)
        }
    }

    /**
     * Get widget statistics
     */
    fun getWidgetStats(): Flow<WidgetStats> = try {
        clipboardDao.getAllItems()
            .map { entities ->
                WidgetStats(
                    totalItems = entities.size,
                    pinnedItems = entities.count { it.isPinned },
                    secureItems = entities.count { it.isSecure },
                    lastUpdate = System.currentTimeMillis()
                )
            }
            .catch { exception ->
                Log.e(TAG, "Error fetching widget stats", exception)
                emit(WidgetStats()) // Empty stats as fallback
            }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create stats flow", e)
        flowOf(WidgetStats()) // Safe fallback
    }

    /**
     * Internal service status check with multiple fallbacks
     */
    private fun getServiceRunningStatus(): Boolean = try {
        // Primary: SharedPreferences
        val fromPrefs = sharedPreferences.getBoolean(KEY_SERVICE_RUNNING, false)
        val lastUpdate = sharedPreferences.getLong(KEY_LAST_UPDATE, 0)

        // Validation: Status çok eski mi?
        val isStale = System.currentTimeMillis() - lastUpdate > 60_000 // 1 dakika

        if (isStale) {
            Log.w(TAG, "Service status is stale, refreshing...")
            // Secondary check: ActivityManager (costly operation)
            checkServiceViaActivityManager()
        } else {
            fromPrefs
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error in service status check", e)
        false // Safe default
    }

    /**
     * Secondary service check via ActivityManager
     * Expensive operation - use sparingly
     */
    private fun checkServiceViaActivityManager(): Boolean = try {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
                as android.app.ActivityManager

        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
        val isRunning = runningServices.any { serviceInfo ->
            serviceInfo.service.className.contains("ClipboardService")
        }

        // Update cache
        updateServiceStatus(isRunning)
        isRunning

    } catch (e: Exception) {
        Log.e(TAG, "ActivityManager check failed", e)
        false
    }

    /**
     * Clear widget cache
     */
    fun clearCache() {
        try {
            sharedPreferences.edit()
                .clear()
                .apply()
            Log.d(TAG, "Widget cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache", e)
        }
    }

    /**
     * Get cache info for debugging
     */
    fun getCacheInfo(): String = try {
        val lastUpdate = sharedPreferences.getLong(KEY_LAST_UPDATE, 0)
        val serviceRunning = sharedPreferences.getBoolean(KEY_SERVICE_RUNNING, false)
        val age = System.currentTimeMillis() - lastUpdate

        """
        Widget Cache Info:
        - Service Running: $serviceRunning
        - Last Update: ${if (lastUpdate > 0) "${age / 1000}s ago" else "Never"}
        - Cache Age: ${age / 1000}s
        """.trimIndent()
    } catch (e: Exception) {
        "Cache info error: ${e.message}"
    }
}

/**
 * Extension function to convert ClipboardEntity to WidgetClipboardItem
 * Null-safe ve error-resistant
 */
private fun ClipboardEntity.toWidgetItem(): WidgetClipboardItem = try {
    WidgetClipboardItem(
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
} catch (e: Exception) {
    Log.e("WidgetRepository", "Error converting entity to widget item", e)
    WidgetClipboardItem(
        id = this.id,
        content = "Error loading content",
        preview = "Error",
        type = "ERROR",
        timestamp = this.timestamp,
        isPinned = false,
        isSecure = false
    )
}

/**
 * Widget statistics data class
 */
data class WidgetStats(
    val totalItems: Int = 0,
    val pinnedItems: Int = 0,
    val secureItems: Int = 0,
    val lastUpdate: Long = 0L
)