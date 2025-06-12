package com.bt.clipbo.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {

    @Query("SELECT * FROM clipboard_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboard_items WHERE is_pinned = 1 ORDER BY timestamp DESC")
    fun getPinnedItems(): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboard_items WHERE is_secure = 1 ORDER BY timestamp DESC")
    fun getSecureItems(): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboard_items WHERE content LIKE '%' || :searchQuery || '%' ORDER BY timestamp DESC")
    fun searchItems(searchQuery: String): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboard_items WHERE type = :type ORDER BY timestamp DESC")
    fun getItemsByType(type: String): Flow<List<ClipboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ClipboardEntity): Long

    @Update
    suspend fun updateItem(item: ClipboardEntity)

    @Delete
    suspend fun deleteItem(item: ClipboardEntity)

    @Query("DELETE FROM clipboard_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM clipboard_items WHERE is_pinned = 0")
    suspend fun deleteAllUnpinned()

    @Query("SELECT COUNT(*) FROM clipboard_items")
    suspend fun getItemCount(): Int

    @Query("DELETE FROM clipboard_items WHERE id NOT IN (SELECT id FROM clipboard_items ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun keepOnlyLatest(limit: Int)

    @Query("UPDATE clipboard_items SET timestamp = :timestamp WHERE content = :content")
    suspend fun updateItemTimestamp(content: String, timestamp: Long)
}