package com.bt.clipbo.widget.repository

import android.content.Context
import com.bt.clipbo.data.database.ClipboardDao
import com.bt.clipbo.data.database.ClipboardDatabase
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.widget.WidgetClipboardItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class WidgetRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clipboardDao: ClipboardDao
) {

    companion object {
        @Volatile
        private var INSTANCE: WidgetRepository? = null

        fun getInstance(): WidgetRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: createFallbackInstance().also { INSTANCE = it }
            }
        }

        fun initialize(instance: WidgetRepository) {
            INSTANCE = instance
        }

        private fun createFallbackInstance(): WidgetRepository {
            // Fallback DAO implementation
            val fallbackDao = object : ClipboardDao {
                override fun getAllItems() = flowOf(emptyList<ClipboardEntity>())
                override fun getPinnedItems() = flowOf(emptyList<ClipboardEntity>())
                override fun getSecureItems() = flowOf(emptyList<ClipboardEntity>())
                override fun searchItems(searchQuery: String) = flowOf(emptyList<ClipboardEntity>())
                override fun getItemsByType(type: String) = flowOf(emptyList<ClipboardEntity>())
                override suspend fun insertItem(item: ClipboardEntity): Long = 0
                override suspend fun updateItem(item: ClipboardEntity) {}
                override suspend fun deleteItem(item: ClipboardEntity) {}
                override suspend fun deleteItemById(id: Long) {}
                override suspend fun deleteAllUnpinned() {}
                override suspend fun getItemCount(): Int = 0
                override suspend fun keepOnlyLatest(limit: Int) {}
                override suspend fun updateItemTimestamp(content: String, timestamp: Long) {}
            }

            val fallbackContext = android.app.Application()
            return FallbackWidgetRepository(fallbackContext, fallbackDao)
        }
    }

    open fun getRecentItems(limit: Int): Flow<List<WidgetClipboardItem>> {
        return clipboardDao.getAllItems()
            .map { entities ->
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
            .catch {
                // Hata durumunda boş liste döner
                emit(emptyList())
            }
    }

    open fun isServiceRunning(): Flow<Boolean> {
        return try {
            // SharedPreferences'dan service durumunu kontrol et
            val prefs = context.getSharedPreferences("clipbo_prefs", Context.MODE_PRIVATE)
            val isRunning = prefs.getBoolean("service_running", false)
            flowOf(isRunning)
        } catch (e: Exception) {
            flowOf(false)
        }
    }

    open fun updateServiceStatus(isRunning: Boolean) {
        try {
            val prefs = context.getSharedPreferences("clipbo_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("service_running", isRunning).apply()
        } catch (e: Exception) {
            // Ignore errors in fallback mode
        }
    }
}

// Fallback implementation class
private class FallbackWidgetRepository(
    context: Context,
    clipboardDao: ClipboardDao
) : WidgetRepository(context, clipboardDao) {

    override fun getRecentItems(limit: Int): Flow<List<WidgetClipboardItem>> {
        return flowOf(emptyList())
    }

    override fun isServiceRunning(): Flow<Boolean> {
        return flowOf(false)
    }

    override fun updateServiceStatus(isRunning: Boolean) {
        // Do nothing in fallback mode
    }
}

// Mock SharedPreferences for extreme fallback cases
private class MockSharedPreferences : android.content.SharedPreferences {
    override fun getAll(): MutableMap<String, *> = mutableMapOf<String, Any>()
    override fun getString(key: String?, defValue: String?) = defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?) = defValues
    override fun getInt(key: String?, defValue: Int) = defValue
    override fun getLong(key: String?, defValue: Long) = defValue
    override fun getFloat(key: String?, defValue: Float) = defValue
    override fun getBoolean(key: String?, defValue: Boolean) = defValue
    override fun contains(key: String?) = false
    override fun edit() = MockEditor()
    override fun registerOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
}

private class MockEditor : android.content.SharedPreferences.Editor {
    override fun putString(key: String?, value: String?) = this
    override fun putStringSet(key: String?, values: MutableSet<String>?) = this
    override fun putInt(key: String?, value: Int) = this
    override fun putLong(key: String?, value: Long) = this
    override fun putFloat(key: String?, value: Float) = this
    override fun putBoolean(key: String?, value: Boolean) = this
    override fun remove(key: String?) = this
    override fun clear() = this
    override fun commit() = true
    override fun apply() {}
}