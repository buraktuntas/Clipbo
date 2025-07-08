package com.bt.clipbo.widget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.bt.clipbo.data.database.ClipboardDatabase
import com.bt.clipbo.data.service.ClipboardService
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

object WidgetUtils {
    private const val TAG = "WidgetUtils"

    /**
     * Update all widgets safely
     */
    suspend fun updateAllWidgets(context: Context) {
        try {
            ClipboWidget().updateAll(context)
            Log.d(TAG, "✅ All widgets updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to update widgets", e)
        }
    }

    /**
     * Copy content to clipboard with widget feedback
     */
    fun copyToClipboard(context: Context, content: String, showFeedback: Boolean = true) {
        try {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Clipbo Widget", content)
            clipboardManager.setPrimaryClip(clipData)

            if (showFeedback) {
                android.widget.Toast.makeText(
                    context,
                    "📋 Panoya kopyalandı",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }

            Log.d(TAG, "Content copied to clipboard from widget")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy to clipboard", e)
            if (showFeedback) {
                android.widget.Toast.makeText(
                    context,
                    "❌ Kopyalama başarısız",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Get widget preview text with smart truncation
     */
    fun getWidgetPreviewText(content: String, maxLength: Int = 30): String {
        return when {
            content.length <= maxLength -> content
            content.contains(" ") -> {
                // Word-aware truncation
                val words = content.split(" ")
                var result = ""
                for (word in words) {
                    if ((result + word).length > maxLength - 3) break
                    result += if (result.isEmpty()) word else " $word"
                }
                "$result..."
            }
            else -> content.take(maxLength - 3) + "..."
        }
    }

    /**
     * Format time for widget display
     */
    fun formatTimeForWidget(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "az önce"
            diff < 3600_000 -> "${diff / 60_000}dk"
            diff < 86400_000 -> "${diff / 3600_000}s"
            diff < 604800_000 -> "${diff / 86400_000}g"
            else -> "${diff / 604800_000}h"
        }
    }

    /**
     * Get content type emoji for widget
     */
    fun getTypeEmoji(type: String): String {
        return when (type.uppercase()) {
            "URL" -> "🔗"
            "EMAIL" -> "📧"
            "PHONE" -> "📱"
            "PASSWORD" -> "🔒"
            "IBAN" -> "🏦"
            "PIN" -> "🔢"
            "ADDRESS" -> "📍"
            else -> "📝"
        }
    }

    /**
     * Get content type display name
     */
    fun getContentTypeDisplayName(type: String): String {
        return when (type.uppercase()) {
            "URL" -> "Link"
            "EMAIL" -> "E-posta"
            "PHONE" -> "Telefon"
            "PASSWORD" -> "Şifre"
            "IBAN" -> "IBAN"
            "PIN" -> "PIN"
            "ADDRESS" -> "Adres"
            "TEXT" -> "Metin"
            else -> "Öğe"
        }
    }

    /**
     * Check if content should be masked in widget
     */
    fun shouldMaskContent(isSecure: Boolean, type: String): Boolean {
        return isSecure || type.uppercase() in listOf("PASSWORD", "PIN", "IBAN")
    }

    /**
     * Get masked content for display
     */
    fun getMaskedContent(content: String, type: String): String {
        return when (type.uppercase()) {
            "PASSWORD" -> "•".repeat(minOf(content.length, 12))
            "PIN" -> "•".repeat(content.length)
            "IBAN" -> "TR** **** **** ****"
            else -> "•".repeat(8)
        }
    }

    /**
     * Update widget cache with new data
     */
    suspend fun updateWidgetCache(context: Context, items: List<WidgetClipboardItem>) {
        try {
            val prefs = context.getSharedPreferences("clipbo_widget_cache", Context.MODE_PRIVATE)
            val editor = prefs.edit()

            // JSON Array oluştur
            val jsonArray = JSONArray()
            items.take(10).forEach { item ->
                val jsonObject = JSONObject().apply {
                    put("id", item.id)
                    put("content", item.content)
                    put("preview", item.preview)
                    put("type", item.type)
                    put("timestamp", item.timestamp)
                    put("isPinned", item.isPinned)
                    put("isSecure", item.isSecure)
                }
                jsonArray.put(jsonObject)
            }

            editor.putString("cached_items", jsonArray.toString())
            editor.putLong("cache_timestamp", System.currentTimeMillis())
            editor.putInt("item_count", items.size)
            editor.apply()

            Log.d(TAG, "Widget cache updated with ${items.size} items")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update widget cache", e)
        }
    }
    /**
     * Get cached items for widget
     */
    suspend fun getCachedItems(context: Context): List<WidgetClipboardItem> {
        return try {
            val prefs = context.getSharedPreferences("clipbo_widget_cache", Context.MODE_PRIVATE)
            val itemsJson = prefs.getString("cached_items", null)
            val cacheTimestamp = prefs.getLong("cache_timestamp", 0)

            // Cache 5 dakikadan eski mi?
            val isStale = System.currentTimeMillis() - cacheTimestamp > 300_000

            if (itemsJson != null && !isStale) {
                kotlinx.serialization.json.Json.decodeFromString<List<WidgetClipboardItem>>(itemsJson)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get cached items", e)
            emptyList()
        }
    }

    /**
     * Get service status for widget
     */
    fun getServiceStatus(context: Context): ServiceStatusInfo {
        return try {
            val prefs = context.getSharedPreferences("clipbo_widget_prefs", Context.MODE_PRIVATE)
            val isRunning = prefs.getBoolean("service_running", false)
            val lastUpdate = prefs.getLong("last_update", 0)

            ServiceStatusInfo(
                isRunning = isRunning,
                lastUpdate = lastUpdate,
                isStale = System.currentTimeMillis() - lastUpdate > 120_000 // 2 dakika
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get service status", e)
            ServiceStatusInfo(false, 0, true)
        }
    }

    /**
     * Force refresh widget data
     */
    suspend fun forceRefreshWidget(context: Context) {
        try {
            // Database'den fresh data al
            val database = ClipboardDatabase.buildDatabase(context)
            val items = database.clipboardDao().getAllItems().first()

            // Widget item'lara dönüştür
            val widgetItems = items.take(10).map { entity ->
                WidgetClipboardItem(
                    id = entity.id,
                    content = entity.content,
                    preview = getWidgetPreviewText(entity.content),
                    type = entity.type,
                    timestamp = entity.timestamp,
                    isPinned = entity.isPinned,
                    isSecure = entity.isSecure
                )
            }

            // Cache güncelle
            updateWidgetCache(context, widgetItems)

            // Service status güncelle
            val isServiceRunning = ClipboardService.isServiceRunning(context)
            context.getSharedPreferences("clipbo_widget_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("service_running", isServiceRunning)
                .putLong("last_update", System.currentTimeMillis())
                .apply()

            // Widget'ları güncelle
            updateAllWidgets(context)

            Log.d(TAG, "Widget force refresh completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to force refresh widget", e)
        }
    }

    /**
     * Clear widget cache
     */
    fun clearWidgetCache(context: Context) {
        try {
            context.getSharedPreferences("clipbo_widget_cache", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

            Log.d(TAG, "Widget cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear widget cache", e)
        }
    }
}

/**
 * Service status info data class
 */
data class ServiceStatusInfo(
    val isRunning: Boolean,
    val lastUpdate: Long,
    val isStale: Boolean
) {
    val statusText: String
        get() = when {
            !isRunning -> "Kapalı"
            isStale -> "Belirsiz"
            else -> "Aktif"
        }

    val statusEmoji: String
        get() = when {
            !isRunning -> "❌"
            isStale -> "⚠️"
            else -> "✅"
        }
}