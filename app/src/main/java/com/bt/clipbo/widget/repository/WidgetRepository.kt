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

        // Thread-safe singleton with AtomicReference
        private val INSTANCE = AtomicReference<WidgetRepository?>(null)

        /**
         * Thread-safe singleton getter with double-checked locking pattern
         */
        fun getInstance(): WidgetRepository {
            return INSTANCE.get() ?: throw IllegalStateException(
                "WidgetRepository not initialized. Ensure Hilt injection is working properly."
            )
        }

        /**
         * Thread-safe initialization with compare-and-set
         */
        fun initialize(instance: WidgetRepository): Boolean {
            val success = INSTANCE.compareAndSet(null, instance)
            if (success) {
                Log.d(TAG, "✅ WidgetRepository initialized successfully")
            } else {
                Log.w(TAG, "⚠️ WidgetRepository already initialized")
            }
            return success
        }

        /**
         * Check if initialized atomically
         */
        fun isInitialized(): Boolean = INSTANCE.get() != null

        /**
         * Safe cleanup with atomic operation
         */
        fun cleanup() {
            val previous = INSTANCE.getAndSet(null)
            if (previous != null) {
                Log.d(TAG, "🧹 WidgetRepository cleaned up")
            }
        }

        /**
         * Force reset (use only in tests)
         */
        @JvmStatic
        fun forceReset() {
            INSTANCE.set(null)
        }
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Initialize immediately upon creation
    init {
        initialize(this)
    }

    /**
     * Get recent clipboard items with comprehensive error handling
     */
    fun getRecentItems(limit: Int): Flow<List<WidgetClipboardItem>> {
        return try {
            clipboardDao.getAllItems()
                .map { entities ->
                    entities
                        .take(limit.coerceIn(1, 20)) // Limit validation
                        .mapNotNull { entity ->
                            try {
                                entity.toWidgetItem()
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to convert entity ${entity.id}", e)
                                null // Skip problematic items
                            }
                        }
                }
                .catch { exception ->
                    Log.e(TAG, "Error fetching recent items", exception)
                    emit(emptyList()) // Graceful fallback
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create items flow", e)
            flowOf(emptyList()) // Safe fallback
        }
    }

    /**
     * Get service running status with multi-layer validation
     */
    fun isServiceRunning(): Flow<Boolean> = try {
        flowOf(getServiceRunningStatusWithValidation())
    } catch (e: Exception) {
        Log.e(TAG, "Error checking service status", e)
        flowOf(false) // Safe default
    }

    /**
     * Atomic service status update
     */
    fun updateServiceStatus(isRunning: Boolean) {
        try {
            val timestamp = System.currentTimeMillis()

            val success = sharedPreferences.edit()
                .putBoolean(KEY_SERVICE_RUNNING, isRunning)
                .putLong(KEY_LAST_UPDATE, timestamp)
                .commit() // Synchronous for reliability

            if (success) {
                Log.d(TAG, "✅ Service status updated: $isRunning at $timestamp")
            } else {
                Log.e(TAG, "❌ Failed to update service status")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception updating service status", e)
        }
    }

    /**
     * Multi-layer service status validation
     */
    private fun getServiceRunningStatusWithValidation(): Boolean {
        return try {
            // Layer 1: SharedPreferences (fast)
            val fromPrefs = sharedPreferences.getBoolean(KEY_SERVICE_RUNNING, false)
            val lastUpdate = sharedPreferences.getLong(KEY_LAST_UPDATE, 0)

            // Layer 2: Staleness check
            val age = System.currentTimeMillis() - lastUpdate
            val isStale = age > 120_000 // 2 minutes

            if (isStale && lastUpdate > 0) {
                Log.w(TAG, "⏰ Service status is stale (${age / 1000}s old), refreshing...")
                refreshServiceStatusFromSystem()
            } else {
                fromPrefs
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in service status validation", e)
            false // Safe default
        }
    }

    /**
     * System-level service status check (expensive operation)
     */
    private fun refreshServiceStatusFromSystem(): Boolean = try {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
                as android.app.ActivityManager

        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
        val isRunning = runningServices.any { serviceInfo ->
            serviceInfo.service.className.contains("ClipboardService") &&
                    serviceInfo.service.packageName == context.packageName
        }

        // Update cache with fresh data
        updateServiceStatus(isRunning)
        isRunning

    } catch (e: Exception) {
        Log.e(TAG, "System service check failed", e)
        false
    }

    /**
     * Get widget statistics with error handling
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
                emit(WidgetStats()) // Empty stats fallback
            }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create stats flow", e)
        flowOf(WidgetStats())
    }

    /**
     * Clear widget cache safely
     */
    fun clearCache() {
        try {
            val success = sharedPreferences.edit().clear().commit()
            if (success) {
                Log.d(TAG, "🧹 Widget cache cleared successfully")
            } else {
                Log.e(TAG, "❌ Failed to clear widget cache")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception clearing cache", e)
        }
    }

    /**
     * Get comprehensive cache info for debugging
     */
    fun getCacheInfo(): String = try {
        val lastUpdate = sharedPreferences.getLong(KEY_LAST_UPDATE, 0)
        val serviceRunning = sharedPreferences.getBoolean(KEY_SERVICE_RUNNING, false)
        val age = if (lastUpdate > 0) System.currentTimeMillis() - lastUpdate else 0

        buildString {
            appendLine("📊 Widget Cache Info:")
            appendLine("├─ Service Running: ${if (serviceRunning) "✅" else "❌"}")
            appendLine("├─ Last Update: ${if (lastUpdate > 0) "${age / 1000}s ago" else "Never"}")
            appendLine("├─ Cache Age: ${age / 1000}s")
            appendLine("├─ Cache Status: ${if (age < 120_000) "Fresh" else "Stale"}")
            appendLine("└─ Instance: ${if (isInitialized()) "✅ Ready" else "❌ Not Ready"}")
        }
    } catch (e: Exception) {
        "❌ Cache info error: ${e.message}"
    }
}

/**
 * Thread-safe extension function to convert ClipboardEntity to WidgetClipboardItem
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
    Log.e("WidgetRepository", "Error converting entity ${this.id} to widget item", e)
    WidgetClipboardItem(
        id = this.id,
        content = "⚠️ Error loading content",
        preview = "Error",
        type = "ERROR",
        timestamp = this.timestamp,
        isPinned = false,
        isSecure = false
    )
}

/**
 * Widget statistics data class with defaults
 */
data class WidgetStats(
    val totalItems: Int = 0,
    val pinnedItems: Int = 0,
    val secureItems: Int = 0,
    val lastUpdate: Long = 0L
) {
    val isEmpty: Boolean get() = totalItems == 0
    val hasSecureItems: Boolean get() = secureItems > 0
    val pinnedPercentage: Float get() = if (totalItems > 0) (pinnedItems.toFloat() / totalItems) * 100 else 0f
}