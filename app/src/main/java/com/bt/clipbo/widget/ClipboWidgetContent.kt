package com.bt.clipbo.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.bt.clipbo.presentation.ui.main.MainActivity
import com.bt.clipbo.widget.repository.WidgetRepository

@Composable
fun ClipboWidgetContent(context: Context) {
    val currentSize = LocalSize.current

    // Size-aware content
    when {
        currentSize.width < 200.dp -> SmallWidgetContent(context)
        currentSize.width < 280.dp -> MediumWidgetContent(context)
        else -> LargeWidgetContent(context)
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun SmallWidgetContent(context: Context) {
    val serviceRunning = getServiceStatus(context)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    day = Color(0xFFF8F4FF),
                    night = Color(0xFF1A1A1A)
                )
            )
            .appWidgetBackground()
            .cornerRadius(16.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // App icon
            Text(
                text = "📋",
                style = TextStyle(fontSize = 24.sp)
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = "Clipbo",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(
                        day = Color(0xFF2D2D2D),
                        night = Color(0xFFFFFFFF)
                    )
                )
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            // Status indicator
            Row(
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(6.dp)
                        .background(
                            if (serviceRunning) ColorProvider(Color(0xFF4CAF50))
                            else ColorProvider(Color(0xFFE57373))
                        )
                        .cornerRadius(3.dp)
                ){

                }

                Spacer(modifier = GlanceModifier.width(4.dp))

                Text(
                    text = if (serviceRunning) "Aktif" else "Kapalı",
                    style = TextStyle(
                        fontSize = 8.sp,
                        color = ColorProvider(
                            day = Color(0xFF666666),
                            night = Color(0xFF999999)
                        )
                    )
                )
            }
        }
    }
}
@SuppressLint("RestrictedApi")
@Composable
fun MediumWidgetContent(context: Context) {
    val serviceRunning = getServiceStatus(context)
    val recentItems = getRecentItemsFromPrefs(context, 3)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    day = Color(0xFFF8F4FF),
                    night = Color(0xFF1A1A1A)
                )
            )
            .appWidgetBackground()
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            // Header
            WidgetHeader(serviceRunning, recentItems.size)

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Content
            if (recentItems.isEmpty()) {
                EmptyStateWidget()
            } else {
                LazyColumn {
                    items(recentItems) { item ->
                        CompactClipboardItem(item)
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }

                    item {
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .clickable(actionStartActivity<MainActivity>()),
                            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                        ) {
                            Text(
                                text = "Daha fazla →",
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = ColorProvider(Color(0xFF7B4397))
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LargeWidgetContent(context: Context) {
    val serviceRunning = getServiceStatus(context)
    val recentItems = getRecentItemsFromPrefs(context, 5)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    day = Color(0xFFF8F4FF),
                    night = Color(0xFF1A1A1A)
                )
            )
            .appWidgetBackground()
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            // Enhanced header with quick actions
            WidgetHeader(serviceRunning, recentItems.size)

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Content with categories
            if (recentItems.isEmpty()) {
                EmptyStateWidget()
            } else {
                LazyColumn {
                    items(recentItems) { item ->
                        DetailedClipboardItem(item, context)
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }

                    item {
                        ActionButtonRow(context)
                    }
                }
            }
        }
    }
}

