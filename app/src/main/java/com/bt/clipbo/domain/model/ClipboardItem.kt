package com.bt.clipbo.domain.model

import com.bt.clipbo.data.database.ClipboardEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Domain model for clipboard items
 * Converts database entity to business logic model
 */
data class ClipboardItem(
    val id: Long,
    val content: String,
    val timestamp: Long,
    val type: ClipboardType,
    val isPinned: Boolean,
    val isSecure: Boolean,
    val tags: List<String>,
    val preview: String,
) {
    enum class ClipboardType(val displayName: String, val icon: String) {
        TEXT("Metin", "📝"),
        URL("Link", "🔗"),
        EMAIL("E-posta", "📧"),
        PHONE("Telefon", "📱"),
        PASSWORD("Şifre", "🔒"),
        IBAN("IBAN", "🏦"),
        PIN("PIN", "🔢"),
        ADDRESS("Adres", "📍"),
        ;

        companion object {
            fun fromString(type: String): ClipboardType {
                return values().find { it.name == type } ?: TEXT
            }
        }
    }

    /**
     * Formatted timestamp for display
     */
    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Şimdi"
            diff < 3600_000 -> "${diff / 60_000} dakika önce"
            diff < 86400_000 -> "${diff / 3600_000} saat önce"
            diff < 604800_000 -> "${diff / 86400_000} gün önce"
            else -> SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
    }

    /**
     * Get display content (masked for secure items)
     */
    fun getDisplayContent(): String {
        return if (isSecure && type in listOf(ClipboardType.PASSWORD, ClipboardType.PIN)) {
            "•".repeat(minOf(content.length, 12))
        } else {
            content
        }
    }

    /**
     * Check if content contains search query
     */
    fun matchesSearch(query: String): Boolean {
        if (query.isBlank()) return true

        val searchQuery = query.lowercase()
        return content.lowercase().contains(searchQuery) ||
            type.displayName.lowercase().contains(searchQuery) ||
            tags.any { it.lowercase().contains(searchQuery) }
    }

    /**
     * Get content size info
     */
    fun getSizeInfo(): String {
        return "${content.length} karakter"
    }

    companion object {
        /**
         * Convert from database entity
         */
        fun fromEntity(entity: ClipboardEntity): ClipboardItem {
            return ClipboardItem(
                id = entity.id,
                content = entity.content,
                timestamp = entity.timestamp,
                type = ClipboardType.fromString(entity.type),
                isPinned = entity.isPinned,
                isSecure = entity.isSecure,
                tags = entity.tags.split(",").filter { it.isNotBlank() },
                preview = entity.preview,
            )
        }

        /**
         * Convert to database entity
         */
        fun toEntity(item: ClipboardItem): ClipboardEntity {
            return ClipboardEntity(
                id = item.id,
                content = item.content,
                timestamp = item.timestamp,
                type = item.type.name,
                isPinned = item.isPinned,
                isSecure = item.isSecure,
                tags = item.tags.joinToString(","),
                preview = item.preview.ifEmpty { item.content.take(100) },
            )
        }
    }
}
