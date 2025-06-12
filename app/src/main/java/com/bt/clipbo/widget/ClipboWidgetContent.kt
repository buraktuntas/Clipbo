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
import com.bt.clipbo.presentation.ui.components.getTypeIcon
import com.bt.clipbo.presentation.ui.main.MainActivity
import com.bt.clipbo.widget.repository.WidgetRepository

@Composable
fun ClipboWidgetContent() {
    // Repository'yi güvenli şekilde al
    val widgetRepository = try {
        WidgetRepository.getInstance()
    } catch (e: Exception) {
        null
    }

    // Eğer repository yoksa, fallback UI göster
    if (widgetRepository == null) {
        FallbackWidgetContent()
        return
    }

    // Normal widget içeriği
    val recentItems by widgetRepository.getRecentItems(6).collectAsState(initial = emptyList())
    val isServiceRunning by widgetRepository.isServiceRunning().collectAsState(initial = false)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(
                ColorProvider(
                    day = Color(0xFFF8F4FF),
                    night = Color(0xFF1A1A1A)
                )
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            // Header
            WidgetHeader(
                isServiceRunning = isServiceRunning,
                itemCount = recentItems.size
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Content
            if (recentItems.isEmpty()) {
                EmptyWidgetContent()
            } else {
                ClipboardItemsList(items = recentItems)
            }
        }
    }
}

@Composable
fun FallbackWidgetContent() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(
                ColorProvider(
                    day = Color(0xFFF8F4FF),
                    night = Color(0xFF1A1A1A)
                )
            )
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "📋",
                style = TextStyle(fontSize = 32.sp)
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = "Clipbo",
                style = TextStyle(
                    color = ColorProvider(
                        day = Color(0xFF2D2D2D),
                        night = Color(0xFFFFFFFF)
                    ),
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = "Dokunarak açın",
                style = TextStyle(
                    color = ColorProvider(
                        day = Color(0xFF666666),
                        night = Color(0xFF999999)
                    ),
                    fontSize = 12.sp
                )
            )
        }
    }
}