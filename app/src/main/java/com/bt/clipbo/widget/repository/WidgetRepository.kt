package com.bt.clipbo.widget.repository

import android.content.Context
import com.bt.clipbo.data.database.ClipboardDao
import com.bt.clipbo.data.database.ClipboardDatabase
import com.bt.clipbo.widget.WidgetClipboardItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clipboardDao: ClipboardDao
) {

    companion object {
        @Volatile
        private var INSTANCE: WidgetRepository? = null

        fun getInstance(): WidgetRepository {
            return INSTANCE ?: synchronized(this) {
                throw IllegalStateException("WidgetRepository not initialized")
            }
        }

        fun initialize(instance: WidgetRepository) {
            INSTANCE = instance
        }
    }

    fun getRecentItems(limit: Int): Flow<List<WidgetClipboardItem>> {
        return clipboardDao.getAllItems().map { entities ->
            entities.take(limit).map { entity ->
                WidgetClipboardItem(
                    id = entity.id,
                    content = entity.content,
                    preview = entity.preview.ifEmpty { entity.content.take(30) },
                    type = entity.type,
                    timestamp = entity.timestamp,
                    isPinned = entity.isPinned,
                    isSecure = entity.isSecure
                )
            }
        }
    }

    fun isServiceRunning(): Flow<Boolean> {
        // Service durumunu kontrol et (SharedPreferences veya başka yöntemle)
        return kotlinx.coroutines.flow.flowOf(true) // Placeholder
    }
}