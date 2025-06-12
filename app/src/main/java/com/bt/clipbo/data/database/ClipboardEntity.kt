package com.bt.clipbo.data.database


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_items")
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "type")
    val type: String, // TEXT, URL, EMAIL, PHONE, PASSWORD, IBAN

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "is_secure")
    val isSecure: Boolean = false,

    @ColumnInfo(name = "tags")
    val tags: String = "", // JSON string olarak saklanacak

    @ColumnInfo(name = "preview")
    val preview: String = "" // İlk 100 karakter
)