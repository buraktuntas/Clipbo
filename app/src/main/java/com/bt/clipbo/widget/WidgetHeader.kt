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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

@Composable
fun WidgetHeader(
    isServiceRunning: Boolean,
    itemCount: Int
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        // Status indicator
        Box(
            modifier = GlanceModifier
                .size(8.dp)
                .background(
                    if (isServiceRunning) {
                        androidx.glance.unit.ColorProvider(Color(0xFF4CAF50))
                    } else {
                        androidx.glance.unit.ColorProvider(Color(0xFFE57373))
                    }
                )
        ){}

        Spacer(modifier = GlanceModifier.width(6.dp))

        Text(
            text = "📋 Clipbo",
            style = TextStyle(
                color = ColorProvider(
                    day = Color(0xFF2D2D2D),
                    night = Color(0xFFFFFFFF)
                ),
                fontSize = 14.sp
            )
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        Text(
            text = "$itemCount",
            style = TextStyle(
                color = ColorProvider(
                    day = Color(0xFF7B4397),
                    night = Color(0xFFBB86FC)
                ),
                fontSize = 12.sp
            )
        )
    }
}
