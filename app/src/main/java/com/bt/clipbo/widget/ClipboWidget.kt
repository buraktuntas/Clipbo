package com.bt.clipbo.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.*
import com.bt.clipbo.ClipboApplication
import com.bt.clipbo.data.service.ClipboardService

class ClipboWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 110.dp), // Small - 3x2
            DpSize(250.dp, 110.dp), // Medium - 4x2
            DpSize(250.dp, 180.dp), // Large - 4x3
            DpSize(320.dp, 180.dp), // Extra Large - 5x3
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Widget güncelleme service'ini çağır
        updateWidgetData(context)

        provideContent {
            GlanceTheme {
                ClipboWidgetContent(context = context)
            }
        }
    }

    private suspend fun updateWidgetData(context: Context) {
        try {
            // Widget verilerini güncelle
            val widgetRepository = try {
                // Hilt container'dan repository al
                val hiltComponent = (context.applicationContext as ClipboApplication)
                // Direct injection olmadığı için fallback kullan
                null
            } catch (e: Exception) {
                null
            }

            // Service status güncelle
            val isServiceRunning = ClipboardService.isServiceRunning(context)
            context.getSharedPreferences("clipbo_widget_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("service_running", isServiceRunning)
                .putLong("last_update", System.currentTimeMillis())
                .apply()

        } catch (e: Exception) {
            Log.e("ClipboWidget", "Failed to update widget data", e)
        }
    }
}
