package com.bt.clipbo.widget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.glance.appwidget.updateAll

object WidgetUtils {

    suspend fun updateAllWidgets(context: Context) {
        ClipboWidget().updateAll(context)
    }

    fun copyToClipboard(context: Context, content: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Clipbo Widget", content)
        clipboardManager.setPrimaryClip(clipData)
    }

    fun getWidgetPreviewText(content: String, maxLength: Int = 30): String {
        return if (content.length > maxLength) {
            content.take(maxLength) + "..."
        } else {
            content
        }
    }
}