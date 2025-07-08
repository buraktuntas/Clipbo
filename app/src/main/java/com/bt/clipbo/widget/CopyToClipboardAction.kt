package com.bt.clipbo.widget

// WidgetActions.kt
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.updateAll
import com.bt.clipbo.data.database.ClipboardDatabase
import com.bt.clipbo.data.service.ClipboardService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Copy text to clipboard action
 */
class CopyToClipboardAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        try {
            val content = parameters[contentKey] ?: return

            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Clipbo Widget", content)
            clipboardManager.setPrimaryClip(clipData)

            // Widget güncelle
            ClipboWidget().updateAll(context)

            // Toast mesajı (opsiyonel)
            showToast(context, "📋 Panoya kopyalandı")

            Log.d("WidgetAction", "Content copied to clipboard from widget")

        } catch (e: Exception) {
            Log.e("WidgetAction", "Failed to copy content", e)
        }
    }

    companion object {
        val contentKey = ActionParameters.Key<String>("content")
    }
}

/**
 * Paste last item action
 */
class PasteLastItemAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        try {
            // En son clipboard öğesini database'den al
            val database = ClipboardDatabase.buildDatabase(context)
            val clipboardDao = database.clipboardDao()

            val allItems = clipboardDao.getAllItems().first()
            val lastItem = allItems.firstOrNull()

            if (lastItem != null) {
                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("Clipbo Widget", lastItem.content)
                clipboardManager.setPrimaryClip(clipData)

                showToast(context, "📋 Son öğe yapıştırıldı")
                Log.d("WidgetAction", "Last item pasted from widget")
            } else {
                showToast(context, "❌ Yapıştırılacak öğe yok")
            }

            // Widget güncelle
            ClipboWidget().updateAll(context)

        } catch (e: Exception) {
            Log.e("WidgetAction", "Failed to paste last item", e)
            showToast(context, "❌ Yapıştırma başarısız")
        }
    }
}

/**
 * Refresh widget action
 */
class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        try {
            // Service status kontrol et
            val isServiceRunning = ClipboardService.isServiceRunning(context)

            // SharedPreferences güncelle
            context.getSharedPreferences("clipbo_widget_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("service_running", isServiceRunning)
                .putLong("last_update", System.currentTimeMillis())
                .apply()

            // Widget güncelle
            ClipboWidget().updateAll(context)

            showToast(context, "🔄 Widget güncellendi")
            Log.d("WidgetAction", "Widget refreshed")

        } catch (e: Exception) {
            Log.e("WidgetAction", "Failed to refresh widget", e)
        }
    }
}

/**
 * Toggle service action
 */
class ToggleServiceAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        try {
            val isCurrentlyRunning = ClipboardService.isServiceRunning(context)

            if (isCurrentlyRunning) {
                ClipboardService.stopService(context)
                showToast(context, "⏹️ Servis durduruldu")
            } else {
                ClipboardService.startService(context)
                showToast(context, "▶️ Servis başlatıldı")
            }

            // Widget güncelle
            delay(1000) // Service'in başlaması için bekle
            ClipboWidget().updateAll(context)

            Log.d("WidgetAction", "Service toggled from widget")

        } catch (e: Exception) {
            Log.e("WidgetAction", "Failed to toggle service", e)
            showToast(context, "❌ Servis kontrolü başarısız")
        }
    }
}

/**
 * Helper function to show toast
 */
private fun showToast(context: Context, message: String) {
    try {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("WidgetAction", "Failed to show toast", e)
    }
}

// Action parameter keys
object ActionKeys {
    val CONTENT = ActionParameters.Key<String>("content")
    val ITEM_ID = ActionParameters.Key<Long>("item_id")
    val ACTION_TYPE = ActionParameters.Key<String>("action_type")
}