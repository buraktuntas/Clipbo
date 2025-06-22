package com.bt.clipbo.utils

import android.content.Context
import com.bt.clipbo.BuildConfig
import com.bt.clipbo.data.database.UsageAnalyticsDao
import com.bt.clipbo.data.database.UsageAnalyticsEntity
import com.bt.clipbo.data.preferences.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

sealed class AnalyticsEvent(val eventName: String) {
    // User Actions
    object AppLaunched : AnalyticsEvent("app_launched")

    object ServiceStarted : AnalyticsEvent("service_started")

    object ServiceStopped : AnalyticsEvent("service_stopped")

    // Clipboard Events
    object ClipboardItemCopied : AnalyticsEvent("clipboard_item_copied")

    object ClipboardItemDeleted : AnalyticsEvent("clipboard_item_deleted")

    object ClipboardItemPinned : AnalyticsEvent("clipboard_item_pinned")

    object ClipboardItemUnpinned : AnalyticsEvent("clipboard_item_unpinned")

    // Search & Filter
    object SearchPerformed : AnalyticsEvent("search_performed")

    object FilterApplied : AnalyticsEvent("filter_applied")

    // Security
    object BiometricAuthSuccess : AnalyticsEvent("biometric_auth_success")

    object BiometricAuthFailed : AnalyticsEvent("biometric_auth_failed")

    object SecureItemViewed : AnalyticsEvent("secure_item_viewed")

    // Features
    object BackupCreated : AnalyticsEvent("backup_created")

    object BackupRestored : AnalyticsEvent("backup_restored")

    object TagCreated : AnalyticsEvent("tag_created")

    object TagAssigned : AnalyticsEvent("tag_assigned")

    // Errors
    object ErrorOccurred : AnalyticsEvent("error_occurred")

    object CrashOccurred : AnalyticsEvent("crash_occurred")

    // Performance
    object DatabaseQuerySlow : AnalyticsEvent("database_query_slow")

    object MemoryWarning : AnalyticsEvent("memory_warning")

    // Widget
    object WidgetAdded : AnalyticsEvent("widget_added")

    object WidgetClicked : AnalyticsEvent("widget_clicked")

    // Settings
    object SettingChanged : AnalyticsEvent("setting_changed")

    object ThemeChanged : AnalyticsEvent("theme_changed")
}

data class AnalyticsData(
    val parameters: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String = "",
)

