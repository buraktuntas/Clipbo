package com.bt.clipbo.widget

data class WidgetClipboardItem(
    val id: Long,
    val content: String,
    val preview: String,
    val type: String,
    val timestamp: Long,
    val isPinned: Boolean,
    val isSecure: Boolean
)