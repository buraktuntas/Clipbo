package com.bt.clipbo.domain.usecase

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.data.repository.ClipboardRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardUseCase
    @Inject
    constructor(
        private val repository: ClipboardRepository,
        @ApplicationContext private val context: Context,
    ) {
        fun getAllItems(): Flow<List<ClipboardEntity>> = repository.getAllItems()

        fun getPinnedItems(): Flow<List<ClipboardEntity>> = repository.getPinnedItems()

        fun getSecureItems(): Flow<List<ClipboardEntity>> = repository.getSecureItems()

        fun searchItems(query: String): Flow<List<ClipboardEntity>> = repository.searchItems(query)

        suspend fun copyToClipboard(content: String) {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Clipbo", content)
            clipboardManager.setPrimaryClip(clipData)

            // Mevcut öğeyi güncelle (eğer varsa)
            repository.updateItemTimestamp(content)
        }

        suspend fun togglePin(item: ClipboardEntity) = repository.togglePin(item)

        suspend fun toggleSecure(item: ClipboardEntity) = repository.toggleSecure(item)

        suspend fun deleteItem(item: ClipboardEntity) = repository.deleteItem(item)

        suspend fun clearAllUnpinned() = repository.clearAllUnpinned()

        fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60_000 -> "Şimdi"
                diff < 3600_000 -> "${diff / 60_000} dakika önce"
                diff < 86400_000 -> "${diff / 3600_000} saat önce"
                diff < 604800_000 -> "${diff / 86400_000} gün önce"
                else -> "${diff / 604800_000} hafta önce"
            }
        }

        fun getTypeIcon(type: String): String {
            return when (type) {
                "URL" -> "🔗"
                "EMAIL" -> "📧"
                "PHONE" -> "📱"
                "IBAN" -> "🏦"
                "PASSWORD" -> "🔒"
                "PIN" -> "🔢"
                "ADDRESS" -> "📍"
                else -> "📋"
            }
        }
    }
