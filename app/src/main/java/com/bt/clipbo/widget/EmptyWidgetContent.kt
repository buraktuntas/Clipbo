package com.bt.clipbo.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.bt.clipbo.presentation.ui.main.MainActivity
import com.bt.clipbo.widget.repository.WidgetRepository

@Composable
fun EmptyWidgetContent() {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = "📝",
            style = TextStyle(fontSize = 24.sp)
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        Text(
            text = "Henüz kopyalanan öğe yok",
            style = TextStyle(
                color = ColorProvider(
                    day = Color(0xFF666666),
                    night = Color(0xFF999999)
                ),
                fontSize = 10.sp
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Row(
            modifier = GlanceModifier
                .background(
                    ColorProvider(
                        day = Color(0xFF7B4397),
                        night = Color(0xFFBB86FC)
                    )
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Text(
                text = "Uygulamayı Aç",
                style = TextStyle(
                    color = androidx.glance.unit.ColorProvider(Color(0xFFFFFFFF)),
                    fontSize = 10.sp
                )
            )
        }
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