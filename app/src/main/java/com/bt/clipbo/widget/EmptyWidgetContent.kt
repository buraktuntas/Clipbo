package com.bt.clipbo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.bt.clipbo.presentation.ui.main.MainActivity

@Composable
fun EmptyStateWidget() {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = "📝",
            style = TextStyle(fontSize = 24.sp)
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        Text(
            text = "Henüz kopyalanan öğe yok",
            style = TextStyle(
                fontSize = 10.sp,
                color = ColorProvider(
                    day = Color(0xFF666666),
                    night = Color(0xFF999999)
                )
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Button(
            text = "Uygulamayı Aç",
            onClick = actionStartActivity<MainActivity>()
        )
    }
}

fun getServiceStatus(context: Context): Boolean {
    return try {
        context.getSharedPreferences("clipbo_widget_prefs", Context.MODE_PRIVATE)
            .getBoolean("service_running", false)
    } catch (e: Exception) {
        false
    }
}

fun getRecentItemsFromPrefs(context: Context, limit: Int): List<WidgetClipboardItem> {
    // Basit implementation - gerçek projede Room database kullanılacak
    return emptyList()
}

fun formatLastUpdate(context: Context): String {
    val lastUpdate = context.getSharedPreferences("clipbo_widget_prefs", Context.MODE_PRIVATE)
        .getLong("last_update", 0)

    return if (lastUpdate > 0) {
        val diff = System.currentTimeMillis() - lastUpdate
        when {
            diff < 60_000 -> "az önce"
            diff < 3600_000 -> "${diff / 60_000}dk önce"
            else -> "${diff / 3600_000}s önce"
        }
    } else {
        "bilinmiyor"
    }
}

fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "şimdi"
        diff < 3600_000 -> "${diff / 60_000}dk"
        diff < 86400_000 -> "${diff / 3600_000}s"
        diff < 604800_000 -> "${diff / 86400_000}g"
        else -> "${diff / 604800_000}h"
    }
}