@Singleton
class AnalyticsManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val usageAnalyticsDao: UsageAnalyticsDao,
        private val userPreferences: UserPreferences,
    ) {
        private val analyticsScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        private var currentSessionId = generateSessionId()
        private var isAnalyticsEnabled = true

        companion object {
            private const val TAG = "AnalyticsManager"
            private const val SESSION_TIMEOUT = 30 * 60 * 1000L // 30 minutes
        }

        init {
            analyticsScope.launch {
                // Check if analytics is enabled
                isAnalyticsEnabled = userPreferences.enableAnalytics.first()

                // Clean old analytics data (keep last 30 days)
                cleanOldAnalyticsData()

                // Start session
                trackEvent(
                    AnalyticsEvent.AppLaunched,
                    AnalyticsData(
                        parameters =
                            mapOf(
                                "app_version" to getAppVersion(),
                                "android_version" to android.os.Build.VERSION.RELEASE,
                                "device_model" to android.os.Build.MODEL,
                                "device_manufacturer" to android.os.Build.MANUFACTURER,
                            ),
                    ),
                )
            }
        }

        fun trackEvent(
            event: AnalyticsEvent,
            data: AnalyticsData = AnalyticsData(),
        ) {
            if (!isAnalyticsEnabled) return

            analyticsScope.launch {
                try {
                    val eventEntity =
                        UsageAnalyticsEntity(
                            eventType = event.eventName,
                            eventData = encodeEventData(data.parameters),
                            timestamp = data.timestamp,
                            sessionId = currentSessionId,
                        )

                    usageAnalyticsDao.insertEvent(eventEntity)

                    // Log to console in debug mode
                    if (BuildConfig.DEBUG) {
                        android.util.Log.d(TAG, "Analytics: ${event.eventName} - ${data.parameters}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Failed to track event: ${event.eventName}", e)
                }
            }
        }

        fun trackClipboardOperation(
            operation: String,
            contentType: String,
            contentLength: Int,
            isSecure: Boolean = false,
        ) {
            val event =
                when (operation) {
                    "copy" -> AnalyticsEvent.ClipboardItemCopied
                    "delete" -> AnalyticsEvent.ClipboardItemDeleted
                    "pin" -> AnalyticsEvent.ClipboardItemPinned
                    "unpin" -> AnalyticsEvent.ClipboardItemUnpinned
                    else -> return
                }

            trackEvent(
                event,
                AnalyticsData(
                    parameters =
                        mapOf(
                            "content_type" to contentType,
                            "content_length" to contentLength,
                            "is_secure" to isSecure,
                        ),
                ),
            )
        }

        fun trackSearch(
            query: String,
            resultCount: Int,
            filters: List<String>,
        ) {
            trackEvent(
                AnalyticsEvent.SearchPerformed,
                AnalyticsData(
                    parameters =
                        mapOf(
                            "query_length" to query.length,
                            "result_count" to resultCount,
                            "filters_applied" to filters.joinToString(","),
                            "has_results" to (resultCount > 0),
                        ),
                ),
            )
        }

        fun trackError(
            errorCode: String,
            errorMessage: String,
            isCritical: Boolean = false,
            stackTrace: String? = null,
        ) {
            trackEvent(
                AnalyticsEvent.ErrorOccurred,
                AnalyticsData(
                    parameters =
                        mapOf(
                            "error_code" to errorCode,
                            "error_message" to errorMessage,
                            "is_critical" to isCritical,
                            "stack_trace" to (stackTrace?.take(1000) ?: ""), // Limit stack trace length
                            "session_duration" to getSessionDuration(),
                        ),
                ),
            )
        }

        fun trackPerformance(
            operation: String,
            duration: Long,
            itemCount: Int = 0,
        ) {
            if (duration > 1000) { // Only track slow operations (>1 second)
                trackEvent(
                    AnalyticsEvent.DatabaseQuerySlow,
                    AnalyticsData(
                        parameters =
                            mapOf(
                                "operation" to operation,
                                "duration_ms" to duration,
                                "item_count" to itemCount,
                                "is_slow" to true,
                            ),
                    ),
                )
            }
        }

        fun trackMemoryWarning(
            availableMemory: Long,
            totalMemory: Long,
            threshold: Long,
        ) {
            trackEvent(
                AnalyticsEvent.MemoryWarning,
                AnalyticsData(
                    parameters =
                        mapOf(
                            "available_memory_mb" to (availableMemory / 1024 / 1024),
                            "total_memory_mb" to (totalMemory / 1024 / 1024),
                            "threshold_mb" to (threshold / 1024 / 1024),
                            "memory_pressure" to ((totalMemory - availableMemory).toFloat() / totalMemory),
                        ),
                ),
            )
        }

        fun trackSettingChange(
            settingName: String,
            oldValue: Any?,
            newValue: Any?,
        ) {
            trackEvent(
                AnalyticsEvent.SettingChanged,
                AnalyticsData(
                    parameters =
                        mapOf(
                            "setting_name" to settingName,
                            "old_value" to (oldValue?.toString() ?: "null"),
                            "new_value" to (newValue?.toString() ?: "null"),
                        ),
                ),
            )
        }

        suspend fun getAnalyticsSummary(days: Int = 7): AnalyticsSummary {
            val startTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            val events = usageAnalyticsDao.getEventsAfter(startTime)

            val eventCounts = events.groupBy { it.eventType }.mapValues { it.value.size }
            val sessionCount = events.map { it.sessionId }.distinct().size
            val totalEvents = events.size

            return AnalyticsSummary(
                periodDays = days,
                totalEvents = totalEvents,
                sessionCount = sessionCount,
                eventCounts = eventCounts,
                mostUsedFeature = eventCounts.maxByOrNull { it.value }?.key ?: "none",
                averageEventsPerSession = if (sessionCount > 0) totalEvents.toFloat() / sessionCount else 0f,
            )
        }

        fun setAnalyticsEnabled(enabled: Boolean) {
            isAnalyticsEnabled = enabled
            analyticsScope.launch {
                if (!enabled) {
                    // Clear all analytics data if disabled
                    usageAnalyticsDao.deleteOldEvents(System.currentTimeMillis())
                }
            }
        }

        private fun generateSessionId(): String {
            return "${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
        }

        private fun getSessionDuration(): Long {
            return System.currentTimeMillis() - currentSessionId.split("_")[0].toLong()
        }

        private fun getCurrentMemoryUsage(): Long {
            val runtime = Runtime.getRuntime()
            return runtime.totalMemory() - runtime.freeMemory()
        }

        private fun getAppVersion(): String {
            return try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionName ?: "1.0.0"
            } catch (e: Exception) {
                "1.0.0"
            }
        }

        private fun encodeEventData(parameters: Map<String, Any>): String {
            return try {
                kotlinx.serialization.json.Json.encodeToString(
                    kotlinx.serialization.json.JsonObject(
                        parameters.mapValues {
                            kotlinx.serialization.json.JsonPrimitive(it.value.toString())
                        },
                    ),
                )
            } catch (e: Exception) {
                "{}"
            }
        }

        private suspend fun cleanOldAnalyticsData() {
            try {
                val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days
                usageAnalyticsDao.deleteOldEvents(cutoffTime)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to clean old analytics data", e)
            }
        }

        fun startNewSession() {
            currentSessionId = generateSessionId()
            trackEvent(AnalyticsEvent.AppLaunched)
        }
    }

data class AnalyticsSummary(
    val periodDays: Int,
    val totalEvents: Int,
    val sessionCount: Int,
    val eventCounts: Map<String, Int>,
    val mostUsedFeature: String,
    val averageEventsPerSession: Float,
)

// Extension to UserPreferences for analytics
val UserPreferences.enableAnalytics: kotlinx.coroutines.flow.Flow<Boolean>
    get() = kotlinx.coroutines.flow.flowOf(true) // Default implementation
