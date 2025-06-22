package com.bt.clipbo.data.repository

import com.bt.clipbo.data.database.ClipboardDao
import com.bt.clipbo.data.database.ClipboardEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardRepository
    @Inject
    constructor(
        private val clipboardDao: ClipboardDao,
    ) {
        fun getAllItems(): Flow<List<ClipboardEntity>> = clipboardDao.getAllItems()

        fun getPinnedItems(): Flow<List<ClipboardEntity>> = clipboardDao.getPinnedItems()

        fun getSecureItems(): Flow<List<ClipboardEntity>> = clipboardDao.getSecureItems()

        fun searchItems(query: String): Flow<List<ClipboardEntity>> = clipboardDao.searchItems(query)

        fun getItemsByType(type: String): Flow<List<ClipboardEntity>> = clipboardDao.getItemsByType(type)

        suspend fun insertItem(item: ClipboardEntity): Long = clipboardDao.insertItem(item)

        suspend fun updateItem(item: ClipboardEntity) = clipboardDao.updateItem(item)

        suspend fun deleteItem(item: ClipboardEntity) = clipboardDao.deleteItem(item)

        suspend fun deleteItemById(id: Long) = clipboardDao.deleteItemById(id)

        suspend fun togglePin(item: ClipboardEntity) {
            val updatedItem = item.copy(isPinned = !item.isPinned)
            clipboardDao.updateItem(updatedItem)
        }

        suspend fun toggleSecure(item: ClipboardEntity) {
            val updatedItem = item.copy(isSecure = !item.isSecure)
            clipboardDao.updateItem(updatedItem)
        }

        suspend fun clearAllUnpinned() = clipboardDao.deleteAllUnpinned()

        suspend fun getItemCount(): Int = clipboardDao.getItemCount()

        suspend fun updateItemTimestamp(content: String) {
            clipboardDao.updateItemTimestamp(content, System.currentTimeMillis())
        }
    }
